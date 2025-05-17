package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javafx.scene.image.Image;

import java.io.IOException;

public class MouseModule implements Module {
    private static final short FLY           = 0x01;
    private static final short DISABLE_MOUSE = FLY + 1;
    private static final short ENABLE_MOUSE  = DISABLE_MOUSE + 1;

    @Override
    public short getID() {
        return 0x07;
    }

    @Override
    public String getName() {
        return "鼠标管理";
    }

    @Override
    public String getTooltip() {
        return "管理学生的鼠标";
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
        return "mouse";
    }

    private static void printHelp() {
        System.out.println("""
                    鼠标管理模块命令格式: mouse [option]
                    
                    option:
                      help - 显示此帮助信息
                    
                      fly - 让学生鼠标乱飞
                      disable - 禁用学生鼠标
                      enable - 启用学生鼠标""");
    }

    @Override
    public void cmd(String[] args) throws IOException {
        if (args.length < 1) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "fly" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: mouse fly");
                    return;
                }

                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                ByteBuf request = Unpooled.buffer(2);
                request.writeShort(FLY);
                student.sendRequest(getID(), request);
                request.release();

                System.out.println("鼠标乱飞指令已发送");
            }

            case "disable" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: mouse disable");
                    return;
                }

                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                ByteBuf request = Unpooled.buffer(2);
                request.writeShort(DISABLE_MOUSE);
                student.sendRequest(getID(), request);
                request.release();

                System.out.println("禁用鼠标指令已发送");
            }

            case "enable" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: mouse enable");
                    return;
                }

                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                ByteBuf request = Unpooled.buffer(2);
                request.writeShort(ENABLE_MOUSE);
                student.sendRequest(getID(), request);
                request.release();

                System.out.println("启用鼠标指令已发送");
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
