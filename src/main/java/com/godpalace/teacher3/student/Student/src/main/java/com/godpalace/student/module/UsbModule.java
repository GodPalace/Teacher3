package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;

@Slf4j
public class UsbModule implements Module {
    private static final short DISABLE = 0x01;
    private static final short ENABLE  = DISABLE + 1;

    private static native int Disable();
    private static native int Enable();

    public UsbModule() {
        Runtime.getRuntime().addShutdownHook(new Thread(UsbModule::Enable));
    }

    @Override
    public short getID() {
        return 0x09;
    }

    @Override
    public String getName() {
        return "UsbModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) throws Exception {
        switch (data.getShort()) {
            case DISABLE -> log.debug("Disable usb: {}", Disable());
            case ENABLE  -> log.debug("Enable usb: {}", Enable());
        }
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
