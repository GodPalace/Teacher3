package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.nio.file.StandardOpenOption;
import java.util.zip.GZIPOutputStream;

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

    @Override
    public short getID() {
        return 0x05;
    }

    @Override
    public String getName() {
        return "FileManagerModule";
    }

    @Override
    public void execute(Teacher teacher, ByteBuffer data) throws Exception {
        short command = data.getShort();

        // 响应数据
        short response = SUCCESS;
        ByteArrayOutputStream responseBytes = new ByteArrayOutputStream();

        switch (command) {
            case CHECK_EXIST -> {
                int pathLength = data.getInt();
                byte[] pathBytes = new byte[pathLength];
                data.get(pathBytes);
                String path = new String(pathBytes);

                File file = new File(path);
                if (file.exists()) {
                    responseBytes.write(1);
                } else {
                    responseBytes.write(0);
                }
            }

            case CHECK_FILE -> {
                int pathLength = data.getInt();
                byte[] pathBytes = new byte[pathLength];
                data.get(pathBytes);
                String path = new String(pathBytes);

                File file = new File(path);
                if (file.exists()) {
                    if (file.isFile()) {
                        responseBytes.write(1);
                    } else {
                        responseBytes.write(0);
                    }
                } else {
                    response = ERROR_NOT_FOUND_FILE;
                    log.info("Not found file: {}", path);
                }
            }

            case LIST_FILES -> {
                int pathLength = data.getInt();
                byte[] pathBytes = new byte[pathLength];
                data.get(pathBytes);
                String path = new String(pathBytes);

                File file = new File(path);
                if (file.exists()) {
                    if (file.isDirectory()) {
                        File[] files = (path.equals(File.separator)?
                                File.listRoots() : file.listFiles());
                        int count = (files == null ? 0 : files.length);

                        byte[] countBytes = ByteBuffer.allocate(4).putInt(count).array();
                        responseBytes.write(countBytes);

                        GZIPOutputStream gzipOut = new GZIPOutputStream(responseBytes);
                        ObjectOutputStream objOut = new ObjectOutputStream(gzipOut);

                        for (int i = 0; i < count; i++) {
                            objOut.writeObject(files[i]);
                        }

                        objOut.flush();
                        gzipOut.finish();
                        gzipOut.flush();

                        objOut.close();
                        gzipOut.close();
                    } else {
                        response = ERROR_INVALID_PATH;
                        log.info("Not a directory: {}", path);
                    }
                } else {
                    response = ERROR_NOT_FOUND_PATH;
                    log.info("Not found directory: {}", path);
                }
            }

            case NEW_FILE -> {
                int pathLength = data.getInt();
                byte[] pathBytes = new byte[pathLength];
                data.get(pathBytes);
                String path = new String(pathBytes);

                File file = new File(path);
                if (file.exists()) {
                    response = ERROR_INVALID_FILE;
                    log.info("File already exists: {}", path);
                } else {
                    boolean success = file.createNewFile();

                    if (success) {
                        log.info("New file created: {}", path);
                    } else {
                        response = ERROR_CREATE_FILE;
                        log.info("Failed to create file: {}", path);
                    }
                }
            }

            case NEW_DIRECTORY -> {
                int pathLength = data.getInt();
                byte[] pathBytes = new byte[pathLength];
                data.get(pathBytes);
                String path = new String(pathBytes);

                File file = new File(path);
                if (file.exists()) {
                    response = ERROR_INVALID_PATH;
                    log.info("Path already exists: {}", path);
                } else {
                    boolean success = file.mkdirs();

                    if (success) {
                        log.info("New directory created: {}", path);
                    } else {
                        response = ERROR_CREATE_FILE;
                        log.info("Failed to create directory: {}", path);
                    }
                }
            }

            case DELETE_FILE -> {
                int pathLength = data.getInt();
                byte[] pathBytes = new byte[pathLength];
                data.get(pathBytes);
                String path = new String(pathBytes);

                File file = new File(path);
                if (file.exists()) {
                    boolean success = file.delete();

                    if (success) {
                        log.info("File deleted: {}", path);
                    } else {
                        response = ERROR_DELETE_FILE;
                        log.info("Failed to delete file: {}", path);
                    }
                } else {
                    response = ERROR_NOT_FOUND_FILE;
                    log.info("Not found file: {}", path);
                }
            }

            case RENAME_FILE -> {
                int oldPathLength = data.getInt();
                byte[] oldPathBytes = new byte[oldPathLength];
                data.get(oldPathBytes);
                String oldPath = new String(oldPathBytes);

                int newPathLength = data.getInt();
                byte[] newPathBytes = new byte[newPathLength];
                data.get(newPathBytes);
                String newPath = new String(newPathBytes);

                File oldFile = new File(oldPath);
                if (oldFile.exists()) {
                    File newFile = new File(newPath);

                    if (newFile.exists()) {
                        response = ERROR_INVALID_FILE;
                        log.info("File already exists: {}", newPath);
                    } else {
                        boolean success = oldFile.renameTo(newFile);

                        if (success) {
                            log.info("File renamed: {} -> {}", oldPath, newPath);
                        } else {
                            response = ERROR_RENAME_FILE;
                            log.info("Failed to rename file: {} -> {}", oldPath, newPath);
                        }
                    }
                } else {
                    response = ERROR_NOT_FOUND_FILE;
                    log.info("Not found file: {}", oldPath);
                }
            }

            case UPLOAD_FILE -> {
                int pathLength = data.getInt();
                byte[] pathBytes = new byte[pathLength];
                data.get(pathBytes);
                String path = new String(pathBytes);

                File file = new File(path);
                file.createNewFile();

                String ip = teacher.getIp();
                int port = data.getInt();

                Thread.sleep(1000);
                SocketChannel channel = SocketChannel.open(
                        new InetSocketAddress(ip, port));
                FileChannel fileChannel = FileChannel.open(file.toPath(),
                        StandardOpenOption.WRITE);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (channel.read(buffer) != -1) {
                    buffer.flip();
                    fileChannel.write(buffer);
                    buffer.clear();
                }

                fileChannel.close();
                channel.close();
                return;
            }

            case DOWNLOAD_FILE -> {
                int pathLength = data.getInt();
                byte[] pathBytes = new byte[pathLength];
                data.get(pathBytes);
                String path = new String(pathBytes);

                File file = new File(path);
                file.createNewFile();

                String ip = teacher.getIp();
                int port = data.getInt();

                Thread.sleep(1000);
                SocketChannel channel = SocketChannel.open(
                        new InetSocketAddress(ip, port));
                FileChannel fileChannel = FileChannel.open(file.toPath(),
                        StandardOpenOption.READ);

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (fileChannel.read(buffer) != -1) {
                    buffer.flip();
                    channel.write(buffer);
                    buffer.clear();
                }

                fileChannel.close();
                channel.close();
                file.delete();
                return;
            }

            default -> log.info("Unknown command: {}", command);
        }

        // 发送响应数据
        responseBytes.flush();
        ByteBuffer responseData = ByteBuffer.allocate(2 + responseBytes.size());
        responseData.putShort(response);
        responseData.put(responseBytes.toByteArray());
        responseData.flip();
        sendResponseWithSize(teacher.getChannel(), responseData);
        responseBytes.close();
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }
}
