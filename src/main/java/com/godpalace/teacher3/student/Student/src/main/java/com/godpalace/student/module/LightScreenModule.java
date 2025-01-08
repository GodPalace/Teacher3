package com.godpalace.student.module;

import com.godpalace.student.Teacher;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

public class LightScreenModule implements Module {
    private static final Frame lightFrame = new Frame();

    static {
        BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);

        lightFrame.setUndecorated(true);
        lightFrame.setAlwaysOnTop(true);
        lightFrame.setType(Frame.Type.UTILITY);
        lightFrame.setBackground(Color.WHITE);
        lightFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        lightFrame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                image,
                new Point(0, 0), "invisible cursor"));
    }

    @Override
    public short getID() {
        return 0x03;
    }

    @Override
    public String getName() {
        return "LightScreenModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) throws Exception {
        lightFrame.setVisible(true);
        Thread.sleep(100);
        lightFrame.setVisible(false);
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
