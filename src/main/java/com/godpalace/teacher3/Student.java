package com.godpalace.teacher3;

import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.manager.ModuleManager;
import com.godpalace.teacher3.manager.StudentManager;
import com.godpalace.teacher3.manager.ThreadPoolManager;
import com.godpalace.teacher3.netty.DecryptHandler;
import com.godpalace.teacher3.netty.EncryptHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.Lz4FrameDecoder;
import io.netty.handler.codec.compression.Lz4FrameEncoder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class Student {
    @Getter
    private static final AtomicInteger idCounter = new AtomicInteger(0);

    @Getter
    private final Channel channel;

    @Getter
    private final Object lock = new Object();

    @Getter
    private final ConcurrentHashMap<Short, ConcurrentHashMap<Short, ByteBuf>> responses = new ConcurrentHashMap<>();
    private final AtomicReference<String> name = new AtomicReference<>("...");

    @Getter
    private final AtomicBoolean[] statuses = new AtomicBoolean[ModuleManager.getShellMap().size() + 1];

    @Getter
    private final String ip;

    @Getter
    private final int port;

    @Getter
    private final int id;

    public Student(Channel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new Lz4FrameEncoder());
        pipeline.addLast(new Lz4FrameDecoder());
        pipeline.addLast(new EncryptHandler());
        pipeline.addLast(new DecryptHandler());
        pipeline.addLast(new Student.ReadHandler());
        this.channel = channel;

        ThreadPoolManager.getExecutor().execute(() -> {
            name.set(((InetSocketAddress) channel.remoteAddress()).getAddress().getHostName());

            if (!Main.isRunOnCmd()) {
                if (StudentManager.getStudentTable() != null) {
                    StudentManager.getStudentTable().refresh();
                }
            }
        });

        ip = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();
        port = ((InetSocketAddress) channel.remoteAddress()).getPort();

        for (int i = 0; i < statuses.length; i++) statuses[i] = new AtomicBoolean(false);
        id = idCounter.getAndIncrement();
    }

    public short sendRequest(short id, ByteBuf buf) {
        if (channel.isActive()) {
            short timestamp = (short) Math.abs(System.nanoTime() % 65536);
            byte[] bytes = buf.array();

            ByteBuf data = Unpooled.buffer(4 + bytes.length);
            data.writeShort(id);
            data.writeShort(timestamp);
            data.writeBytes(bytes);
            channel.writeAndFlush(data);

            return timestamp;
        } else {
            throw new IllegalStateException("Channel is not active");
        }
    }

    public boolean getStatus(int id) {
        return statuses[id].get();
    }

    public void setStatus(int id, boolean status) {
        this.statuses[id].set(status);
    }

    public String getName() {
        return name.get();
    }

    public boolean isAlive() {
        return channel.isOpen();
    }

    public void close() {
        if (channel.isOpen()) {
            channel.close();
        }
    }

    public boolean isClosed() {
        return !channel.isOpen();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Student student) {
            return Arrays.equals(((InetSocketAddress) this.getChannel().remoteAddress()).getAddress().getAddress(),
                    ((InetSocketAddress) student.getChannel().remoteAddress()).getAddress().getAddress());
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + "(" + ip + ":" + port + ")";
    }

    class ReadHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            try {
                if (msg instanceof ByteBuf buf) {
                    short moduleId = buf.readShort();
                    short timestamp = buf.readShort();

                    if (buf.readableBytes() > 0) {
                        if (!responses.containsKey(moduleId)) {
                            responses.put(moduleId, new ConcurrentHashMap<>());
                        }

                        responses.get(moduleId).put(timestamp, buf.retain());
                        synchronized (lock) {
                            lock.notifyAll();
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error while reading message", e);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            StudentManager.removeStudent(Student.this);

            if (Main.isRunOnCmd()) {
                System.out.println("学生" + ip + "已断开连接");
            } else {
                Notification.show("提示", "学生" + ip + "已断开连接", ToastTypes.INFO);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Error while handling message: {}", Arrays.toString(cause.getStackTrace()));
            ctx.close();
        }
    }
}
