package com.godpalace.teacher3.module;

import com.godpalace.teacher3.NetworkListener;
import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/*
 * 数据文件格式:
 * 4字节: IP长度
 * IP长度字节: IP地址
 * 2字节: 端口号
 */

@Slf4j
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
        return "学生管理";
    }

    @Override
    public Image getStatusImage() {
        return null;
    }

    @Override
    public Button getGuiButton() {
        return null;
    }

    @Override
    public String getCommand() {
        return "student";
    }

    private static void printHelp() {
        System.out.println("""
                    学生管理模块命令格式: student [option]
                    
                    option:
                      help - 显示此帮助信息
                    
                      build - 构建学生端程序
                      build [listener_id] - 构建学生端程序(启用反向连接)
                      build [ip] [port] - 构建学生端程序(启用反向连接)
                    
                      scan - 扫描学生端
                      connect [ip] - 连接到学生端
                      disconnect [student_id] - 断开学生端连接
                    
                      select [student_id] - 选择学生
                      deselect [student_id] - 取消选择学生
                      select-all - 选择所有学生
                      deselect-all - 清除所有学生的选择
                      list - 列出所有学生的状态
                      selected-list - 列出已选择的学生""");
    }

    @Override
    public void cmd(String[] args) throws IOException {
        if (args.length < 1) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "help" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student help");
                    return;
                }

                // 显示帮助信息
                printHelp();
            }

            case "build" -> {
                if (args.length > 3) {
                    System.out.println("命令格式错误, 请使用格式: student build");
                }

                String listenerIp = "";
                short listenerPort = 0;

                if (args.length == 3) {
                    String ip = args[1];
                    int port;

                    try {
                        port = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        System.out.println("命令格式错误, 请使用格式: student build [ip] [port]");
                        return;
                    }

                    // 启用反向连接
                    listenerIp = ip;
                    listenerPort = (short) port;
                } else if (args.length == 2) {
                    int listenerId;

                    try {
                        listenerId = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("命令格式错误, 请使用格式: student build [listener_id]");
                        return;
                    }

                    // 启用反向连接
                    NetworkListener listener = NetworkListener.getListeners().get(listenerId);
                    listenerIp = listener.getAddress().getAddress().getHostAddress();
                    listenerPort = (short) listener.getAddress().getPort();
                }

                System.out.println("正在构建学生端程序...");
                StudentManager.build(new InetSocketAddress(listenerIp, listenerPort));
                System.out.println("构建成功!");
            }

            case "scan" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student scan");
                    return;
                }

                // 扫描学生端
                if (StudentManager.scan()) {
                    System.out.println("扫描成功!");
                } else {
                    System.out.println("扫描失败!");
                }
            }

            case "connect" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: student connect [ip]");
                    return;
                }

                // 连接到学生端
                try {
                    Student student = StudentManager.connect(args[1]);
                    if (student != null) {
                        System.out.println("已连接到学生端: " + student.getIp()
                                + " (ID: " + student.getId() + ")");
                    }
                } catch (Exception e) {
                    System.out.println("连接失败: " + e.getMessage());
                }
            }

            case "disconnect" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: student disconnect [student_id]");
                    return;
                }

                // 断开学生端连接
                try {
                    Student student = StudentManager.getStudent(Integer.parseInt(args[1]));
                    if (student == null) {
                        System.out.println("未找到学生: " + args[1]);
                        return;
                    }

                    StudentManager.disconnect(student);
                    System.out.println("已断开学生端连接: " + student.getName()
                            + " (ID: " + student.getId() + ")");
                } catch (NumberFormatException e) {
                    System.out.println("命令格式错误, 请使用格式: student disconnect [student_id]");
                } catch (IOException e) {
                    System.out.println("断开连接失败: " + e.getMessage());
                }
            }

            case "select" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: student select [student_id]");
                    return;
                }

                // 选择学生
                try {
                    if (StudentManager.selectStudent(Integer.parseInt(args[1]))) {
                        System.out.println("已选择学生: " + args[1]);
                    } else {
                        System.out.println("未找到学生: " + args[1]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("命令格式错误, 请使用格式: student select [student_id]");
                }
            }

            case "deselect" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: student deselect [student_id]");
                    return;
                }

                // 取消选择学生
                try {
                    if (StudentManager.deselectStudent(Integer.parseInt(args[1]))) {
                        System.out.println("已取消选择学生: " + args[1]);
                    } else {
                        System.out.println("未选择该学生: " + args[1]);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("命令格式错误, 请使用格式: student deselect [student_id]");
                }
            }

            case "select-all" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student select-all");
                    return;
                }

                // 选择所有学生
                StudentManager.selectAllStudents();
                System.out.println("已选择所有学生");
            }

            case "deselect-all" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student deselect-all");
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
                    System.out.println("主机名: "+ student.getName()
                            + " ID: " + student.getId());
                }
            }

            case "selected-list" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student selected-list");
                    return;
                }

                // 列出选择的学生
                System.out.println("已选择的学生:");
                for (Student student : StudentManager.getSelectedStudents()) {
                    System.out.println("ID: " + student.getId());
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
        return false;
    }
}
