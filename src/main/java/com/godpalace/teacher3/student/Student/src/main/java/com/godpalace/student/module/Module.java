package com.godpalace.student.module;

import com.godpalace.student.Teacher;

import java.nio.ByteBuffer;

public interface Module {
    short getID();
    String getName();
    void execute(Teacher teacher, ByteBuffer data) throws Exception;
}
