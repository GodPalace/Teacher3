package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import com.godpalace.student.util.DialogUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class MessageModule implements Module{
    @Override
    public short getID() {
        return 0x03;
    }

    @Override
    public String getName() {
        return "MessageModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) {
        DialogUtil.showMessage(
                new String(data.array(), StandardCharsets.UTF_8),
                Color.BLACK, Color.WHITE,
                new Font("Arial", Font.PLAIN, 16),
                3000);
    }
}
