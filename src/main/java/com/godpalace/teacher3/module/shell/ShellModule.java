package com.godpalace.teacher3.module.shell;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import com.godpalace.teacher3.module.Module;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.compression.Lz4FrameDecoder;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Arrays;
import java.util.Random;

@Slf4j
public class ShellModule implements Module {
    private static final Random random = new Random();
    private static final EventLoopGroup workerGroup = new NioEventLoopGroup(1);

    @Override
    public short getID() {
        return 0x04;
    }

    @Override
    public String getName() {
        return "远程命令";
    }

    @Override
    public String getTooltip() {
        return "向学生发送命令，并执行命令";
    }

    @Override
    public Image getStatusImage() {
        return null;
    }

    @Override
    public void onGuiButtonAction() {
        ShellStage stage = new ShellStage(this);
        stage.show();
    }

    @Override
    public String getCommand() {
        return "shell";
    }

    @Override
    public void cmd(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(args).forEach(arg -> sb.append(arg).append(" "));
        String cmd = sb.toString().trim();

        // 选择第一个选中的学生
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return;

        runShell(student, cmd, new Listener() {
            @Override
            public void onShellResult(String result) {
                System.out.println(result);
            }

            @Override
            public void onShellError() {
                System.out.println("命令执行失败");
            }
        });
    }

    protected void runShell(Student student, String cmd, Listener listener) {
        Object lock = new Object();

        int port = random.nextInt(1000) + 37000;
        while (true) {
            try {
                InetAddress local = ((InetSocketAddress) student.getChannel().localAddress()).getAddress();

                ServerSocket serverSocket = new ServerSocket(port, 1, local);
                serverSocket.close();

                EventLoopGroup group = new NioEventLoopGroup(1);
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(group, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();

                                pipeline.addLast(new Lz4FrameDecoder());
                                pipeline.addLast(new ShellReceiveHandler(listener, lock));
                            }
                        });
                bootstrap.bind(local, port).sync();

                byte[] cmdBytes = cmd.getBytes();
                ByteBuf request = Unpooled.buffer(4 + cmdBytes.length);
                request.writeInt(port);
                request.writeBytes(cmdBytes);
                student.sendRequest(getID(), request);
                request.release();

                synchronized (lock) {
                    lock.wait(10000);
                }

                break;
            } catch (BindException e) {
                port = random.nextInt(1000) + 37000;
            } catch (Exception e) {
                log.error("命令发送失败");
            }
        }
    }

    static class ShellReceiveHandler extends ChannelInboundHandlerAdapter {
        private final Listener listener;
        private final Object lock;

        public ShellReceiveHandler(Listener listener, Object lock) {
            this.listener = listener;
            this.lock = lock;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ByteBuf buf) {
                byte[] bytes = new byte[buf.readableBytes()];
                buf.readBytes(bytes);
                String result = new String(bytes, "GB2312");

                if (result.equals("/SHELL_END/")) listener.onShellEnd();
                else if (result.equals("/SHELL_ERR/")) listener.onShellError();
                else listener.onShellResult(result);

                buf.release();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            synchronized (lock) {
                lock.notifyAll();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("ShellReceiveHandler exceptionCaught", cause);
            listener.onShellError();
            ctx.close();
        }
    }

    protected interface Listener {
        default void onShellResult(String result) {
        }

        default void onShellEnd() {
        }

        default void onShellError() {
        }
    }

    @Override
    public boolean isSupportMultiSelection() {
        return false;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return true;
    }
}
