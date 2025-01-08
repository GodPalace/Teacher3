package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import com.godpalace.student.ThreadPoolManager;
import com.godpalace.student.util.DialogUtil;

import java.awt.*;
import java.nio.ByteBuffer;

public class MessageModule implements Module{
    @Override
    public short getID() {
        return 0x02;
    }

    @Override
    public String getName() {
        return "MessageModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) {
        ThreadPoolManager.getExecutor().execute(() -> DialogUtil.showMessage(
                new String(data.array()),
                Color.BLACK, Color.WHITE,
                new Font("Arial", Font.PLAIN, 16),
                3000));
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
