package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import com.godpalace.student.util.DialogUtil;
import io.netty.buffer.ByteBuf;

import java.awt.*;

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
    public ByteBuf execute(Teacher teacher, ByteBuf data) throws Exception {
        byte[] bytes = new byte[data.readableBytes()];
        data.readBytes(bytes);

        DialogUtil.showMessage(
                new String(bytes),
                Color.BLACK, Color.WHITE,
                new Font("Arial", Font.PLAIN, 16),
                3000);

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
