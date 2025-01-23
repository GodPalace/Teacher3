package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class UsbModule implements Module {
    private static final short DISABLE = 0x01;
    private static final short ENABLE  = DISABLE + 1;

    @Override
    public short getID() {
        return 0x09;
    }

    @Override
    public String getName() {
        return "USB管理模块";
    }

    @Override
    public String getTooltip() {
        return "管理学生的USB设备";
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
        return "usb";
    }

    private static void printHelp() {
        System.out.println("""
                    USB管理模块命令格式: file [option]
                    
                    option:
                      help - 显示此帮助信息
                    
                      disable - 禁用USB
                      enable - 启用USB""");
    }

    @Override
    public void cmd(String[] args) throws IOException {
        if (args.length != 1) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "disable" -> {
                ByteBuffer data = ByteBuffer.allocate(2);
                data.putShort(DISABLE);
                data.flip();

                for (Student student : StudentManager.getSelectedStudents()) {
                    sendRequest(student, data);
                    System.out.println(student.getName() + "禁用USB成功");
                }
            }

            case "enable" -> {
                ByteBuffer data = ByteBuffer.allocate(2);
                data.putShort(ENABLE);
                data.flip();

                for (Student student : StudentManager.getSelectedStudents()) {
                    sendRequest(student, data);
                    System.out.println(student.getName() + "启用USB成功");
                }
            }

            default -> printHelp();
        }
    }

    @Override
    public boolean isSupportMultiSelection() {
        return true;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return true;
    }
}
