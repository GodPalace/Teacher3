package com.godpalace.student;

import lombok.Getter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;

public class Teacher {
    @Getter
    private final SocketChannel channel;

    @Getter
    private final String ip;

    private boolean isClosed = false;

    public Teacher(SocketChannel channel) throws IOException {
        this.channel = channel;
        this.channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        this.channel.setOption(StandardSocketOptions.SO_RCVBUF, 10240);
        this.channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        this.channel.setOption(StandardSocketOptions.SO_LINGER, 5);
        this.channel.socket().setSoTimeout(0);
        this.channel.configureBlocking(false);

        this.ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Teacher teacher) {
            try {
                return ((InetSocketAddress) this.getChannel().getRemoteAddress()).getAddress()
                        .equals(((InetSocketAddress) teacher.getChannel().getRemoteAddress()).getAddress());
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }
}
