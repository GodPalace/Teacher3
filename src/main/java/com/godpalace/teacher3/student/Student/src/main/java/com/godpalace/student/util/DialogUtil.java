package com.godpalace.student.util;

import com.godpalace.student.manager.ThreadPoolManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
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

            ThreadPoolManager.getExecutor().execute(() -> {
                try {
                    Thread.sleep(duration);
                    frame.dispose();
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            });

            frame.setVisible(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
