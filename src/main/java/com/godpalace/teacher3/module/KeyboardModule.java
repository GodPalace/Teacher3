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
import java.util.LinkedList;
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
                System.out.println("正在获取键盘记录, 请稍候...");

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

                System.out.println("键盘记录如下(共" + len + "条记录):");
                for (int i = 0; i < len; i++) {
                    KeyboardData data = KeyboardData.readFromStream(objIn);
                    LinkedList<Integer> keys = data.keys;

                    System.out.print(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ")
                            .format(data.date));

                    for (int key : keys) {
                        String text = NativeKeyEvent.getKeyText(key);

                        text = text.replace("Enter", "\n");
                        text = text.replace("Tab", "\t");
                        text = text.replace("空格", " ");
                        text = text.replace("Space", " ");
                        text = text.replace("Backspace", "退格");

                        System.out.print(text.toLowerCase());
                    }

                    System.out.println();
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

    record KeyboardData(Date date, LinkedList<Integer> keys) {
        public static KeyboardData readFromStream(ObjectInputStream in) throws IOException {
            try {
                return new KeyboardData((Date) in.readObject(), (LinkedList<Integer>) in.readObject());
            } catch (ClassNotFoundException e) {
                throw new IOException(e);
            }
        }
    }
}
