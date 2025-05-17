package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

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
        return "USB管理";
    }

    @Override
    public String getTooltip() {
        return "管理学生的USB设备";
    }

    @Override
    public Image getStatusImage() {
        return null;
    }

    @Override
    public void onGuiButtonAction() {
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
                ByteBuf request = Unpooled.buffer(2);
                request.writeShort(DISABLE);

                for (Student student : StudentManager.getSelectedStudents()) {
                    student.sendRequest(getID(), request);
                    System.out.println(student.getName() + "禁用USB成功");
                }

                request.release();
            }

            case "enable" -> {
                ByteBuf request = Unpooled.buffer(2);
                request.writeShort(ENABLE);

                for (Student student : StudentManager.getSelectedStudents()) {
                    student.sendRequest(getID(), request);
                    System.out.println(student.getName() + "启用USB成功");
                }

                request.release();
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
}
