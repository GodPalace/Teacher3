package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import io.netty.buffer.ByteBuf;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

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
    public ByteBuf execute(Teacher teacher, ByteBuf buffer) {
        short cmd = buffer.readShort();
        lockFrame.setVisible(cmd == 1);

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
