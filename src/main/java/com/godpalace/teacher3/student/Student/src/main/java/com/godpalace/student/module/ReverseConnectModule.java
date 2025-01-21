package com.godpalace.student.module;

import com.godpalace.student.Main;
import com.godpalace.student.Teacher;
import com.godpalace.student.ThreadPoolManager;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

/*
 * 数据文件格式:
 * 4字节: IP长度
 * IP长度字节: IP地址
 * 2字节: 端口号
 */

@Slf4j
public class ReverseConnectModule implements Module {
    @Override
    public short getID() {
        return -0x02;
    }

    @Override
    public String getName() {
        return "ReverseConnectModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) {
        ThreadPoolManager.getExecutor().execute(() -> {
            try {
                // 读取配置文件
                URL url = ReverseConnectModule.class.getResource("/ReverseConnectConfig.data");
                if (url == null) {
                    log.debug("Failed to find config file. Not enable reverse connect");
                    return;
                }

                InputStream in = url.openStream();

                // 解析配置文件
                int ipLength;
                String ip;
                short port;
                byte[] bytes = new byte[1];

                // 读取IP长度
                int read = in.read(bytes);
                if (read == -1) {
                    log.error("Failed to read IP length.");
                    return;
                }
                ipLength = bytes[0] & 0xFF;

                // 读取IP地址
                bytes = new byte[ipLength];
                read = in.read(bytes);
                if (read == -1) {
                    log.error("Failed to read IP address.");
                    return;
                }
                ip = new String(bytes, 0, ipLength, StandardCharsets.UTF_8);

                // 读取端口号
                bytes = new byte[2];
                read = in.read(bytes);
                if (read == -1) {
                    log.error("Failed to read port.");
                    return;
                }
                port = (short) ((bytes[0] & 0xFF) | (bytes[1] & 0xFF) << 8);

                in.close();
                log.debug("Config[IP: {}, Port: {}] successfully loaded.", ip, port);

                // 连接教师端
                while (true) {
                    try {
                        SocketChannel channel = SocketChannel.open(new InetSocketAddress(ip, port));
                        Teacher t = new Teacher(channel);
                        Main.getDefaultCore().addTeacher(t);

                        log.debug("Connected to teacher: {}", t.getIp());
                        break;
                    } catch (Exception e) {
                        log.debug("Failed to connect to teacher");

                        try {
                            synchronized (this) {
                                wait(10000);
                            }
                        } catch (InterruptedException e1) {
                            log.error("Failed to sleep");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Failed to execute module", e);
            }
        });
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
