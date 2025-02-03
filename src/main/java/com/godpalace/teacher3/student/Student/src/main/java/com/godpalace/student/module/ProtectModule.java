package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;

@Slf4j
public class ProtectModule implements Module {
    protected static native int Protect(int pid);
    protected static native int Unprotect();

    @Override
    public short getID() {
        return -0x04;
    }

    @Override
    public String getName() {
        return "ProtectModule";
    }

    @Override
    public ByteBuf execute(Teacher teacher, ByteBuf data) {
        String name = ManagementFactory.getRuntimeMXBean().getName();
        int pid = Integer.parseInt(name.split("@")[0]);
        log.debug("Protecting process with PID: {}", pid);
        log.debug("Call native method Protect() {}", Protect(pid));

        Runtime.getRuntime().addShutdownHook(new Thread(ProtectModule::Unprotect));
        return null;
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
