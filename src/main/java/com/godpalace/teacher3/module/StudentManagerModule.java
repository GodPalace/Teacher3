package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class StudentManagerModule implements Module {
    @Override
    public short getID() {
        return 0x00;
    }

    @Override
    public String getName() {
        return "StudentManagerModule";
    }

    @Override
    public String getTooltip() {
        return "学生管理器";
    }

    @Override
    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public JButton getGuiButton() {
        return null;
    }

    @Override
    public String getCommand() {
        return "student";
    }

    @Override
    public void cmd(String[] args) {
        if (args.length < 1) {
            System.out.println("""
                    命令格式错误, 请使用格式: student [option]
                    
                    option:
                      connect [ip] - 连接到学生端
                    
                      select [student_id] - 选择学生
                      deselect [student_id] - 取消选择学生
                      clear - 清除所有学生的选择
                      list - 列出所有学生的状态
                      selected-list - 列出已选择的学生""");
            return;
        }

        switch (args[0]) {
            case "connect" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: student connect [ip]");
                    return;
                }

                // 连接到学生端
                try {
                    Student student = StudentManager.connect(args[1]);
                    System.out.println("已连接到学生端: " + student.getName()
                            + " (ID: " + student.getId() + ")");
                } catch (IOException e) {
                    System.out.println("连接失败: " + e.getMessage());
                }
            }

            case "select" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: student select [student_id]");
                    return;
                }

                // 选择学生
                if (StudentManager.selectStudent(Integer.parseInt(args[1]))) {
                    System.out.println("已选择学生: " + args[1]);
                } else {
                    System.out.println("未找到学生: " + args[1]);
                }
            }

            case "deselect" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: student deselect [student_id]");
                    return;
                }

                // 取消选择学生
                if (StudentManager.deselectStudent(Integer.parseInt(args[1]))) {
                    System.out.println("已取消选择学生: " + args[1]);
                } else {
                    System.out.println("未找到学生: " + args[1]);
                }
            }

            case "clear" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student clear");
                    return;
                }

                // 清除所有学生的选择
                StudentManager.clearSelectedStudents();
                System.out.println("已清除所有学生的选择");
            }

            case "list" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student list");
                    return;
                }

                // 列出所有学生的状态
                System.out.println("所有学生:");
                for (Student student : StudentManager.getStudents()) {
                    boolean isAlive = student.isAlive();

                    System.out.println("ID: " + student.getId() + " - "
                            + (isAlive ? "在线" : "离线"));

                    if (!isAlive) StudentManager.removeStudent(student);
                }
            }

            case "selected-list" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student list");
                    return;
                }

                // 列出选择的学生
                System.out.println("已选择的学生:");
                for (Student student : StudentManager.getSelectedStudents()) {
                    System.out.println("ID: " + student.getId());
                }
            }

            default -> System.out.println("未知命令");
        }
    }

    @Override
    public boolean isSupportMultiSelection() {
        return false;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return false;
    }
}
