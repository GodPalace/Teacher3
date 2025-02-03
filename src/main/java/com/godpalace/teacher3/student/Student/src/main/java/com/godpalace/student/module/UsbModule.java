package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

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
    public ByteBuf execute(Teacher teacher, ByteBuf data) throws Exception {
        switch (data.readShort()) {
            case DISABLE -> log.debug("Disable usb: {}", Disable());
            case ENABLE  -> log.debug("Enable usb: {}", Enable());
        }

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
