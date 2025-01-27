package com.godpalace.student.module;

import com.godpalace.student.NetworkCore;
import com.godpalace.student.Teacher;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class CloserModule implements Module {
    @Override
    public short getID() {
        return 0x99;
    }

    @Override
    public String getName() {
        return "CloserModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) throws Exception {
        log.info("Closing connection from {}", teacher.getIp());

        ByteBuffer response = ByteBuffer.allocate(2);
        response.putShort((short) 0x00);
        response.flip();

        sendResponse(teacher.getChannel(), response);
        NetworkCore.removeTeacher(teacher);
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
