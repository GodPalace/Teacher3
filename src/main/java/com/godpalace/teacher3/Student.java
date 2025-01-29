package com.godpalace.teacher3;

import com.godpalace.teacher3.manager.ModuleManager;
import com.godpalace.teacher3.manager.StudentManager;
import com.godpalace.teacher3.manager.ThreadPoolManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class Student {
    @Setter
    @Getter
    private static int idCounter = 0;

    @Getter
    private final SocketChannel channel;
    private boolean isClosed = false;

    private final AtomicReference<String> name = new AtomicReference<>("...");

    @Getter
    private final String ip;

    @Getter
    private final int port;

    @Getter
    private final int id;

    @Getter
    private final AtomicBoolean[] status;

    public Student(SocketChannel channel) throws IOException {
        this.channel = channel;
        this.channel.setOption(StandardSocketOptions.TCP_NODELAY, true);
        this.channel.setOption(StandardSocketOptions.SO_RCVBUF, 10240);
        this.channel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
        this.channel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        this.channel.setOption(StandardSocketOptions.SO_LINGER, 5);
        this.channel.socket().setSoTimeout(0);
        this.channel.configureBlocking(false);

        ThreadPoolManager.getExecutor().execute(() -> {
            try {
                name.set(((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostName());

                if (!Main.isRunOnCmd()) {
                    if (StudentManager.getStudentTable() != null) {
                        StudentManager.getStudentTable().refresh();
                    }
                }
            } catch (IOException e) {
                if (Main.isRunOnCmd()) {
                    System.out.println("Get name error, caused by: " + e.getMessage());
                } else {
                    Platform.runLater(() -> {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                        alert.setTitle("错误");
                        alert.setHeaderText("获取学生名失败");
                        alert.setContentText("原因: " + e.getMessage());
                        alert.showAndWait();
                    });
                }
            }
        });

        ip = ((InetSocketAddress) channel.getRemoteAddress()).getAddress().getHostAddress();
        port = ((InetSocketAddress) channel.getRemoteAddress()).getPort();

        status = new AtomicBoolean[ModuleManager.getModules().size() + 1];
        for (int i = 0; i < status.length; i++) status[i] = new AtomicBoolean(false);
        id = idCounter++;
    }

    public String getName() {
        return name.get();
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
