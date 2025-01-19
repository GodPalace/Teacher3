package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;

@Slf4j
public class ProtectModule implements Module {
    private static native boolean Protect(int pid);

    public ProtectModule() {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int pid = Integer.parseInt(name.split("@")[0]);
        log.debug("Protecting process with PID: {}", pid);
        log.debug("Call native method Protect() {}", (Protect(pid)? "success" : "failed"));
    }

    @Override
    public short getID() {
        return -0x04;
    }

    @Override
    public String getName() {
        return "ProtectModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) throws Exception {
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
