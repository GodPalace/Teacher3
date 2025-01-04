package com.godpalace.student;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Teacher {
    private final SocketChannel channel;

    protected Teacher(SocketChannel channel) throws IOException {
        this.channel = channel;
        this.channel.configureBlocking(false);
    }

    public SocketChannel getChannel() {
        return channel;
    }

    public void close() throws IOException {
        channel.close();
    }
}
