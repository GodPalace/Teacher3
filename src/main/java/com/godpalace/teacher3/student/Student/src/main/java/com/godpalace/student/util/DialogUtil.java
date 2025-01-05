package com.godpalace.student.util;

import javax.swing.*;
import java.awt.*;

public final class DialogUtil {
    private DialogUtil() {
    }

    public static void showMessage(String message,
                                   Color color, Color backgroundColor,
                                   Font font, long duration) {
        try {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            FontMetrics metrics = toolkit.getFontMetrics(font);

            JFrame frame = new JFrame();
            frame.setSize(metrics.stringWidth(message) + 10, metrics.getHeight() + 10);
            frame.setLocationRelativeTo(null);
            frame.setType(JFrame.Type.UTILITY);
            frame.setAlwaysOnTop(true);
            frame.setUndecorated(true);
            frame.setResizable(false);
            frame.setBackground(backgroundColor);

            JLabel label = new JLabel(message);
            label.setBackground(backgroundColor);
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setVerticalAlignment(JLabel.CENTER);
            label.setForeground(color);
            label.setFont(font);
            frame.setContentPane(label);

            new Thread(() -> {
                try {
                    Thread.sleep(duration);
                    frame.dispose();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    try {
                        synchronized (frame) {
                            frame.notifyAll();
                        }
                    } catch (Exception ignored) {
                    }
                }
            }).start();

            frame.setVisible(true);
            synchronized (frame) {
                frame.wait();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
