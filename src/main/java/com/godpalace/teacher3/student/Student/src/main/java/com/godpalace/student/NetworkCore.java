package com.godpalace.student;

import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class NetworkCore {
    private static final Selector allSelector;

    static {
        try {
            allSelector = Selector.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final Selector selector;
    private final ServerSocketChannel positiveServerChannel;

    private boolean isClosed = false;

    public NetworkCore(InetSocketAddress address) throws IOException {
        positiveServerChannel = ServerSocketChannel.open();
        positiveServerChannel.configureBlocking(false);
        positiveServerChannel.bind(address);
        positiveServerChannel.register(allSelector, SelectionKey.OP_ACCEPT).attach(this);
        selector = Selector.open();
    }

    public static void manage() {
        new Thread(() -> {
            try {
                while (true) {
                    allSelector.select();

                    for (SelectionKey key : allSelector.selectedKeys()) {
                        if (key.isAcceptable()) {
                            NetworkCore core = (NetworkCore) key.attachment();
                            SocketChannel channel = core.positiveServerChannel.accept();
                            core.addTeacher(new Teacher(channel));

                            log.info("New teacher connected: {}", channel.getRemoteAddress());
                        }
                    }

                    allSelector.selectedKeys().clear();
                }
            } catch (IOException e) {
                log.error("Error while managing selector", e);
            }
        }).start();
    }

    public void start() {
        new Thread(() -> {
            log.info("Starting receiver");
            runReceiver();
        }).start();
    }

    private void addTeacher(Teacher teacher) throws ClosedChannelException {
        teacher.getChannel()
                .register(selector, SelectionKey.OP_READ)
                .attach(teacher);
    }

    public void removeTeacher(Teacher teacher) throws IOException {
        teacher.close();
    }

    private void runReceiver() {
        while (!isClosed) {
            try {
                selector.select(1000);

                for (SelectionKey key : selector.selectedKeys()) {
                    Teacher teacher = (Teacher) key.attachment();

                    if (key.isReadable()) {
                        if (!teacher.isAlive()) {
                            removeTeacher(teacher);
                            continue;
                        }

                        CommandProcessor.handle(teacher);
                    }
                }

                selector.selectedKeys().clear();
            } catch (IOException e) {
                log.error("Error while running receiver", e);
            }
        }
    }

    public void close() throws IOException {
        isClosed = true;
        positiveServerChannel.close();
        selector.close();
    }

    public boolean isClosed() {
        return isClosed;
    }
}
