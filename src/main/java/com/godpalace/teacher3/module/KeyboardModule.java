package com.godpalace.teacher3.module;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPInputStream;

@Slf4j
public class KeyboardModule implements Module {
    private static final short GET_KEYBOARD_RECORD = 0x01;
    private static final short DISABLE_KEYBOARD    = GET_KEYBOARD_RECORD + 1;
    private static final short ENABLE_KEYBOARD     = DISABLE_KEYBOARD + 1;

    @Override
    public short getID() {
        return 0x06;
    }

    @Override
    public String getName() {
        return "键盘管理模块";
    }

    @Override
    public String getTooltip() {
        return "管理学生的键盘";
    }

    @Override
    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public JButton getGuiButton() {
        return createButton();
    }

    @Override
    public String getCommand() {
        return "keyboard";
    }

    private static void printHelp() {
        System.out.println("""
                    键盘管理模块命令格式: keyboard [option]
                    
                    option:
                      help - 显示此帮助信息
                    
                      record - 获取学生的键盘记录
                      disable - 禁用学生的键盘
                      enable - 启用学生的键盘""");
    }

    @Override
    public void cmd(String[] args) throws IOException {
        if (args.length < 1) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "record" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: keyboard record");
                    return;
                }

                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                ByteBuffer buffer = ByteBuffer.allocate(2);
                buffer.putShort(GET_KEYBOARD_RECORD);
                sendRequest(student, buffer);

                int count = 0;

                // 处理键盘记录
                buffer = ByteBuffer.allocate(4);
                while (student.getChannel().read(buffer) != 4) {
                    try {
                        synchronized (this) {
                            wait(1000);
                        }

                        count++;
                        if (count > 5) {
                            System.out.println("获取键盘记录超时");
                            return;
                        }
                    } catch (InterruptedException e) {
                        log.error("线程中断", e);
                    }
                }
                buffer.flip();
                int size = buffer.getInt();

                buffer = ByteBuffer.allocate(4);
                student.getChannel().read(buffer);
                buffer.flip();
                int len = buffer.getInt();

                buffer = ByteBuffer.allocate(size - 4);
                student.getChannel().read(buffer);
                buffer.flip();

                ByteArrayInputStream in = new ByteArrayInputStream(buffer.array());
                GZIPInputStream gzipIn = new GZIPInputStream(in);
                ObjectInputStream objIn = new ObjectInputStream(gzipIn);

                KeyboardData lastData = null;
                boolean isEnter = false;

                System.out.println("键盘记录如下(共" + len + "条记录):");
                for (int i = 0; i < len; i++) {
                    KeyboardData data = KeyboardData.readFromStream(objIn);
                    String key = NativeKeyEvent.getKeyText(data.key);

                    if (key.equals("空格")) key = " ";
                    if (key.equals("Backspace")) key = "退格";
                    if (key.equals("Enter")) key = "\n";

                    if (isEnter || lastData == null || Math.abs(data.date.getTime() - lastData.date.getTime()) > 2000) {

                        String time = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ")
                                .format(data.date);
                        System.out.print("\n" + time + key);
                    } else {
                        System.out.print(key);
                    }

                    isEnter = key.equals("\n");
                    lastData = data;
                }
            }

            case "disable" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: keyboard disable");
                    return;
                }

                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                ByteBuffer buffer = ByteBuffer.allocate(2);
                buffer.putShort(DISABLE_KEYBOARD);
                buffer.flip();
                sendRequest(student, buffer);

                System.out.println("禁用键盘指令已发送");
            }

            case "enable" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: keyboard enable");
                    return;
                }

                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                ByteBuffer buffer = ByteBuffer.allocate(2);
                buffer.putShort(ENABLE_KEYBOARD);
                buffer.flip();
                sendRequest(student, buffer);

                System.out.println("启用键盘指令已发送");
            }

            default -> printHelp();
        }
    }

    @Override
    public boolean isSupportMultiSelection() {
        return false;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return true;
    }

    record KeyboardData(Date date, int key) {
        public static KeyboardData readFromStream(ObjectInputStream in) throws IOException {
            try {
                return new KeyboardData((Date) in.readObject(), in.readInt());
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }
}
