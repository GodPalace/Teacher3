package com.godpalace.student.module;

import com.godpalace.student.Teacher;

import java.nio.ByteBuffer;

public class ScreenUnlockModule implements Module {
    @Override
    public short getID() {
        return 0x02;
    }

    @Override
    public String getName() {
        return "ScreenUnlockModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) {
        ScreenLockModule.lockFrame.setVisible(false);
    }
}
