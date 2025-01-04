package com.godpalace.student;

import lombok.Getter;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Teacher {
    @Getter
    private final SocketChannel channel;
    private boolean isClosed = false;

    protected Teacher(SocketChannel channel) throws IOException {
        this.channel = channel;
        this.channel.configureBlocking(false);
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

        isClosed = true;
        channel.close();
    }

    public boolean isClosed() {
        return isClosed;
    }
}
