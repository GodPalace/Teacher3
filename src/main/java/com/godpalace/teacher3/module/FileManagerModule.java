package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.StandardOpenOption;
import java.util.Random;
import java.util.zip.GZIPInputStream;

@Slf4j
public class FileManagerModule implements Module {
    private static final short CHECK_EXIST   = 0x01;
    private static final short CHECK_FILE    = CHECK_EXIST + 1;
    private static final short LIST_FILES    = CHECK_FILE + 1;
    private static final short NEW_FILE      = LIST_FILES + 1;
    private static final short NEW_DIRECTORY = NEW_FILE + 1;
    private static final short DELETE_FILE   = NEW_DIRECTORY + 1;
    private static final short RENAME_FILE   = DELETE_FILE + 1;
    private static final short UPLOAD_FILE   = RENAME_FILE + 1;
    private static final short DOWNLOAD_FILE = UPLOAD_FILE + 1;

    private static final short SUCCESS              = 0x01;
    private static final short ERROR_NOT_FOUND_PATH = SUCCESS + 1;
    private static final short ERROR_NOT_FOUND_FILE = ERROR_NOT_FOUND_PATH + 1;
    private static final short ERROR_INVALID_PATH   = ERROR_NOT_FOUND_FILE + 1;
    private static final short ERROR_INVALID_FILE   = ERROR_INVALID_PATH + 1;
    private static final short ERROR_CREATE_FILE    = ERROR_INVALID_FILE + 1;
    private static final short ERROR_DELETE_FILE    = ERROR_CREATE_FILE + 1;
    private static final short ERROR_RENAME_FILE    = ERROR_DELETE_FILE + 1;

    @Getter
    private static String curDir = File.separator;

    @Override
    public short getID() {
        return 0x05;
    }

    @Override
    public String getName() {
        return "文件管理模块";
    }

    @Override
    public String getTooltip() {
        return "管理学生的磁盘文件";
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
        return "file";
    }

    private static void printHelp() {
        System.out.println("""
                    文件管理模块命令格式: student [option]
                    
                    option:
                      help - 显示此帮助信息
                    
                      cd [path] - 切换当前目录
                      dir - 显示当前目录路径
                      list - 列出当前目录文件
                      new-file [name] - 创建新文件
                      new-dir [name] - 创建新目录
                      delete [name] - 删除文件
                      rename [old] [new] - 重命名文件
                    
                      upload [local_file] - 上传文件到当前目录
                      download [remote_file] - 下载文件到本地""");
    }

    @Override
    public void cmd(String[] args) throws IOException {
        if (args.length == 0) {
            printHelp();
            return;
        }

        ByteArrayOutputStream requestBytes = new ByteArrayOutputStream();

        switch (args[0]) {
            case "help" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: file help");
                    return;
                }

                // 显示帮助信息
                printHelp();
            }

            case "cd" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file cd [path]");
                    return;
                }

                // 切换当前目录
                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                String arg = args[1].trim();
                if (arg.startsWith(File.separator)) arg = arg.substring(1);
                if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);
                if (arg.equals("..")) {
                    try {
                        if (curDir.length() == 3) {
                            curDir = File.separator;
                            System.out.println("切换成功");
                            return;
                        }

                        curDir = curDir.substring(0, curDir
                                .substring(0, curDir.length() - 1)
                                .lastIndexOf(File.separator));

                        arg = "";
                    } catch (StringIndexOutOfBoundsException e) {
                        System.out.println("切换失败, 已经在根目录");
                        return;
                    }
                }

                String path = (arg.contains(":")? arg : curDir + arg);
                if (!path.endsWith(File.separator)) path += File.separator;

                byte[] bytes = path.getBytes();

                // 发送请求
                requestBytes.write(ByteBuffer.allocate(2).putShort(CHECK_FILE).array());
                requestBytes.write(ByteBuffer.allocate(4).putInt(bytes.length).array());

                requestBytes.write(bytes);
                requestBytes.flush();
                sendRequest(student, requestBytes.toByteArray());
                requestBytes.close();

                ByteBuffer response = ByteBuffer.allocate(4);
                while (student.getChannel().read(response) != 4) Thread.yield();
                response.flip();
                int size = response.getInt();

                response = ByteBuffer.allocate(size);
                student.getChannel().read(response);
                response.flip();

                // 处理响应
                switch (response.getShort()) {
                    case SUCCESS -> {
                        try {
                            byte is = response.get();

                            if (is == 0) {
                                // 不是文件
                                curDir = path;
                                System.out.println("切换成功");
                            } else {
                                // 是文件
                                System.out.println("切换失败, 指向的是文件");
                            }
                        } catch (Exception e) {
                            System.out.println("切换失败: " + e.getMessage());
                        }
                    }

                    case ERROR_NOT_FOUND_FILE -> System.out.println("路径不存在");

                    default -> System.out.println("切换失败");
                }
            }

            case "dir" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: file dir");
                    return;
                }

                // 显示当前目录路径
                System.out.println("当前目录路径: " + curDir);
            }

            case "list" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: file list");
                    return;
                }

                // 列出当前目录文件
                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                byte[] bytes = curDir.getBytes();

                // 发送请求
                requestBytes.write(ByteBuffer.allocate(2).putShort(LIST_FILES).array());
                requestBytes.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
                requestBytes.write(bytes);

                requestBytes.flush();
                sendRequest(student, requestBytes.toByteArray());
                requestBytes.close();

                ByteBuffer response = ByteBuffer.allocate(4);
                while (student.getChannel().read(response) != 4) Thread.yield();
                response.flip();
                int size = response.getInt();

                response = ByteBuffer.allocate(size);
                student.getChannel().read(response);
                response.flip();

                // 处理响应
                if (response.getShort() == SUCCESS) {
                    int count = response.getInt();
                    bytes = new byte[response.remaining()];
                    response.get(bytes);

                    // 处理文件列表
                    GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(bytes));
                    ObjectInputStream objIn = new ObjectInputStream(gzipIn);

                    try {
                        for (int i = 0; i < count; i++) {
                            File file = (File) objIn.readObject();
                            System.out.println("[" + (file.isFile() ? "文件" : "目录") + "] "
                                    + file.getName()
                                    + (file.isFile() ? "  |  大小: " + file.length() + "字节" : "")
                                    + "  |  是否隐藏: " + (file.isHidden()? "是" : "否")
                                    + "  |  上次修改时间: "
                                    + (System.currentTimeMillis() - file.lastModified()) / 1000 + "秒");
                        }
                    } catch (ClassNotFoundException e) {
                        System.out.println("列出失败: " + e.getMessage());
                    }

                    objIn.close();
                    gzipIn.close();
                } else {
                    System.out.println("列出失败");
                }
            }

            case "new-file" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file new-file [name]");
                    return;
                }

                // 创建新文件
                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                String arg = args[1].trim();
                if (arg.startsWith(File.separator)) arg = arg.substring(1);
                if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

                String path = (arg.contains(":")? arg : curDir + arg);
                byte[] bytes = path.getBytes();

                // 发送请求
                requestBytes.write(ByteBuffer.allocate(2).putShort(NEW_FILE).array());
                requestBytes.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
                requestBytes.write(bytes);

                requestBytes.flush();
                sendRequest(student, requestBytes.toByteArray());
                requestBytes.close();

                ByteBuffer response = ByteBuffer.allocate(4);
                while (student.getChannel().read(response) != 4) Thread.yield();
                response.flip();
                int size = response.getInt();

                response = ByteBuffer.allocate(size);
                student.getChannel().read(response);
                response.flip();

                // 处理响应
                switch (response.getShort()) {
                    case SUCCESS -> System.out.println("创建成功");
                    case ERROR_INVALID_FILE -> System.out.println("文件已存在");
                    case ERROR_CREATE_FILE -> System.out.println("创建失败");
                }
            }

            case "new-dir" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file new-dir [name]");
                    return;
                }

                // 创建新目录
                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                String arg = args[1].trim();
                if (arg.startsWith(File.separator)) arg = arg.substring(1);
                if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

                String path = (arg.contains(":")? arg : curDir + arg);
                byte[] bytes = path.getBytes();

                // 发送请求
                requestBytes.write(ByteBuffer.allocate(2).putShort(NEW_DIRECTORY).array());
                requestBytes.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
                requestBytes.write(bytes);

                requestBytes.flush();
                sendRequest(student, requestBytes.toByteArray());
                requestBytes.close();

                ByteBuffer response = ByteBuffer.allocate(4);
                while (student.getChannel().read(response) != 4) Thread.yield();
                response.flip();
                int size = response.getInt();

                response = ByteBuffer.allocate(size);
                student.getChannel().read(response);
                response.flip();

                // 处理响应
                switch (response.getShort()) {
                    case SUCCESS -> System.out.println("创建成功");
                    case ERROR_INVALID_PATH -> System.out.println("路径已存在");
                    case ERROR_CREATE_FILE -> System.out.println("创建失败");
                }
            }

            case "delete" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file delete [name]");
                    return;
                }

                // 删除文件
                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                String arg = args[1].trim();
                if (arg.startsWith(File.separator)) arg = arg.substring(1);
                if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

                String path = (arg.contains(":")? arg : curDir + arg);
                byte[] bytes = path.getBytes();

                // 发送请求
                requestBytes.write(ByteBuffer.allocate(2).putShort(DELETE_FILE).array());
                requestBytes.write(ByteBuffer.allocate(4).putInt(bytes.length).array());
                requestBytes.write(bytes);

                requestBytes.flush();
                sendRequest(student, requestBytes.toByteArray());
                requestBytes.close();

                ByteBuffer response = ByteBuffer.allocate(4);
                while (student.getChannel().read(response) != 4) Thread.yield();
                response.flip();
                int size = response.getInt();

                response = ByteBuffer.allocate(size);
                student.getChannel().read(response);
                response.flip();

                // 处理响应
                switch (response.getShort()) {
                    case SUCCESS -> System.out.println("删除成功");
                    case ERROR_NOT_FOUND_FILE -> System.out.println("文件不存在");
                    case ERROR_DELETE_FILE -> System.out.println("删除失败");
                }
            }

            case "rename" -> {
                if (args.length != 3) {
                    System.out.println("命令格式错误, 请使用格式: file rename [old] [new]");
                    return;
                }

                // 重命名文件
                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;

                String old = args[1].trim();
                if (old.startsWith(File.separator)) old = old.substring(1);
                if (old.endsWith(File.separator)) old = old.substring(0, old.length() - 1);

                String new_ = args[2].trim();
                if (new_.startsWith(File.separator)) new_ = new_.substring(1);
                if (new_.endsWith(File.separator)) new_ = new_.substring(0, new_.length() - 1);

                String old_path = (old.contains(":")? old : curDir + old);
                String new_path = (new_.contains(":")? new_ : curDir + new_);
                byte[] old_bytes = old_path.getBytes();
                byte[] new_bytes = new_path.getBytes();

                requestBytes.write(ByteBuffer.allocate(2).putShort(RENAME_FILE).array());
                requestBytes.write(ByteBuffer.allocate(4).putInt(old_bytes.length).array());
                requestBytes.write(old_bytes);
                requestBytes.write(ByteBuffer.allocate(4).putInt(new_bytes.length).array());
                requestBytes.write(new_bytes);

                requestBytes.flush();
                sendRequest(student, requestBytes.toByteArray());
                requestBytes.close();

                ByteBuffer response = ByteBuffer.allocate(4);
                while (student.getChannel().read(response) != 4) Thread.yield();
                response.flip();
                int size = response.getInt();

                response = ByteBuffer.allocate(size);
                student.getChannel().read(response);
                response.flip();

                // 处理响应
                switch (response.getShort()) {
                    case SUCCESS -> System.out.println("重命名成功");
                    case ERROR_NOT_FOUND_FILE -> System.out.println("文件不存在");
                    case ERROR_INVALID_FILE -> System.out.println("新文件名已存在");
                    case ERROR_RENAME_FILE -> System.out.println("重命名失败");
                }
            }

            case "upload" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file upload [local_file]");
                    return;
                }

                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;
                InetSocketAddress sip = (InetSocketAddress) student.getChannel().getLocalAddress();

                // 获取本地文件
                String arg = args[1].trim();
                if (arg.startsWith(File.separator)) arg = arg.substring(1);
                if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);
                String path = (arg.contains(":")? arg : curDir + arg);

                File localFile = new File(path);
                if (!localFile.exists()) {
                    System.out.println("本地文件不存在");
                    return;
                }
                byte[] fileName = localFile.getName().getBytes();
                int nameLength = fileName.length;

                // 上传文件到当前目录
                Random portRandom = new Random();
                int port = portRandom.nextInt(1000) + 37000;
                while (true) {
                    try (ServerSocketChannel channel = ServerSocketChannel.open()) {
                        channel.bind(new InetSocketAddress(sip.getAddress(), port));

                        // 发送请求
                        requestBytes.write(ByteBuffer.allocate(2)
                                .putShort(UPLOAD_FILE).array());
                        requestBytes.write(ByteBuffer.allocate(4)
                                .putInt(nameLength).array());
                        requestBytes.write(fileName);
                        requestBytes.write(ByteBuffer.allocate(4)
                                .putInt(port).array());

                        requestBytes.flush();
                        sendRequest(student, requestBytes.toByteArray());
                        requestBytes.close();

                        // 等待客户端连接
                        SocketChannel clientChannel = channel.accept();

                        // 发送文件
                        FileChannel localChannel = FileChannel.open(localFile.toPath(),
                                StandardOpenOption.READ);
                        ByteBuffer buffer = ByteBuffer.allocate(1024);

                        long startTime = System.currentTimeMillis();
                        System.out.println("开始上传...");
                        while (localChannel.read(buffer) != -1) {
                            buffer.flip();
                            clientChannel.write(buffer);
                            buffer.clear();
                        }
                        System.out.println("上传成功! 耗时: "
                                + (System.currentTimeMillis() - startTime) / 1000 + "秒");

                        clientChannel.close();
                        localChannel.close();
                        break;
                    } catch (BindException e) {
                        port = portRandom.nextInt(1000) + 37000;
                    } catch (IOException e) {
                        System.out.println("上传失败: " + e.getMessage());
                        break;
                    }
                }
            }

            case "download" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file download [remote_file]");
                    return;
                }

                // 下载文件到本地
                Student student = StudentManager.getFirstSelectedStudent();
                if (student == null) return;
                InetSocketAddress sip = (InetSocketAddress) student.getChannel().getLocalAddress();

                String arg = args[1].trim();
                if (arg.startsWith(File.separator)) arg = arg.substring(1);
                if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

                String path = (arg.contains(":") ? arg : curDir + arg);
                File localFile = new File(path.substring(
                        path.lastIndexOf(File.separator) + 1));
                byte[] bytes = path.getBytes();

                // 发送请求
                Random portRandom = new Random();
                int port = portRandom.nextInt(1000) + 37000;
                while (true) {
                    try (ServerSocketChannel channel = ServerSocketChannel.open()) {
                        channel.bind(new InetSocketAddress(sip.getAddress(), port));

                        // 发送请求
                        requestBytes.write(ByteBuffer.allocate(2)
                                .putShort(DOWNLOAD_FILE).array());
                        requestBytes.write(ByteBuffer.allocate(4)
                                .putInt(bytes.length).array());
                        requestBytes.write(bytes);
                        requestBytes.write(ByteBuffer.allocate(4)
                                .putInt(port).array());

                        requestBytes.flush();
                        sendRequest(student, requestBytes.toByteArray());
                        requestBytes.close();

                        // 等待客户端连接
                        SocketChannel clientChannel = channel.accept();

                        // 接收文件
                        FileChannel localChannel = FileChannel.open(localFile.toPath(),
                                StandardOpenOption.WRITE, StandardOpenOption.CREATE);
                        ByteBuffer buffer = ByteBuffer.allocate(1024);

                        long startTime = System.currentTimeMillis();
                        System.out.println("开始下载...");
                        while (clientChannel.read(buffer) != -1) {
                            buffer.flip();
                            localChannel.write(buffer);
                            buffer.clear();
                        }
                        System.out.println("下载成功! 耗时: "
                                + (System.currentTimeMillis() - startTime) / 1000 + "秒");

                        clientChannel.close();
                        localChannel.close();
                        break;
                    } catch (BindException e) {
                        port = portRandom.nextInt(1000) + 37000;
                    } catch (IOException e) {
                        System.out.println("下载失败: " + e.getMessage());
                        break;
                    }
                }
            }

            default -> printHelp();
        }

        requestBytes.close();
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
