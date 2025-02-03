package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import io.netty.buffer.ByteBuf;

public interface Module {
    short getID();
    String getName();
    ByteBuf execute(Teacher teacher, ByteBuf data) throws Exception;

    boolean isLocalModule();
}
