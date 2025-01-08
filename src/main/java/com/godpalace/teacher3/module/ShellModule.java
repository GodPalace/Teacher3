package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ShellModule implements Module {
    @Override
    public short getID() {
        return 0x04;
    }

    @Override
    public String getName() {
        return "远程命令执行模块";
    }

    @Override
    public String getTooltip() {
        return "向学生发送命令，并执行命令";
    }

    @Override
    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public JButton getGuiButton() {
        JButton button = createButton();

        button.addActionListener(e -> {
            JDialog dialog = new JDialog((Frame) null, "远程命令执行模块");
            dialog.setSize(600, 400);
            dialog.setLocationRelativeTo(null);
        });

        return button;
    }

    @Override
    public String getCommand() {
        return "shell";
    }

    @Override
    public void cmd(String[] args) throws IOException {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(args).forEach(arg -> sb.append(arg).append(" "));
        String cmd = sb.toString().trim();

        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) {
            System.out.println("未选择学生");
            return;
        }

        byte[] cmdBytes = cmd.getBytes();
        ByteBuffer sendBuffer = ByteBuffer.allocate(2 + cmdBytes.length);
        sendBuffer.putShort((short) 0);
        sendBuffer.put(cmdBytes);

        sendCmd(student, sendBuffer);
        System.out.println("命令已发送, 正在等待执行结果...");

        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(4);
            while (student.getChannel().read(buffer) != 4) Thread.yield();
            buffer.flip();

            int dataLength = buffer.getInt();

            buffer = ByteBuffer.allocate(dataLength);
            student.getChannel().read(buffer);
            String result = new String(buffer.array(), "GBK");

            if (result.equals("/SHELL_END/")) break;
            if (result.equals("/SHELL_ERR/")) {
                System.out.println("命令执行失败");
                break;
            }

            System.out.println(result);
            buffer.clear();
        }
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
