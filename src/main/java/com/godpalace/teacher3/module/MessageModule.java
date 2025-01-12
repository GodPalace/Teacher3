package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class MessageModule implements Module {
    @Override
    public short getID() {
        return 0x02;
    }

    @Override
    public String getName() {
        return "消息发送模块";
    }

    @Override
    public String getTooltip() {
        return "发送一条消息给学生, 并在学生屏幕中央显示3秒";
    }

    @Override
    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public String getCommand() {
        return "message";
    }

    @Override
    public void cmd(String[] args) {
        if (args.length != 1) {
            System.out.println("命令格式错误, 请使用格式: message <消息>");
            return;
        }

        byte[] bytes = args[0].trim().getBytes();
        ByteBuffer data = ByteBuffer.allocate(bytes.length);
        data.put(bytes);
        data.flip();

        int count = 0;
        for (Student student : StudentManager.getSelectedStudents()) {
            try {
                sendRequest(student, data);
                count++;
            } catch (IOException ex) {
                log.error("发送消息到学生[{}]失败", student.getName(), ex);
            }
        }

        System.out.println("消息发送成功! 共有" + count + "个学生发送成功.");
    }

    @Override
    public JButton getGuiButton() {
        JButton button = createButton();

        button.addActionListener(e -> {
            String message = JOptionPane.showInputDialog(null,
                    "请输入要发送的消息:", "发送消息",
                    JOptionPane.INFORMATION_MESSAGE);
            if (message == null || message.isEmpty()) return;

            byte[] bytes = message.getBytes();
            ByteBuffer data = ByteBuffer.allocate(bytes.length);
            data.put(bytes);
            data.flip();

            int count = 0;
            for (Student student : StudentManager.getSelectedStudents()) {
                try {
                    sendRequest(student, data);
                    count++;
                } catch (IOException ex) {
                    log.error("发送消息到学生[{}]失败", student.getName(), ex);
                }
            }

            JOptionPane.showMessageDialog(null,
                    "消息发送成功! 共有" + count + "个学生发送成功.");
        });

        return button;
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
