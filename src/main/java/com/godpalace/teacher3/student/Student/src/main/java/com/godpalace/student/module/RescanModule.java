package com.godpalace.student.module;

import com.godpalace.student.Main;
import com.godpalace.student.Teacher;
import com.godpalace.student.manager.ThreadPoolManager;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.net.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RescanModule implements Module {
    private static final byte VERIFY_CODE = 0x52;

    @Override
    public short getID() {
        return -0x05;
    }

    @Override
    public String getName() {
        return "RescanModule";
    }

    @Override
    public ByteBuf execute(Teacher teacher, ByteBuf data) {
        ThreadPoolManager.getExecutor().execute(() -> {
            HashMap<InetAddress, NetworkInterface> addresses = Main.getAddresses();

            for (Map.Entry<InetAddress, NetworkInterface> entry : addresses.entrySet()) {
                try {
                    MulticastSocket socket = new MulticastSocket();

                    InetSocketAddress group = new InetSocketAddress((entry.getKey() instanceof Inet4Address ? Main.IPV4_RESCAN_GROUP : Main.IPV6_RESCAN_GROUP), Main.RESCAN_PORT);
                    socket.joinGroup(group, entry.getValue());

                    DatagramPacket packet = new DatagramPacket(new byte[]{VERIFY_CODE}, 1, group);
                    socket.send(packet);

                    socket.close();
                    log.info("RescanModule sent to {} on {}", group.getAddress(), entry.getValue().getDisplayName());
                } catch (Exception e) {
                    log.error("RescanModule error: {}", e.getMessage());
                }
            }
        });

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
