package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import com.godpalace.student.manager.ThreadPoolManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.Lz4FrameEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
public class ShellModule implements Module {
    private static final String OS = System.getProperty("os.name").toLowerCase();

    public static boolean isLinux(){
        return OS.contains("linux");
    }

    public static boolean isMacOS(){
        return OS.contains("mac") && OS.indexOf("os") > 0 && !OS.contains("x");
    }

    public static boolean isMacOSX(){
        return OS.contains("mac") && OS.indexOf("os") > 0 && OS.indexOf("x") > 0;
    }

    public static boolean isWindows(){
        return OS.contains("windows");
    }

    @Override
    public short getID() {
        return 0x04;
    }

    @Override
    public String getName() {
        return "ShellModule";
    }

    @Override
    public ByteBuf execute(Teacher teacher, ByteBuf data) {
        int port = data.readInt();
        byte[] msgBytes = new byte[data.readableBytes()];
        data.readBytes(msgBytes);
        String msg = new String(msgBytes);

        ThreadPoolManager.getExecutor().execute(() -> {
            try {
                EventLoopGroup group = ThreadPoolManager.getGroup();
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ChannelInitializer<NioSocketChannel>() {
                            @Override
                            protected void initChannel(NioSocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();

                                pipeline.addLast(new Lz4FrameEncoder());
                                pipeline.addLast(new ShellResponseHandler(msg));
                            }
                        });
                bootstrap.connect(teacher.getIp(), port);
            } catch (Exception e) {
                log.error("ShellModule execute error", e);
            }
        });

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }

    static class ShellResponseHandler extends ChannelInboundHandlerAdapter {
        private final String msg;

        public ShellResponseHandler(String msg) {
            this.msg = msg;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            boolean isNeedWait = true;

            // 构造命令
            StringBuilder cmd = new StringBuilder();
            if (isWindows()) {
                if (msg.startsWith("start ")) isNeedWait = false;
                cmd.append("cmd /c \"");
            } else if (isLinux()) {
                if (msg.startsWith("gnome-terminal ")) isNeedWait = false;
                cmd.append("bash -c \"");
            } else if (isMacOS() || isMacOSX()) {
                if (msg.startsWith("open ")) isNeedWait = false;
                cmd.append("sh -c \"");
            }
            cmd.append(msg).append("\"");

            // 执行命令
            Process process;
            try {
                process = Runtime.getRuntime().exec(cmd.toString());
            } catch (IOException e) {
                // 发送错误消息
                String endMsg = "/SHELL_ERR/";
                ByteBuf buf = Unpooled.wrappedBuffer(endMsg.getBytes("GB2312"));
                ctx.writeAndFlush(buf);
                buf.release();

                log.error("ShellModule execute error", e);
                return;
            }

            if (isNeedWait && process != null) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "GB2312"));

                String line;
                while (process.isAlive()) {
                    while ((line = reader.readLine()) != null) {
                        ByteBuf buf = Unpooled.wrappedBuffer(line.getBytes("GB2312"));
                        ctx.writeAndFlush(buf);
                    }
                }

                reader.close();
            }

            // 发送结束消息
            String endMsg = "/SHELL_END/";
            ByteBuf buf = Unpooled.wrappedBuffer(endMsg.getBytes("GB2312"));
            ctx.writeAndFlush(buf);

            ctx.close();
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("ShellModule ShellResponseHandler exceptionCaught", cause);
            ctx.close();
        }
    }
}
