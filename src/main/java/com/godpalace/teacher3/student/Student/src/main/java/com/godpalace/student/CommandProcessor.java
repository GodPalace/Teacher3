package com.godpalace.student;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class CommandProcessor {
    public static final short SCREEN_LOCK   = 0x01;
    public static final short SCREEN_UNLOCK = 0x02;

    private static final Frame lockFrame = new Frame();

    // Initialization lockFrame
    static {
        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);

        lockFrame.setSize(Toolkit.getDefaultToolkit().getScreenSize());
        lockFrame.setUndecorated(true);
        lockFrame.setAlwaysOnTop(true);
        lockFrame.setType(Frame.Type.UTILITY);
        lockFrame.setBackground(Color.BLACK);
        lockFrame.setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                cursorImg, new Point(0, 0), "Custom Cursor"));
    }

    public static void handle(Teacher teacher) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(2);
        teacher.getChannel().read(buffer);
        buffer.flip();
        short cmd = buffer.getShort();

        log.debug("Received command: {}", cmd);

        switch (cmd) {
            case SCREEN_LOCK -> screenLock();
            case SCREEN_UNLOCK -> screenUnlock();

            default -> log.warn("Unknown command: {}", cmd);
        }
    }

    public static void screenLock() {
        lockFrame.setVisible(true);
    }

    public static void screenUnlock() {
        lockFrame.setVisible(false);
    }
}
