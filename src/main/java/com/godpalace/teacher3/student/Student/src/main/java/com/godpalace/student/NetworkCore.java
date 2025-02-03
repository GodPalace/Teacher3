package com.godpalace.student;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class NetworkCore {
    @Getter
    private static final CopyOnWriteArrayList<Teacher> teachers = new CopyOnWriteArrayList<>();

    @Getter
    private final InetAddress addr;

    @Getter
    private final int port;

    private final EventLoopGroup acceptorGroup = new NioEventLoopGroup(1);
    private final EventLoopGroup acceptorWorkerGroup = new NioEventLoopGroup();

    public NetworkCore(InetAddress addr, int port) {
        this.addr = addr;
        this.port = port;
    }

    public void start() {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(acceptorGroup, acceptorWorkerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline pipeline = ch.pipeline();

                        pipeline.addLast("Acceptor", new AcceptorHandler());
                    }
                })
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        bootstrap.bind(addr, port).syncUninterruptibly();
    }

    static class AcceptorHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            Teacher teacher = new Teacher(ctx.channel());

            log.info("New teacher connected: {}", teacher.getIp());
            super.channelActive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught in AcceptorHandler", cause);
            ctx.close();
        }
    }

    public void close() {
        acceptorGroup.shutdownGracefully();
        acceptorWorkerGroup.shutdownGracefully();
    }

    public boolean isClosed() {
        return acceptorGroup.isShutdown() && acceptorWorkerGroup.isShutdown();
    }
}
