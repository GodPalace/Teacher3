package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Main;
import com.godpalace.teacher3.NetworkListener;
import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

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
        return "学生管理模块";
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

    private static void printHelp() {
        System.out.println("""
                    学生管理模块命令格式: student [option]
                    
                    option:
                      help - 显示此帮助信息
                    
                      build - 构建学生端程序
                      build [listener_id] - 构建学生端程序(启用反向连接)
                    
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
                if (args.length > 2) {
                    System.out.println("命令格式错误, 请使用格式: student build");
                }

                int listenerId;
                int listenerIpLength = 0;
                String listenerIp = "";
                short listenerPort = 0;

                if (args.length == 2) {
                    listenerId = Integer.parseInt(args[1]);

                    if (!NetworkListener.getListeners().containsKey(listenerId)) {
                        System.out.println("未找到监听器: " + listenerId);
                        return;
                    }

                    NetworkListener listener = NetworkListener.getListeners().get(listenerId);
                    listenerIp = listener.getAddress().getAddress().getHostAddress();
                    listenerIpLength = listenerIp.length();
                    listenerPort = (short) listener.getAddress().getPort();
                }

                System.out.println("正在构建学生端程序...");

                // 读取资源文件并写入临时文件
                File tempFile = new File(System.getenv("TEMP") + "\\Student.jar");
                File file = new File(System.currentTimeMillis() + "-Student.jar");

                URL url = Main.class.getResource("/Student.jar");
                if (url == null) {
                    System.out.println("未找到资源文件, 无法构建学生端程序");
                    return;
                }

                // 输出到临时文件
                InputStream in = url.openStream();
                FileOutputStream out = new FileOutputStream(tempFile);
                byte[] buffer = new byte[10240];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.close();

                // 写入临时数据
                ZipInputStream zipIn = new ZipInputStream(new FileInputStream(tempFile));
                ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(file));
                ZipEntry entry = zipIn.getNextEntry();

                while (entry != null) {
                    zipOut.putNextEntry(new ZipEntry(entry.getName()));
                    buffer = new byte[10240];

                    while ((len = zipIn.read(buffer)) != -1) {
                        zipOut.write(buffer, 0, len);
                    }

                    zipOut.closeEntry();
                    entry = zipIn.getNextEntry();
                }

                // 写入反向连接配置
                if (args.length == 2) {
                    try {
                        byte[] configData = new byte[listenerIpLength + 3];

                        // IP地址长度
                        configData[0] = (byte) listenerIpLength;

                        // IP地址
                        byte[] ipBytes = listenerIp.getBytes(StandardCharsets.UTF_8);
                        System.arraycopy(ipBytes, 0, configData, 1, ipBytes.length);

                        // 端口号
                        configData[listenerIpLength + 1] = (byte) (listenerPort & 0xFF);
                        configData[listenerIpLength + 2] = (byte) ((listenerPort >> 8) & 0xFF);

                        // 写入配置数据
                        zipOut.putNextEntry(new ZipEntry("ReverseConnectConfig.data"));
                        zipOut.write(configData);
                        zipOut.closeEntry();
                    } catch (NumberFormatException e) {
                        System.out.println("命令格式错误, 请使用格式: student build [listener_id]");
                        return;
                    }
                }

                zipOut.close();
                zipIn.close();
                tempFile.delete();

                System.out.println("构建成功!");
                in.close();
            }

            case "scan" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: student scan");
                    return;
                }

                // 扫描学生端
                for (NetworkInterface anInterface : Main.getAddresses().values()) {
                    InetSocketAddress group = new InetSocketAddress(
                            InetAddress.getByName("224.3.7.1"), Main.SCAN_PORT);

                    try (MulticastSocket socket = new MulticastSocket()) {
                        socket.joinGroup(group, anInterface);

                        byte[] data = new byte[1];
                        data[0] = (byte) 1;

                        DatagramPacket packet = new DatagramPacket(data, data.length, group);
                        socket.send(packet);

                        System.out.println("正在扫描网卡: " + anInterface.getName());
                    } catch (IOException e) {
                        System.out.println("扫描失败: " + e.getMessage());
                    }
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
                    System.out.println("已连接到学生端: " + student.getName()
                            + " (ID: " + student.getId() + ")");
                } catch (IOException e) {
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

                    student.close();
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
                        System.out.println("未找到学生: " + args[1]);
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
                    boolean isAlive = student.isAlive();

                    System.out.println("主机名: "+ student.getName()
                            + " ID: " + student.getId() + " - "
                            + (isAlive ? "在线" : "离线"));

                    if (!isAlive) StudentManager.removeStudent(student);
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
