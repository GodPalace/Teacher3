package com.godpalace.teacher3;

import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.manager.StudentManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantLock;

@Getter
@Slf4j
public class NetworkListener {
    private static final ReentrantLock lock = new ReentrantLock();

    private static int idCounter = 0;

    @Getter
    private final int id;

    @Getter
    private static final HashMap<Integer, NetworkListener> listeners = new HashMap<>();

    @Getter
    private static final ArrayList<NetworkListener> scanListeners = new ArrayList<>();

    private final InetSocketAddress address;
    private final EventLoopGroup workerGroup;
    private final EventLoopGroup group;

    public NetworkListener(InetSocketAddress address, boolean add) {
        this.address = address;

        group = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(group, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("Acceptor", new AcceptStudentHandler());
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.bind(address).syncUninterruptibly();

        if (add) {
            id = idCounter++;
            listeners.put(id, this);
        } else {
            id = -1;
        }
    }

    public void close() throws IOException {
        group.shutdownGracefully();
        workerGroup.shutdownGracefully();

        if (id != -1) {
            listeners.remove(id);
        }
    }

    @Override
    public String toString() {
        return "[" + id + "] " + address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    static class AcceptStudentHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Student student = new Student(ctx.channel());
            ObservableList<Student> students = StudentManager.getStudents();

            int index = students.indexOf(student);
            if (index >= 0) {
                Student s = students.get(index);

                if (s.isAlive()) {
                    Student.getIdCounter().decrementAndGet();
                    return;
                } else {
                    StudentManager.removeStudent(s);
                    s.close();
                }
            }

            StudentManager.addStudent(student);

            try {
                lock.lock();

                if (Main.isRunOnCmd()) {
                    System.out.println("\n新的学生连接: " + student.getIp() + "(ID: " + student.getId() + ")");
                    System.out.print("> ");
                } else {
                    Notification.show("新的学生连接", student.getIp(), ToastTypes.INFO);
                }
            } finally {
                lock.unlock();
            }

            super.channelActive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Student disconnected: {}", ctx.channel().remoteAddress());
            ctx.close();
        }
    }
}
