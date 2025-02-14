package com.godpalace.teacher3.module.file;

import com.godpalace.teacher3.Main;
import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import com.godpalace.teacher3.module.Module;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.compression.Lz4FrameDecoder;
import io.netty.handler.codec.compression.Lz4FrameEncoder;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

@Slf4j
public class FileManagerModule implements Module {
    private static final short CHECK_EXIST   = 0x01;
    private static final short GET_FILE_TYPE = CHECK_EXIST + 1;
    private static final short LIST_FILES    = GET_FILE_TYPE + 1;
    private static final short NEW_FILE      = LIST_FILES + 1;
    private static final short NEW_DIRECTORY = NEW_FILE + 1;
    private static final short DELETE_FILE   = NEW_DIRECTORY + 1;
    private static final short RENAME_FILE   = DELETE_FILE + 1;
    private static final short UPLOAD_FILE   = RENAME_FILE + 1;
    private static final short DOWNLOAD_FILE = UPLOAD_FILE + 1;
    private static final short LOCK_FILE     = DOWNLOAD_FILE + 1;
    private static final short UNLOCK_FILE   = LOCK_FILE + 1;
    private static final short GET_FILE_INFO = UNLOCK_FILE + 1;

    private static final short SUCCESS              = 0x01;
    private static final short ERROR_NOT_FOUND_PATH = SUCCESS + 1;
    private static final short ERROR_NOT_FOUND_FILE = ERROR_NOT_FOUND_PATH + 1;
    private static final short ERROR_INVALID_PATH   = ERROR_NOT_FOUND_FILE + 1;
    private static final short ERROR_INVALID_FILE   = ERROR_INVALID_PATH + 1;
    private static final short ERROR_CREATE_FILE    = ERROR_INVALID_FILE + 1;
    private static final short ERROR_DELETE_FILE    = ERROR_CREATE_FILE + 1;
    private static final short ERROR_RENAME_FILE    = ERROR_DELETE_FILE + 1;
    private static final short ERROR_LOCK_FILE      = ERROR_RENAME_FILE + 1;

    @Getter
    private static final HashMap<Student, String> curDirs = new HashMap<>();

    static {
        StudentManager.getStudents().addListener((ListChangeListener<Student>) change -> {
            while (change.next()) {
                if (change.wasAdded()) {
                    for (Student student : change.getAddedSubList()) {
                        curDirs.put(student, File.separator);
                    }
                } else if (change.wasRemoved()) {
                    for (Student student : change.getRemoved()) {
                        curDirs.remove(student);
                    }
                }
            }
        });
    }

    @Override
    public short getID() {
        return 0x05;
    }

    @Override
    public String getName() {
        return "文件管理";
    }

    @Override
    public String getTooltip() {
        return "管理学生的磁盘文件";
    }

    @Override
    public Image getStatusImage() {
        return null;
    }

    @Override
    public Button getGuiButton() {
        return createButton();
    }

    @Override
    public String getCommand() {
        return "file";
    }

    private static void printHelp() {
        System.out.println("""
                    文件管理模块命令格式: file [option]
                    
                    option:
                      help - 显示此帮助信息
                    
                      cd [path] - 切换当前目录
                      dir - 显示当前目录路径
                      list - 列出当前目录文件
                      new-file [name] - 创建新文件
                      new-dir [name] - 创建新目录
                      delete [name] - 删除文件
                      rename [old] [new] - 重命名文件
                      info [name] - 获取文件信息
                    
                      upload [local_file] - 上传文件到当前目录
                      download [remote_file] - 下载文件到本地
                    
                      lock [name] - 锁定文件, 目录, 设备
                      unlock [name] - 解锁文件, 目录, 设备""");
    }

    @Override
    public void cmd(String[] args) throws IOException {
        if (args.length == 0) {
            printHelp();
            return;
        }

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
                if (cd(args[1].trim())) {
                    System.out.println("切换成功");
                } else {
                    System.out.println("切换失败");
                }
            }

            case "dir" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: file dir");
                    return;
                }

                // 显示当前目录路径
                String curDir = curDirs.get(StudentManager.getFirstSelectedStudent());
                System.out.println("当前目录路径: " + curDir);
            }

            case "list" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: file list");
                    return;
                }

                // 列出当前目录文件
                List<RemoteFile> files = list();
                if (files != null) {
                    for (RemoteFile file : files) {
                        System.out.println("[" + file.type() + "] " + file.name());
                    }
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
                if (newFile(args[1].trim())) {
                    System.out.println("创建成功");
                } else {
                    System.out.println("创建失败");
                }
            }

            case "new-dir" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file new-dir [name]");
                    return;
                }

                // 创建新目录
                if (newDir(args[1].trim())) {
                    System.out.println("创建成功");
                } else {
                    System.out.println("创建失败");
                }
            }

            case "delete" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file delete [name]");
                    return;
                }

                // 删除文件
                if (delete(args[1].trim())) {
                    System.out.println("删除成功");
                } else {
                    System.out.println("删除失败");
                }
            }

            case "rename" -> {
                if (args.length != 3) {
                    System.out.println("命令格式错误, 请使用格式: file rename [old] [new]");
                    return;
                }

                // 重命名文件
                if (rename(args[1].trim(), args[2].trim())) {
                    System.out.println("重命名成功");
                } else {
                    System.out.println("重命名失败");
                }
            }

            case "upload" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file upload [local_file]");
                    return;
                }

                // 上传文件到当前目录
                if (upload(args[1].trim())) {
                    System.out.println("上传成功");
                } else {
                    System.out.println("上传失败");
                }
            }

            case "download" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file download [remote_file]");
                    return;
                }

                // 下载文件到本地
                if (download(args[1].trim())) {
                    System.out.println("下载成功");
                } else {
                    System.out.println("下载失败");
                }
            }

            case "lock" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file lock [name]");
                    return;
                }

                // 锁定文件
                if (lock(args[1].trim())) {
                    System.out.println("锁定成功");
                } else {
                    System.out.println("锁定失败");
                }
            }

            case "unlock" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file unlock [name]");
                    return;
                }

                // 解锁文件
                if (unlock(args[1].trim())) {
                    System.out.println("解锁成功");
                } else {
                    System.out.println("解锁失败");
                }
            }

            case "info" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: file info [name]");
                    return;
                }

                // 获取文件信息
                try {
                    File file = getFileInfo(args[1].trim());

                    long size = file.length();
                    String sizeStr = size < 1024 ? size + " B" : size < 1024 * 1024 ? size / 1024 + " KB" : size / (1024 * 1024) + " MB";

                    System.out.println("文件名称: " + file.getName());
                    System.out.println("文件大小: " + sizeStr);
                    System.out.println("更改时间: " + new SimpleDateFormat("yyyy年MM月dd日HH点mm分ss秒s毫秒").format(new Date(file.lastModified())));
                    System.out.println("是否可读: " + file.canRead());
                    System.out.println("是否可写: " + file.canWrite());
                    System.out.println("是否隐藏: " + file.isHidden());
                    System.out.println("绝对路径: " + file.getAbsolutePath());
                } catch (Exception e) {
                    System.out.println("获取文件信息失败: " + e.getMessage());
                }
            }

            default -> printHelp();
        }
    }

    public RemoteFileType getFileType(String arg) throws FileNotFoundException {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return null;
        String curDir = curDirs.get(student);

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

        String path = (arg.contains(":")? arg : curDir + arg);
        if (!path.endsWith(File.separator)) path += File.separator;

        byte[] bytes = path.getBytes();

        // 发送请求
        ByteBuf request = Unpooled.buffer(2 + bytes.length);
        request.writeShort(GET_FILE_TYPE);
        request.writeBytes(bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return null;

        switch (response.readShort()) {
            case SUCCESS -> {
                RemoteFileType type = RemoteFileType.getRemoteFileType(response.readInt());
                response.release();
                return type;
            }

            case ERROR_NOT_FOUND_FILE -> {
                response.release();
                throw new FileNotFoundException("未找到" + path);
            }

            default -> {
                response.release();
                return null;
            }
        }
    }

    public boolean cd(String arg) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);
        if (arg.equals("..")) {
            try {
                if (curDir.length() == 3) {
                    curDirs.put(student, File.separator);
                    return true;
                }

                curDir = curDir.substring(0, curDir
                        .substring(0, curDir.length() - 1)
                        .lastIndexOf(File.separator));

                arg = "";
            } catch (StringIndexOutOfBoundsException e) {
                return false;
            }
        }

        String path = (arg.contains(":")? arg : curDir + arg);
        if (!path.endsWith(File.separator)) path += File.separator;

        byte[] bytes = path.getBytes();

        // 发送请求
        try {
            RemoteFileType type = getFileType(path);
            if (type == null) return false;

            if (type == RemoteFileType.DIRECTORY || type == RemoteFileType.ROOT) {
                curDirs.put(student, path);
                return true;
            } else {
                return false;
            }
        } catch (FileNotFoundException e) {
            return false;
        }
    }

    public List<RemoteFile> list() throws IOException {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return null;
        String curDir = curDirs.get(student);

        byte[] bytes = curDir.getBytes();

        // 发送请求
        ByteBuf request = Unpooled.buffer(2 + bytes.length);
        request.writeShort(LIST_FILES);
        request.writeBytes(bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return null;

        // 处理响应
        ArrayList<RemoteFile> files = new ArrayList<>();
        if (response.readShort() == SUCCESS) {
            int count = response.readInt();
            bytes = new byte[response.readableBytes()];
            response.readBytes(bytes);

            // 处理文件列表
            GZIPInputStream gzipIn = new GZIPInputStream(new ByteArrayInputStream(bytes));
            ObjectInputStream objIn = new ObjectInputStream(gzipIn);

            for (int i = 0; i < count; i++) {
                RemoteFile file = RemoteFile.readRemoteFileFromStream(objIn);
                files.add(file);
            }

            objIn.close();
            gzipIn.close();
        } else {
            return null;
        }

        files.sort((o1, o2) -> {
            if (o1.type() == RemoteFileType.DIRECTORY && o2.type() == RemoteFileType.FILE) {
                return -1;
            }

            if (o1.type() == RemoteFileType.FILE && o2.type() == RemoteFileType.DIRECTORY) {
                return 1;
            }

            return o1.name().compareTo(o2.name());
        });

        response.release();
        return files;
    }

    public boolean newFile(String arg) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

        String path = (arg.contains(":")? arg : curDir + arg);
        byte[] bytes = path.getBytes();

        // 发送请求
        ByteBuf request = Unpooled.buffer(2 + bytes.length);
        request.writeShort(NEW_FILE);
        request.writeBytes(bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return false;

        // 处理响应
        boolean success = response.readShort() == SUCCESS;
        response.release();
        return success;
    }

    public boolean newDir(String arg) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

        String path = (arg.contains(":")? arg : curDir + arg);
        byte[] bytes = path.getBytes();

        // 发送请求
        ByteBuf request = Unpooled.buffer(2 + bytes.length);
        request.writeShort(NEW_DIRECTORY);
        request.writeBytes(bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return false;

        // 处理响应
        boolean success = response.readShort() == SUCCESS;
        response.release();
        return success;
    }

    public boolean delete(String arg) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

        String path = (arg.contains(":")? arg : curDir + arg);
        byte[] bytes = path.getBytes();

        // 发送请求
        ByteBuf request = Unpooled.buffer(2 + bytes.length);
        request.writeShort(DELETE_FILE);
        request.writeBytes(bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return false;

        // 处理响应
        boolean success = response.readShort() == SUCCESS;
        response.release();
        return success;
    }

    public boolean rename(String old, String new_) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);

        if (old.startsWith(File.separator)) old = old.substring(1);
        if (old.endsWith(File.separator)) old = old.substring(0, old.length() - 1);

        if (new_.startsWith(File.separator)) new_ = new_.substring(1);
        if (new_.endsWith(File.separator)) new_ = new_.substring(0, new_.length() - 1);

        String old_path = (old.contains(":")? old : curDir + old);
        String new_path = (new_.contains(":")? new_ : curDir + new_);
        byte[] old_bytes = old_path.getBytes();
        byte[] new_bytes = new_path.getBytes();

        ByteBuf request = Unpooled.buffer(10 + old_bytes.length + new_bytes.length);
        request.writeShort(RENAME_FILE);
        request.writeInt(old_bytes.length);
        request.writeBytes(old_bytes);
        request.writeInt(new_bytes.length);
        request.writeBytes(new_bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return false;

        // 处理响应
        boolean success = response.readShort() == SUCCESS;
        response.release();
        return success;
    }

    public boolean upload(String arg) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);
        InetSocketAddress sip = (InetSocketAddress) student.getChannel().localAddress();

        // 获取本地文件
        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);
        String path = (arg.contains(":")? arg : curDir + arg);

        File localFile = new File(path);
        if (!localFile.exists()) {
            return false;
        }

        // 上传文件到当前目录
        Object lock = new Object();
        Random portRandom = new Random();

        int port = portRandom.nextInt(1000) + 37000;
        while (true) {
            try {
                ServerSocket socket = new ServerSocket(port, 1, sip.getAddress());
                socket.close();

                EventLoopGroup group = new NioEventLoopGroup(1);
                EventLoopGroup workerGroup = new NioEventLoopGroup(1);
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(group, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();

                                pipeline.addLast(new Lz4FrameEncoder());
                                pipeline.addLast(new UploadFileHandler(localFile, lock));
                            }
                        });
                ChannelFuture future = bootstrap.bind(new InetSocketAddress(sip.getAddress(), port))
                        .syncUninterruptibly();

                byte[] fileName = (curDir + localFile.getName()).getBytes();
                int nameLength = fileName.length;

                // 发送请求
                ByteBuf request = Unpooled.buffer(10 + nameLength);
                request.writeShort(UPLOAD_FILE);
                request.writeInt(nameLength);
                request.writeBytes(fileName);
                request.writeInt(port);
                student.sendRequest(getID(), request);
                request.release();

                try {
                    synchronized (lock) {
                        lock.wait();
                    }

                    return true;
                } catch (InterruptedException e) {
                    return false;
                } finally {
                    future.channel().close().syncUninterruptibly();

                    group.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            } catch (BindException e) {
                port = portRandom.nextInt(1000) + 37000;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public boolean download(String arg) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);
        InetSocketAddress sip = (InetSocketAddress) student.getChannel().localAddress();

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

        String path = (arg.contains(":") ? arg : curDir + arg);
        File localFile = new File(path.substring(
                path.lastIndexOf(File.separator) + 1));

        // 发送请求
        Object lock = new Object();
        Random portRandom = new Random();

        int port = portRandom.nextInt(1000) + 37000;
        while (true) {
            try {
                ServerSocket socket = new ServerSocket(port, 1, sip.getAddress());
                socket.close();

                EventLoopGroup group = new NioEventLoopGroup(1);
                EventLoopGroup workerGroup = new NioEventLoopGroup(1);
                ServerBootstrap bootstrap = new ServerBootstrap();
                bootstrap.group(group, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) {
                                ChannelPipeline pipeline = ch.pipeline();

                                pipeline.addLast(new Lz4FrameDecoder());
                                pipeline.addLast(new DownloadFileHandler(localFile, lock));
                            }
                        });
                ChannelFuture future = bootstrap.bind(new InetSocketAddress(sip.getAddress(), port))
                        .syncUninterruptibly();

                byte[] filePath = path.getBytes();
                int pathLength = filePath.length;

                // 发送请求
                ByteBuf request = Unpooled.buffer(10 + pathLength);
                request.writeShort(DOWNLOAD_FILE);
                request.writeInt(pathLength);
                request.writeBytes(filePath);
                request.writeInt(port);
                student.sendRequest(getID(), request);
                request.release();

                try {
                    synchronized (lock) {
                        lock.wait();
                    }

                    return true;
                } catch (InterruptedException e) {
                    return false;
                } finally {
                    future.channel().close().syncUninterruptibly();

                    group.shutdownGracefully();
                    workerGroup.shutdownGracefully();
                }
            } catch (BindException e) {
                port = portRandom.nextInt(1000) + 37000;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public boolean lock(String arg) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

        String path = (arg.contains(":") ? arg : curDir + arg);
        File localFile = new File(path.substring(
                path.lastIndexOf(File.separator) + 1));
        byte[] bytes = path.getBytes();

        // 发送请求
        ByteBuf request = Unpooled.buffer(2 + bytes.length);
        request.writeShort(LOCK_FILE);
        request.writeBytes(bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return false;

        // 处理响应
        boolean success = response.readShort() == SUCCESS;
        response.release();
        return success;
    }

    public boolean unlock(String arg) {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return false;
        String curDir = curDirs.get(student);

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

        String path = (arg.contains(":") ? arg : curDir + arg);
        File localFile = new File(path.substring(
                path.lastIndexOf(File.separator) + 1));
        byte[] bytes = path.getBytes();

        // 发送请求
        ByteBuf request = Unpooled.buffer(2 + bytes.length);
        request.writeShort(UNLOCK_FILE);
        request.writeBytes(bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return false;

        // 处理响应
        boolean success = response.readShort() == SUCCESS;
        response.release();
        return success;
    }

    public File getFileInfo(String arg) throws IOException, ClassNotFoundException {
        Student student = StudentManager.getFirstSelectedStudent();
        if (student == null) return null;
        String curDir = curDirs.get(student);

        if (arg.startsWith(File.separator)) arg = arg.substring(1);
        if (arg.endsWith(File.separator)) arg = arg.substring(0, arg.length() - 1);

        String path = (arg.contains(":") ? arg : curDir + arg);
        byte[] bytes = path.getBytes();

        // 发送请求
        ByteBuf request = Unpooled.buffer(2 + bytes.length);
        request.writeShort(GET_FILE_INFO);
        request.writeBytes(bytes);
        short timestamp = student.sendRequest(getID(), request);
        request.release();

        ByteBuf response = readResponse(student, timestamp);
        if (response == null) return null;

        // 处理响应
        File file = null;
        if (response.readShort() == SUCCESS) {
            byte[] data = new byte[response.readableBytes()];
            response.readBytes(data);

            ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
            ObjectInputStream objIn = new ObjectInputStream(byteIn);

            file = (File) objIn.readObject();

            objIn.close();
            byteIn.close();
        }

        return file;
    }

    static class DownloadFileHandler extends ChannelInboundHandlerAdapter {
        private final File outFile;
        private final Object lock;
        private FileChannel outChannel;

        public DownloadFileHandler(File outFile, Object lock) {
            this.outFile = outFile;
            this.lock = lock;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            outChannel = FileChannel.open(outFile.toPath(),
                    StandardOpenOption.WRITE, StandardOpenOption.CREATE);
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ByteBuf buf) {
                outChannel.write(buf.nioBuffer());

                if (Main.isRunOnCmd()) {
                    System.out.print(".");
                }
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            outChannel.close();

            synchronized (lock) {
                lock.notifyAll();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught in DownloadFileHandler", cause);
            ctx.close();
        }
    }

    static class UploadFileHandler extends ChannelInboundHandlerAdapter {
        private final File inFile;
        private final Object lock;

        public UploadFileHandler(File inFile, Object lock) {
            this.inFile = inFile;
            this.lock = lock;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            try {
                FileChannel inChannel = FileChannel.open(inFile.toPath(), StandardOpenOption.READ);
                ByteBuffer buffer = ByteBuffer.allocate(10240);

                while (inChannel.read(buffer) != -1) {
                    buffer.flip();
                    ctx.writeAndFlush(Unpooled.wrappedBuffer(buffer));
                    buffer.clear();

                    if (Main.isRunOnCmd()) {
                        System.out.print(".");
                    }
                }

                inChannel.close();
            } catch (IOException e) {
                log.error("Exception caught in UploadFileHandler", e);
            } finally {
                ctx.close();

                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught in UploadFileHandler", cause);
            ctx.close();
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
