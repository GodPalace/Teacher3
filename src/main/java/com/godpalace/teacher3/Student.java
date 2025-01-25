package com.godpalace.teacher3;

import com.godpalace.teacher3.manager.ModuleManager;
import com.godpalace.teacher3.manager.StudentManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

@Slf4j
public class Student {
    @Setter
    @Getter
    private static int idCounter = 0;

    @Getter
    private final SocketChannel channel;
    private boolean isClosed = false;

    @Getter
    private final String name;

    @Getter
    private final String ip;

    @Getter
    private final int port;

    @Getter
    private final int id;

    @Getter
    private final boolean[] status;

    public Student(SocketChannel channel) throws IOException {
        this.channel = channel;
        this.channel.configureBlocking(false);

        name = ((InetSocketAddress) channel.getRemoteAddress()).getHostName();
        ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
        port = ((InetSocketAddress) channel.getRemoteAddress()).getPort();

        status = new boolean[ModuleManager.getModules().size() + 1];
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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Student student) {
            try {
                return ((InetSocketAddress) this.getChannel().getRemoteAddress()).getAddress()
                        .equals(((InetSocketAddress) student.getChannel().getRemoteAddress()).getAddress());
            } catch (IOException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[" + id + "] " + name + "(" + ip + ":" + port + ")";
    }
}
