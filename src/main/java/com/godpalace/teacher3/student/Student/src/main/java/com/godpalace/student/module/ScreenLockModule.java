package com.godpalace.student.module;

import com.godpalace.student.Teacher;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

public class ScreenLockModule implements Module {
    private static final File file = new File("C:\\Users\\Public\\.godpalace\\student\\lock.ok");
    private static final Frame lockFrame = new Frame();

    static {
        lockFrame.setUndecorated(true);
        lockFrame.setAlwaysOnTop(!file.exists());
        lockFrame.setType(Frame.Type.UTILITY);
        lockFrame.setBackground(Color.BLACK);
        lockFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        lockFrame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB),
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
        short cmd = buffer.getShort();
        lockFrame.setVisible(cmd == 1);
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
