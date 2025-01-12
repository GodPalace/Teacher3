package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class LightScreenModule implements Module {
    @Override
    public short getID() {
        return 0x03;
    }

    @Override
    public String getName() {
        return "闪屏模块";
    }

    @Override
    public String getTooltip() {
        return "使学生屏幕闪烁";
    }

    @Override
    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public JButton getGuiButton() {
        JButton button = createButton();

        button.addActionListener(e -> {
            for (Student student : StudentManager.getSelectedStudents()) {
                try {
                    sendRequest(student, ByteBuffer.allocate(0));
                } catch (IOException ex) {
                    log.error("学生{}闪屏失败", student.getName(), ex);
                }
            }
        });

        return button;
    }

    @Override
    public String getCommand() {
        return "light-screen";
    }

    @Override
    public void cmd(String[] args) {
        if (args.length != 0) {
            System.out.println("闪屏命令无参数!");
            return;
        }

        for (Student student : StudentManager.getSelectedStudents()) {
            try {
                sendRequest(student, ByteBuffer.allocate(0));
            } catch (IOException ex) {
                System.out.println("学生" + student.getName() + "闪屏失败");
            }

            System.out.println("闪屏完成!");
        }
    }

    @Override
    public boolean isSupportMultiSelection() {
        return true;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return true;
    }
}
