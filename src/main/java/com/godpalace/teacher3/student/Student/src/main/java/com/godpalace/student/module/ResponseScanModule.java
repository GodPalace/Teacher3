package com.godpalace.student.module;

import com.godpalace.student.*;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
        for (Interface anInterface : Main.getAddrInterfaces()) {
            ThreadPoolManager.getExecutor().execute(() -> {
                InetSocketAddress address = new InetSocketAddress(anInterface.addr(), Main.SCAN_PORT);
                InetSocketAddress group = new InetSocketAddress("224.3.7.1", Main.SCAN_PORT);

                try (MulticastSocket socket = new MulticastSocket(address)) {
                    socket.joinGroup(group, anInterface.iface());
                    byte[] buffer = new byte[1];

                    while (true) {
                        try {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);

                            byte[] bytes = packet.getData();
                            byte request = bytes[0];
                            log.debug("Received request {} from {}", request, packet.getAddress());

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
                            log.error("Error while creating packet for {}", anInterface.addr());
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while scanning response from {}", anInterface.addr());
                }
            });
        }
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
