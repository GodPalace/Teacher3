package com.godpalace.student.module;

import com.godpalace.student.*;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Map;

@Slf4j
public class ResponseScanModule implements Module {
    @Override
    public short getID() {
        return -0x01;
    }

    @Override
    public String getName() {
        return "ResponseScanModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) {
        for (Map.Entry<InetAddress, NetworkInterface> entry : Main.getAddresses().entrySet()) {
            ThreadPoolManager.getExecutor().execute(() -> {
                InetSocketAddress address = new InetSocketAddress(entry.getKey(), Main.SCAN_PORT);
                InetSocketAddress group = new InetSocketAddress("224.3.7.1", Main.SCAN_PORT);

                try (MulticastSocket socket = new MulticastSocket(address)) {
                    socket.joinGroup(group, entry.getValue());
                    byte[] buffer = new byte[1];

                    while (true) {
                        try {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);

                            byte[] bytes = packet.getData();
                            byte request = bytes[0];

                            try {
                                if (request == 1) {
                                    // 表示教师希望连接
                                    SocketChannel channel = SocketChannel.open(
                                            new InetSocketAddress(packet.getAddress(), Main.SCAN_PORT));
                                    Teacher newTeacher = new Teacher(channel);

                                    for (NetworkCore core : Main.getCores()) {
                                        if (core.getAddr().equals(packet.getAddress())) {
                                            if (!NetworkCore.getTeachers().contains(newTeacher)) {
                                                core.addTeacher(newTeacher);

                                                log.debug("Added new teacher {} to core {}",
                                                        newTeacher.getIp(), packet.getAddress());
                                            }

                                            break;
                                        }
                                    }
                                }
                            } catch (Exception ignored) {
                            }
                        } catch (Exception e) {
                            log.error("Error while creating packet for {}", entry.getKey(), e);
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while scanning response from {}", entry.getKey(), e);
                }
            });
        }
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
