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

    private final CopyOnWriteArrayList<Teacher> teachers;
    private final Selector selector;
    private final ServerSocketChannel positiveServerChannel;

    private boolean isClosed = false;

    public NetworkCore(InetSocketAddress address) throws IOException {
        positiveServerChannel = ServerSocketChannel.open();
        positiveServerChannel.configureBlocking(false);
        positiveServerChannel.bind(address);
        positiveServerChannel.register(allSelector, SelectionKey.OP_ACCEPT).attach(this);

        teachers = new CopyOnWriteArrayList<>();
        selector = Selector.open();
    }

    public static void manage() {
        new Thread(() -> {
            try {
                allSelector.select();

                for (SelectionKey key : allSelector.selectedKeys()) {
                    if (key.isAcceptable()) {
                        NetworkCore core = (NetworkCore) key.attachment();
                        SocketChannel channel = core.positiveServerChannel.accept();
                        core.addTeacher(new Teacher(channel));
                    }
                }

                allSelector.selectedKeys().clear();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void start() {
        new Thread(() -> {
            try {
                runReceiver();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    private void addTeacher(Teacher teacher) throws ClosedChannelException {
        teachers.add(teacher);
        teacher.getChannel()
                .register(selector, SelectionKey.OP_READ)
                .attach(teacher);
    }

    public void removeTeacher(Teacher teacher) throws IOException {
        teachers.remove(teacher);
        teacher.close();
    }

    public  boolean hasTeachers() {
        return !teachers.isEmpty();
    }

    public boolean hasTeacher(Teacher teacher) {
        return teachers.contains(teacher);
    }

    private void runReceiver() throws IOException {
        while (!isClosed) {
            selector.select();
            System.out.println("selector.select()");

            for (SelectionKey key : selector.selectedKeys()) {
                if (key.isReadable()) {
                    ByteArrayOutputStream bufferOut = new ByteArrayOutputStream();
                    SocketChannel channel = ((Teacher) key.attachment()).getChannel();

                    int len;
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    while ((len = channel.read(buffer)) > 0) {
                        bufferOut.write(buffer.array(), 0, len);
                    }

                    CommandProcessor.handle(bufferOut.toByteArray());
                }
            }

            selector.selectedKeys().clear();
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
