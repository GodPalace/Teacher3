package com.godpalace.teacher3;

import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.manager.StudentManager;
import lombok.extern.slf4j.Slf4j;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.net.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RescanListener {
    private static final byte VERIFY_CODE = 0x52;

    public static void initialize() {
        registerIpv4();
        registerIpv6();
    }

    private static void registerIpv4() {
        HashMap<InetAddress, NetworkInterface> ipv4s = Main.getIpv4s();

        for (Map.Entry<InetAddress, NetworkInterface> entry : ipv4s.entrySet()) {
            new Thread(() -> {
                InetSocketAddress address = new InetSocketAddress(entry.getKey(), Main.RESCAN_PORT);
                InetSocketAddress group = new InetSocketAddress(Main.IPV4_RESCAN_GROUP, Main.RESCAN_PORT);

                try (MulticastSocket socket = new MulticastSocket(address)) {
                    socket.joinGroup(group, entry.getValue());
                    byte[] buffer = new byte[1];

                    while (true) {
                        try {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);

                            byte[] data = packet.getData();
                            byte verifyCode = data[0];

                            if (verifyCode == VERIFY_CODE) {
                                boolean hasSameStudent = false;
                                for (Student student : StudentManager.getStudents()) {
                                    InetSocketAddress addr = (InetSocketAddress) student.getChannel().remoteAddress();

                                    if (addr.getAddress().equals(packet.getAddress())) {
                                        hasSameStudent = true;
                                        break;
                                    }
                                }
                                if (hasSameStudent) continue;

                                Student student = StudentManager.connect(packet.getAddress().getHostAddress());
                                if (Main.isRunOnCmd()) {
                                    System.out.println("\n新的学生连接: " + student.getIp() + "(ID: " + student.getId() + ")");
                                    System.out.print("> ");
                                } else {
                                    Notification.show("新的学生连接", student.getIp(), ToastTypes.INFO);
                                }

                                log.info("Received rescan request from {}", packet.getAddress());
                            }
                        } catch (Exception e) {
                            log.error("Error while listening to ipv4", e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while registering ipv4", e);
                }
            }).start();
        }
    }

    private static void registerIpv6() {
        HashMap<InetAddress, NetworkInterface> ipv6s = Main.getIpv6s();

        for (Map.Entry<InetAddress, NetworkInterface> entry : ipv6s.entrySet()) {
            new Thread(() -> {
                InetSocketAddress address = new InetSocketAddress(entry.getKey(), Main.RESCAN_PORT);
                InetSocketAddress group = new InetSocketAddress(Main.IPV6_RESCAN_GROUP, Main.RESCAN_PORT);

                try (MulticastSocket socket = new MulticastSocket()) {
                    socket.joinGroup(group, entry.getValue());
                    byte[] buffer = new byte[1];

                    while (true) {
                        try {
                            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                            socket.receive(packet);

                            byte[] data = packet.getData();
                            byte verifyCode = data[0];

                            if (verifyCode == VERIFY_CODE) {
                                boolean hasSameStudent = false;
                                for (Student student : StudentManager.getStudents()) {
                                    InetSocketAddress addr = (InetSocketAddress) student.getChannel().remoteAddress();

                                    if (addr.getAddress().equals(packet.getAddress())) {
                                        hasSameStudent = true;
                                        break;
                                    }
                                }
                                if (hasSameStudent) continue;

                                Student student = StudentManager.connect(packet.getAddress().getHostAddress());
                                if (Main.isRunOnCmd()) {
                                    System.out.println("\n新的学生连接: " + student.getIp() + "(ID: " + student.getId() + ")");
                                    System.out.print("> ");
                                } else {
                                    Notification.show("新的学生连接", student.getIp(), ToastTypes.INFO);
                                }

                                log.info("Received rescan request from {}", packet.getAddress());
                            }
                        } catch (Exception e) {
                            log.error("Error while listening to ipv4", e);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error while registering ipv4", e);
                }
            }).start();
        }
    }
}
