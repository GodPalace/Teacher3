package com.godpalace.student;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;

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

    @Getter
    private static final ArrayList<Teacher> teachers = new ArrayList<>();

    @Getter
    private final InetAddress addr;
    private final Selector selector;
    private final ServerSocketChannel positiveServerChannel;

    private boolean isClosed = false;

    public NetworkCore(InetAddress addr, int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(addr, port);
        this.addr = addr;

        positiveServerChannel = ServerSocketChannel.open();
        positiveServerChannel.configureBlocking(false);
        positiveServerChannel.bind(address);
        positiveServerChannel.register(allSelector, SelectionKey.OP_ACCEPT).attach(this);
        selector = Selector.open();
    }

    public static void manage() {
        ThreadPoolManager.getExecutor().execute(() -> {
            while (true) {
                try {
                    allSelector.select();

                    for (SelectionKey key : allSelector.selectedKeys()) {
                        if (key.isAcceptable()) {
                            NetworkCore core = (NetworkCore) key.attachment();

                            SocketChannel channel;
                            while ((channel = core.positiveServerChannel.accept()) == null) {
                                Thread.yield();
                            }

                            Teacher teacher = new Teacher(channel);

                            if (!NetworkCore.getTeachers().contains(teacher)) {
                                core.addTeacher(teacher);
                                log.info("New teacher connected: {}", channel.getRemoteAddress());
                            }
                        }
                    }

                    allSelector.selectedKeys().clear();
                } catch (IOException e) {
                    log.error("Error while managing selector", e);
                    break;
                }
            }
        });
    }

    public void start() {
        new Thread(this::runReceiver).start();
    }

    public void addTeacher(Teacher teacher) throws ClosedChannelException {
        teachers.add(teacher);
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

                        try {
                            CommandHandler.handle(teacher);
                        } catch (Exception e) {
                            log.error("Error while handling command", e);
                            selector.selectedKeys().remove(key);
                        }
                    }
                }

                selector.selectedKeys().clear();
            } catch (Exception e) {
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
