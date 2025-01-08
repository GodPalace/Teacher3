package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface Module {
    short getID();
    String getName();
    String getTooltip();
    BufferedImage getIcon();

    JButton getGuiButton();
    String getCommand();
    void cmd(String[] args) throws IOException;

    boolean isSupportMultiSelection();
    boolean isExecuteWithStudent();

    default JButton createButton() {
        JButton button = new JButton(getName());
        button.setToolTipText(getTooltip());
        button.setBackground(Color.WHITE);
        button.setBorder(new LineBorder(Color.BLACK, 1));
        return button;
    }

    default void sendCmd(Student student, ByteBuffer data) throws IOException {
        sendCmd(student, data.array());
    }

    default void sendCmd(Student student, byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(6 + bytes.length);
        buffer.putShort(getID());
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        student.getChannel().write(buffer);
    }
}
