package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import com.godpalace.student.ThreadPoolManager;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
    public void execute(Teacher teacher, ByteBuffer data) {
        ThreadPoolManager.getExecutor().execute(() -> {
            byte[] msgBytes = new byte[data.remaining()];
            data.get(msgBytes);
            String msg = new String(msgBytes);
            short port = data.getShort();

            SocketChannel channel;
            try {
                if (port == 0) {
                    channel = teacher.getChannel();
                } else {
                    channel = SocketChannel.open(new InetSocketAddress(teacher.getIp(), port));
                }
            } catch (IOException e) {
                log.error("ShellModule execute error", e);
                return;
            }

            try {
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

                Process process;

                // 执行命令
                try {
                    process = Runtime.getRuntime().exec(cmd.toString());
                } catch (IOException e) {
                    // 发送错误消息
                    String endMsg = "/SHELL_ERR/";
                    sendResponseWithSize(channel, endMsg.getBytes("GBK"));

                    log.error("ShellModule execute error", e);
                    return;
                }

                if (isNeedWait && process != null) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(process.getInputStream()));

                    String line;
                    while (process.isAlive()) {
                        while ((line = reader.readLine()) != null) {
                            sendResponseWithSize(channel, line.getBytes());
                        }
                    }

                    reader.close();
                }
            } catch (Exception e) {
                log.error("ShellModule execute error", e);
            } finally {
                try {
                    // 发送结束消息
                    String endMsg = "/SHELL_END/";
                    sendResponseWithSize(channel, endMsg.getBytes("GBK"));
                } catch (Exception e) {
                    log.error("ShellModule execute error", e);
                }
            }
        });
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
