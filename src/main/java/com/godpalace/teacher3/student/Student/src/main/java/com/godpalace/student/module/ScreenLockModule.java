package com.godpalace.student.module;

import com.godpalace.student.Teacher;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class ScreenLockModule implements Module {
    protected static final Frame lockFrame = new Frame();

    static {
        lockFrame.setUndecorated(true);
        lockFrame.setAlwaysOnTop(true);
        lockFrame.setType(Frame.Type.UTILITY);
        lockFrame.setBackground(Color.BLACK);
        lockFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        lockFrame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB),
                new Point(0, 0), "invisible cursor"));
    }

    @Override
    public short getID() {
        return 0x01;
    }

    @Override
    public String getName() {
        return "ScreenLockModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer buffer) {
        char c = buffer.getChar();
        lockFrame.setVisible(c == '1');
    }
}
