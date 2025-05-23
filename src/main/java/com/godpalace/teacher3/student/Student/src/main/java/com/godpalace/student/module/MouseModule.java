package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.util.Random;

@Slf4j
public class MouseModule implements Module {
    private static final short FLY           = 0x01;
    private static final short DISABLE_MOUSE = FLY + 1;
    private static final short ENABLE_MOUSE  = DISABLE_MOUSE + 1;

    private static Robot robot;
    private static final Random random = new Random();

    static {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            log.error("MouseModule init failed", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(MouseModule::EnableMouse));
    }

    private static native void DisableMouse();
    private static native void EnableMouse();

    @Override
    public short getID() {
        return 0x07;
    }

    @Override
    public String getName() {
        return "MouseModule";
    }

    @Override
    public ByteBuf execute(Teacher teacher, ByteBuf data) {
        switch (data.readShort()) {
            case FLY -> {
                if (robot != null) {
                    int x = random.nextInt(Toolkit.getDefaultToolkit().getScreenSize().height);
                    int y = random.nextInt(Toolkit.getDefaultToolkit().getScreenSize().width);
                    robot.mouseMove(x, y);
                }
            }

            case DISABLE_MOUSE -> DisableMouse();
            case ENABLE_MOUSE -> EnableMouse();
        }

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
