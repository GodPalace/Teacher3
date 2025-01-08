package com.godpalace.student.module;

import com.godpalace.student.Teacher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public interface Module {
    short getID();
    String getName();
    void execute(Teacher teacher, ByteBuffer data) throws Exception;

    boolean isLocalModule();

    default void sendResponse(SocketChannel channel, ByteBuffer data) throws IOException {
        sendResponse(channel, data.array());
    }

    default void sendResponse(SocketChannel channel, byte[] bytes) throws IOException {
        ByteBuffer response = ByteBuffer.allocate(4 + bytes.length);
        response.putInt(bytes.length);
        response.put(bytes);
        response.flip();
        channel.write(response);
    }
}
