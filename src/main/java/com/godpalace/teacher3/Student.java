package com.godpalace.teacher3;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.channels.SocketChannel;

@Slf4j
public class Student {
    private static int idCounter = 0;

    @Getter
    private final SocketChannel channel;
    private boolean isClosed = false;

    @Getter
    private final String name;

    @Getter
    private final int id;

    protected Student(SocketChannel channel) throws IOException {
        this.channel = channel;
        this.channel.configureBlocking(false);

        name = channel.getRemoteAddress().toString();
        id = idCounter++;
    }

    public boolean isAlive() {
        if (isClosed) return false;

        try {
            channel.socket().sendUrgentData(0xFF);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void close() throws IOException {
        if (isClosed) return;

        StudentManager.removeStudent(this);
        StudentManager.deselectStudent(this);

        isClosed = true;
        channel.close();
    }

    public boolean isClosed() {
        return isClosed;
    }
}