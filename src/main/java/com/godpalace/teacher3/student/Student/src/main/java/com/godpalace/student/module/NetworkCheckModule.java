package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;

@Slf4j
public class NetworkCheckModule implements Module {
    @Override
    public short getID() {
        return -0x03;
    }

    @Override
    public String getName() {
        return "NetworkCheckModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) {
        boolean isAlive;

        Frame lockFrame = new Frame();
        lockFrame.setUndecorated(true);
        lockFrame.setAlwaysOnTop(true);
        lockFrame.setType(Frame.Type.UTILITY);
        lockFrame.setBackground(Color.BLACK);
        lockFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        lockFrame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0), "invisible cursor"));

        while (true) {
            try {
                isAlive = false;

                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface networkInterface = interfaces.nextElement();

                    if (networkInterface.isUp()) {
                        byte[] mac = networkInterface.getHardwareAddress();

                        if (mac != null) {
                            isAlive = true;
                            break;
                        }
                    }
                }

                lockFrame.setVisible(!isAlive);
                synchronized (this) {
                    wait(3000);
                }
            } catch (Exception e) {
                log.error("NetworkCheckModule error", e);
                break;
            }
        }
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
