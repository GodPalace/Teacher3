package com.godpalace.student.module;

import com.godpalace.student.Teacher;
import com.godpalace.student.manager.ThreadPoolManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.Lz4FrameDecoder;
import io.netty.handler.codec.compression.Lz4FrameEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
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
    private static final short LOCK_FILE     = DOWNLOAD_FILE + 1;
    private static final short UNLOCK_FILE   = LOCK_FILE + 1;

    private static final short SUCCESS              = 0x01;
    private static final short ERROR_NOT_FOUND_PATH = SUCCESS + 1;
    private static final short ERROR_NOT_FOUND_FILE = ERROR_NOT_FOUND_PATH + 1;
    private static final short ERROR_INVALID_PATH   = ERROR_NOT_FOUND_FILE + 1;
    private static final short ERROR_INVALID_FILE   = ERROR_INVALID_PATH + 1;
    private static final short ERROR_CREATE_FILE    = ERROR_INVALID_FILE + 1;
    private static final short ERROR_DELETE_FILE    = ERROR_CREATE_FILE + 1;
    private static final short ERROR_RENAME_FILE    = ERROR_DELETE_FILE + 1;
    private static final short ERROR_LOCK_FILE      = ERROR_RENAME_FILE + 1;

    private static native long LockFile(String path);
    private static native boolean UnlockFile(long ptr);

    private static final HashMap<String, Long> ptrs = new HashMap<>();

    @Override
    public short getID() {
        return 0x05;
    }

    @Override
    public String getName() {
        return "FileManagerModule";
    }

    @Override
    public ByteBuf execute(Teacher teacher, ByteBuf data) {
        short command = data.readShort();

        // 响应数据
        short response = SUCCESS;
        ByteBuf responseBytes = Unpooled.buffer();

        try {
            switch (command) {
                case CHECK_EXIST -> {
                    byte[] pathBytes = new byte[data.readableBytes()];
                    data.readBytes(pathBytes);
                    String path = new String(pathBytes);

                    File file = new File(path);
                    responseBytes.writeBoolean(file.exists());
                }

                case CHECK_FILE -> {
                    byte[] pathBytes = new byte[data.readableBytes()];
                    data.readBytes(pathBytes);
                    String path = new String(pathBytes);

                    File file = new File(path);
                    if (file.exists()) {
                        responseBytes.writeBoolean(file.isFile());
                    } else {
                        response = ERROR_NOT_FOUND_FILE;
                        log.info("Not found file: {}", path);
                    }
                }

                case LIST_FILES -> {
                    byte[] pathBytes = new byte[data.readableBytes()];
                    data.readBytes(pathBytes);
                    String path = new String(pathBytes);

                    File file = new File(path);
                    if (file.exists()) {
                        if (file.isDirectory()) {
                            File[] files = (path.equals(File.separator) ?
                                    File.listRoots() : file.listFiles());
                            int count = (files == null ? 0 : files.length);
                            responseBytes.writeInt(count);

                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                            GZIPOutputStream gzipOut = new GZIPOutputStream(byteOut);
                            ObjectOutputStream objOut = new ObjectOutputStream(gzipOut);

                            for (int i = 0; i < count; i++) {
                                objOut.writeObject(files[i]);
                            }

                            objOut.flush();
                            gzipOut.finish();
                            gzipOut.flush();
                            byteOut.flush();

                            responseBytes.writeBytes(byteOut.toByteArray());

                            objOut.close();
                            gzipOut.close();
                            byteOut.close();
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
                    byte[] pathBytes = new byte[data.readableBytes()];
                    data.readBytes(pathBytes);
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
                    byte[] pathBytes = new byte[data.readableBytes()];
                    data.readBytes(pathBytes);
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
                    byte[] pathBytes = new byte[data.readableBytes()];
                    data.readBytes(pathBytes);
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
                    int oldPathLength = data.readInt();
                    byte[] oldPathBytes = new byte[oldPathLength];
                    data.readBytes(oldPathBytes);
                    String oldPath = new String(oldPathBytes);

                    int newPathLength = data.readInt();
                    byte[] newPathBytes = new byte[newPathLength];
                    data.readBytes(newPathBytes);
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
                    int pathLength = data.readInt();
                    byte[] pathBytes = new byte[pathLength];
                    data.readBytes(pathBytes);
                    String path = new String(pathBytes);

                    File file = new File(path);
                    if (file.exists()) file.delete();
                    file.createNewFile();

                    String ip = teacher.getIp();
                    int port = data.readInt();

                    EventLoopGroup group = ThreadPoolManager.getGroup();
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) {
                                    ChannelPipeline pipeline = ch.pipeline();

                                    pipeline.addLast(new Lz4FrameDecoder());
                                    pipeline.addLast(new UploadFileHandler(file));
                                }
                            });
                    bootstrap.connect(ip, port).sync();

                    responseBytes.release();
                    return null;
                }

                case DOWNLOAD_FILE -> {
                    int pathLength = data.readInt();
                    byte[] pathBytes = new byte[pathLength];
                    data.readBytes(pathBytes);
                    String path = new String(pathBytes);

                    File file = new File(path);
                    if (!file.exists()) file.createNewFile();

                    String ip = teacher.getIp();
                    int port = data.readInt();

                    EventLoopGroup group = ThreadPoolManager.getGroup();
                    Bootstrap bootstrap = new Bootstrap();
                    bootstrap.group(group)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) {
                                    ChannelPipeline pipeline = ch.pipeline();

                                    pipeline.addLast(new Lz4FrameEncoder());
                                    pipeline.addLast(new DownloadFileHandler(file));
                                }
                            });
                    bootstrap.connect(ip, port).sync();

                    responseBytes.release();
                    return null;
                }

                case LOCK_FILE -> {
                    byte[] pathBytes = new byte[data.readableBytes()];
                    data.readBytes(pathBytes);
                    String path = new String(pathBytes);

                    if (new File(path).exists()) {
                        long ptr = LockFile(path);

                        if (ptr == 0) {
                            response = ERROR_LOCK_FILE;
                            log.info("Failed to lock file: {}", path);
                        } else {
                            ptrs.put(path, ptr);
                            log.info("File locked: {}", path);
                        }
                    } else {
                        response = ERROR_NOT_FOUND_FILE;
                        log.info("Not found file: {}", path);
                    }
                }

                case UNLOCK_FILE -> {
                    byte[] pathBytes = new byte[data.readableBytes()];
                    data.readBytes(pathBytes);
                    String path = new String(pathBytes);

                    if (ptrs.containsKey(path)) {
                        if (new File(path).exists()) {
                            if (!UnlockFile(ptrs.get(path))) {
                                response = ERROR_LOCK_FILE;
                                log.info("Failed to unlock file: {}", path);
                            } else {
                                ptrs.remove(path);
                                log.info("File unlocked: {}", path);
                            }
                        } else {
                            response = ERROR_NOT_FOUND_FILE;
                            log.info("Not found file: {}", path);
                        }
                    } else {
                        response = ERROR_INVALID_FILE;
                        log.info("File not locked: {}", path);
                    }
                }

                default -> log.info("Unknown command: {}", command);
            }
        } catch (Exception e) {
            log.error("Failed to execute command: {}", command, e);
        }

        // 发送响应数据
        ByteBuf responseData = Unpooled.buffer(2 + responseBytes.readableBytes());
        responseData.writeShort(response);
        responseData.writeBytes(responseBytes);
        responseBytes.release();

        return responseData;
    }

    @Override
    public boolean isLocalModule() {
        return false;
    }

    // 接收上传的文件
    static class UploadFileHandler extends ChannelInboundHandlerAdapter {
        private final File outFile;
        private FileChannel outChannel;

        public UploadFileHandler(File outFile) {
            this.outFile = outFile;
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
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught in UploadFileHandler", cause);
            ctx.close();
        }
    }

    // 发送下载的文件
    static class DownloadFileHandler extends ChannelInboundHandlerAdapter {
        private final File inFile;

        public DownloadFileHandler(File inFile) {
            this.inFile = inFile;
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
                }

                inChannel.close();
            } catch (Exception e) {
                log.error("Failed to send file", e);
            } finally {
                ctx.close();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error("Exception caught in DownloadFileHandler", cause);
            ctx.close();
        }
    }
}
