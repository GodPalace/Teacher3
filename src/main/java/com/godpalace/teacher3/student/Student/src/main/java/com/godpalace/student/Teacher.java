package com.godpalace.student;

import com.godpalace.student.manager.ThreadPoolManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.Lz4FrameDecoder;
import io.netty.handler.codec.compression.Lz4FrameEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Arrays;

@Slf4j
@Getter
public class Teacher {
    private final Channel channel;
    private final String ip;

    public Teacher(InetSocketAddress target) throws InterruptedException {
        EventLoopGroup group = ThreadPoolManager.getGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast(new Lz4FrameEncoder());
                        pipeline.addLast(new Lz4FrameDecoder());
                        pipeline.addLast(new ReaderHandler());
                    }
                });

        channel = bootstrap.connect(target).sync().channel();
        ip = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();

        NetworkCore.getTeachers().add(this);
    }

    public Teacher(Channel channel) {
        channel.pipeline().addLast(new Lz4FrameEncoder());
        channel.pipeline().addLast(new Lz4FrameDecoder());
        channel.pipeline().addLast(new ReaderHandler());

        this.channel = channel;
        this.ip = ((InetSocketAddress) channel.remoteAddress()).getAddress().getHostAddress();

        NetworkCore.getTeachers().add(this);
    }

    class ReaderHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof ByteBuf buf) {
                CommandHandler.handleCommand(Teacher.this, buf.retain());
                ReferenceCountUtil.release(buf);
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            NetworkCore.getTeachers().remove(Teacher.this);
            log.debug("Teacher {} disconnected", ip);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ctx.close();
        }
    }

    public void sendResponse(short id, short timestamp, ByteBuf buf) {
        ByteBuf dataBuf = Unpooled.buffer(4 + buf.readableBytes());
        dataBuf.writeShort(id);
        dataBuf.writeShort(timestamp);
        dataBuf.writeBytes(buf);

        channel.writeAndFlush(dataBuf);
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
        if (obj instanceof Teacher teacher) {
            return Arrays.equals(((InetSocketAddress) this.getChannel().remoteAddress()).getAddress().getAddress(),
                    ((InetSocketAddress) teacher.getChannel().remoteAddress()).getAddress().getAddress());
        } else {
            return false;
        }
    }
}
