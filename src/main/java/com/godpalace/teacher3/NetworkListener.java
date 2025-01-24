package com.godpalace.teacher3;

import com.godpalace.teacher3.manager.ThreadPoolManager;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Slf4j
public class NetworkListener {
    private static Selector selector = null;

    static {
        try {
            selector = Selector.open();
        } catch (IOException e) {
            log.error("Failed to open selector", e);
        }
    }

    @Setter
    @Getter
    private static int idCounter = 0;

    @Getter
    private static final HashMap<Integer, NetworkListener> listeners = new HashMap<>();

    @Getter
    private static final ArrayList<NetworkListener> scanListeners = new ArrayList<>();

    public static void manage() {
        ThreadPoolManager.getExecutor().execute(() -> {
            while (true) {
                try {
                    selector.select(1500);

                    for (SelectionKey key : selector.selectedKeys()) {
                        if (key.isAcceptable()) {
                            ServerSocketChannel sChannel = ((NetworkListener) key.attachment()).getChannel();
                            if (sChannel == null) {
                                continue;
                            }

                            SocketChannel accept;
                            while ((accept = sChannel.accept()) == null) {
                                Thread.yield();
                            }

                            Student student = new Student(accept);
                            CopyOnWriteArrayList<Student> students = StudentManager.getStudents();

                            int index = students.indexOf(student);
                            if (index >= 0) {
                                Student s = students.get(index);

                                if (s.isAlive()) {
                                    Student.setIdCounter(Student.getIdCounter() - 1);
                                    continue;
                                } else {
                                    StudentManager.removeStudent(s);
                                    s.close();
                                }
                            }

                            StudentManager.addStudent(student);
                            System.out.println("\n新的学生连接: " + student.getName()
                                    + " (ID: " + student.getId() + ")");
                            System.out.print("> ");
                        }
                    }
                } catch (Exception e) {
                    log.error("Failed to accept connection", e);
                    break;
                }
            }
        });
    }

    private final InetSocketAddress address;
    private ServerSocketChannel channel;

    public NetworkListener(InetSocketAddress address) throws IOException {
        this.address = address;
        channel = ServerSocketChannel.open();
        channel.bind(address);

        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_ACCEPT).attach(this);
    }

    public void close() throws IOException {
        channel.close();
        channel = null;
    }
}
