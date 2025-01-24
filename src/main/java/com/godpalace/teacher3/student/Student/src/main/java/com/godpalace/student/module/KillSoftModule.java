package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Disable
@Slf4j
public class KillSoftModule implements Module {
    private static native void Kill();
    private static native void Unkill();

    @Override
    public short getID() {
        return -0x05;
    }

    @Override
    public String getName() {
        return "KillSoftModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) throws Exception {
        Kill();
        log.info("Kill Soft Module executed");

        Runtime.getRuntime().addShutdownHook(new Thread(KillSoftModule::Unkill));
    }

    @Override
    public boolean isLocalModule() {
        return true;
    }
}
