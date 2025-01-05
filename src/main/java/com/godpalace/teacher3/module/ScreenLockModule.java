package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Arrays;

@Slf4j
public class ScreenLockModule implements Module {
    private static BufferedImage LOCK_ICON = null;

    static {
        try {
            URL url = ScreenLockModule.class.getResource("/icon/ScreenLockIcon.png");
            if (url != null) {
                LOCK_ICON = ImageIO.read(url);
            }
        } catch (Exception e) {
            log.error("Failed to load lock icon", e);
        }
    }

    private boolean isLocked = false;

    @Override
    public short getID() {
        return 0x01;
    }

    @Override
    public String getName() {
        return (isLocked? "取消黑屏" : "开启黑屏");
    }

    @Override
    public String getTooltip() {
        return "屏幕锁定";
    }

    @Override
    public BufferedImage getIcon() {
        return LOCK_ICON;
    }

    @Override
    public String getCommand() {
        return "screen-lock";
    }

    @Override
    public void cmd(String[] args) {
        if (args.length != 1) {
            System.out.println("命令格式错误, 请使用格式: screen-lock [lock|unlock]");
            return;
        }

        switch (args[0]) {
            case "lock":
            case "on":
                try {
                    for (Student student : StudentManager.getSelectedStudents()) {
                        ByteBuffer data = ByteBuffer.allocate(1);
                        data.putChar('1');
                        data.flip();

                        sendCmd(student, data);
                    }
                } catch (Exception e) {
                    log.error("Failed to lock screen", e);
                }

                break;

            case "unlock":
            case "off":
                try {
                    for (Student student : StudentManager.getSelectedStudents()) {
                        ByteBuffer data = ByteBuffer.allocate(1);
                        data.putChar('0');
                        data.flip();

                        sendCmd(student, data);
                    }
                } catch (Exception e) {
                    log.error("Failed to unlock screen", e);
                }

                break;

            default:
                System.out.println("命令格式错误, 请使用格式: screen-lock [lock|unlock]");
        }
    }

    @Override
    public JButton getGuiButton() {
        JButton button = createButton();

        button.addActionListener(e -> {
            Student student = StudentManager.getFirstSelectedStudent();
            if (student != null) {
                ByteBuffer data = ByteBuffer.allocate(1);
                data.putChar((isLocked? '0' : '1'));
                data.flip();

                try {
                    sendCmd(student, data);
                } catch (Exception ex) {
                    log.error("Failed to execute command", ex);
                }
            }
        });

        return button;
    }

    @Override
    public boolean isSupportMultiSelection() {
        return false;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return true;
    }
}
