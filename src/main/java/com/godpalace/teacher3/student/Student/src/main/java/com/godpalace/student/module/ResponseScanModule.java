package com.godpalace.student.module;

import com.godpalace.student.Main;
import com.godpalace.student.NetworkCore;
import com.godpalace.student.Teacher;
import com.godpalace.student.manager.ThreadPoolManager;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
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
    public ByteBuf execute(Teacher teacher, ByteBuf data) {
        for (Map.Entry<InetAddress, NetworkInterface> entry : Main.getAddresses().entrySet()) {
            new Thread(() -> {
                InetAddress ipKey = entry.getKey();
                InetSocketAddress address = new InetSocketAddress(ipKey, Main.SCAN_PORT);
                InetSocketAddress group = new InetSocketAddress(
                        (ipKey instanceof Inet4Address? Main.IPV4_MULTICAST_GROUP : Main.IPV6_MULTICAST_GROUP),
                        Main.SCAN_PORT);

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
                                    boolean hasSameTeacher = false;
                                    for (Teacher t : NetworkCore.getTeachers()) {
                                        InetSocketAddress addr = (InetSocketAddress) t.getChannel().remoteAddress();

                                        if (addr.getAddress().equals(packet.getAddress())) {
                                            hasSameTeacher = true;
                                            break;
                                        }
                                    }
                                    if (hasSameTeacher) continue;

                                    Teacher newTeacher = new Teacher(
                                            new InetSocketAddress(packet.getAddress(), Main.SCAN_PORT));

                                    for (NetworkCore core : Main.getCores()) {
                                        if (core.getAddr().equals(packet.getAddress())) {
                                            log.debug("Added new teacher {} to core {}", newTeacher.getIp(), packet.getAddress());
                                            break;
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                log.error("Error while handling request from {}", packet.getAddress(), e);
                            }
                        } catch (Exception e) {
                            log.error("Error while creating packet for {}", ipKey, e);
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while scanning response from {}", ipKey, e);
                }
            }).start();
        }

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
