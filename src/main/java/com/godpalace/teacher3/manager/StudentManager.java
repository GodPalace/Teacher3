package com.godpalace.teacher3.manager;

import com.godpalace.teacher3.Main;
import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.listener.StudentListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class StudentManager {
    @Getter
    private static final ObservableList<Student> students =
            FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    @Getter
    private static final ObservableList<Student> selectedStudents =
            FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    private static final LinkedList<StudentListener> listeners = new LinkedList<>();

    static {
        ThreadPoolManager.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Iterator<Student> iterator = students.iterator();
                        while (iterator.hasNext()) {
                            Student student = iterator.next();

                            if (student.isAlive()) continue;
                            iterator.remove();
                            StudentManager.deselectStudent(student);
                            student.close();
                        }

                        synchronized (this) {
                            wait(500);
                        }
                    } catch (Exception e) {
                        log.error("Error in StudentManager", e);
                        return;
                    }
                }
            }
        });
    }

    public static void addListener(StudentListener listener) {
        listeners.add(listener);
    }

    public static void removeListener(StudentListener listener) {
        listeners.remove(listener);
    }

    public static Student getFirstStudent() {
        if (students.isEmpty()) return null;
        return students.get(0);
    }

    public static Student getFirstSelectedStudent() {
        if (selectedStudents.isEmpty()) return null;
        return selectedStudents.get(0);
    }

    public static void addStudent(Student student) {
        students.add(student);

        for (StudentListener listener : listeners) {
            listener.onStudentAdded(student);
        }
    }

    public static boolean removeStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                students.remove(student);
                deselectStudent(student);

                for (StudentListener listener : listeners) {
                    listener.onStudentRemoved(student);
                }

                return true;
            }
        }

        return false;
    }

    public static void removeStudent(Student student) {
        if (!students.contains(student))
            return;

        students.remove(student);
        deselectStudent(student);

        for (StudentListener listener : listeners) {
            listener.onStudentRemoved(student);
        }
    }

    public static boolean selectStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                selectedStudents.add(student);

                for (StudentListener listener : listeners) {
                    listener.onStudentSelected(student);
                }

                return true;
            }
        }

        return false;
    }

    public static void selectStudent(Student student) {
        if (!students.contains(student))
            return;

        selectedStudents.add(student);
        for (StudentListener listener : listeners) {
            listener.onStudentSelected(student);
        }
    }

    public static boolean deselectStudent(int id) {
        for (Student student : selectedStudents) {
            if (student.getId() == id) {
                selectedStudents.remove(student);

                for (StudentListener listener : listeners) {
                    listener.onStudentDeselected(student);
                }

                return true;
            }
        }

        return false;
    }

    public static void deselectStudent(Student student) {
        if (!selectedStudents.contains(student))
            return;

        for (StudentListener listener : listeners) {
            listener.onStudentDeselected(student);
        }

        selectedStudents.remove(student);
    }

    public static void selectAllStudents() {
        clearSelectedStudents();
        selectedStudents.addAll(students);

        for (StudentListener listener : listeners) {
            for (Student student : students) {
                listener.onStudentSelected(student);
            }
        }
    }

    public static void clearSelectedStudents() {
        for (StudentListener listener : listeners) {
            for (Student student : selectedStudents) {
                listener.onStudentDeselected(student);
            }
        }

        selectedStudents.clear();
    }

    public static Student getStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }

        return null;
    }

    public static Student connect(String ip) throws IOException {
        InetSocketAddress address = new InetSocketAddress(ip, 37000);
        if (!address.getAddress().isReachable(3000))
            return null;

        SocketChannel channel = SocketChannel.open(address);
        Student student = new Student(channel);
        students.add(student);

        return student;
    }

    public static void disconnect(Student student) throws IOException {
        removeStudent(student);

        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.putShort((short) 0x99);
        buffer.putInt(0);
        buffer.flip();
        student.getChannel().write(buffer);
        buffer.clear();

        int count = 0;
        while (student.getChannel().read(buffer) != 2) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                log.error("Error in StudentManager.disconnect, cause: {}", e.getMessage());
            }

            if (count++ > 5) {
                throw new SocketTimeoutException("Timeout while waiting for response");
            }
        }
        buffer.flip();
        int code = buffer.getShort();

        if (code == 0x00) {
            student.close();
        } else {
            throw new IOException("Error while disconnecting: " + code);
        }
    }

    public static boolean scan() throws IOException {
        for (NetworkInterface anInterface : Main.getAddresses().values()) {
            InetSocketAddress group = new InetSocketAddress(
                    InetAddress.getByName("224.3.7.1"), Main.SCAN_PORT);

            try (MulticastSocket socket = new MulticastSocket()) {
                socket.joinGroup(group, anInterface);

                byte[] data = new byte[1];
                data[0] = (byte) 1;

                DatagramPacket packet = new DatagramPacket(data, data.length, group);
                socket.send(packet);
            } catch (IOException e) {
                log.error("Error in StudentManager.scan, cause: {}", e.getMessage());
                return false;
            }
        }

        return true;
    }

    public static void build() throws IOException {
        build(null, 0);
    }

    public static void build(String ip, int port) throws IOException {
        File tempFile = new File(System.getenv("TEMP") + "\\Student.jar");
        File file = new File(System.currentTimeMillis() + "-Student.jar");

        URL url = Main.class.getResource("/Student.jar");
        if (url == null) {
            System.out.println("未找到资源文件, 无法构建学生端程序");
            return;
        }

        // 输出到临时文件
        InputStream in = url.openStream();
        FileOutputStream out = new FileOutputStream(tempFile);
        byte[] buffer = new byte[10240];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.close();

        // 写入临时数据
        ZipInputStream zipIn = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(file));
        ZipEntry entry = zipIn.getNextEntry();

        while (entry != null) {
            zipOut.putNextEntry(new ZipEntry(entry.getName()));
            buffer = new byte[10240];

            while ((len = zipIn.read(buffer)) != -1) {
                zipOut.write(buffer, 0, len);
            }

            zipOut.closeEntry();
            entry = zipIn.getNextEntry();
        }

        // 写入反向连接配置
        if (ip != null && port != 0) {
            byte[] ipBytes = ip.getBytes(StandardCharsets.UTF_8);
            int ipLength = ipBytes.length;

            try {
                byte[] configData = new byte[ipLength + 3];

                // IP地址长度
                configData[0] = (byte) ipLength;

                // IP地址
                System.arraycopy(ipBytes, 0, configData, 1, ipBytes.length);

                // 端口号
                configData[ipLength + 1] = (byte) (port & 0xFF);
                configData[ipLength + 2] = (byte) ((port >> 8) & 0xFF);

                // 写入配置数据
                zipOut.putNextEntry(new ZipEntry("ReverseConnectConfig.data"));
                zipOut.write(configData);
                zipOut.closeEntry();
            } catch (NumberFormatException e) {
                System.out.println("命令格式错误, 请使用格式: student build [ip] [port]");
                return;
            }
        }

        zipOut.close();
        zipIn.close();
        tempFile.delete();

        in.close();
    }

    // GUI部分的代码
    private static final int ICON_SIZE   = 16;
    private static final int ICON_SPACER = 10;

    private static void onListChanged(TableView<Student> list) {
        selectedStudents.setAll(list.getSelectionModel().getSelectedItems());

        for (Short id : ModuleManager.getNotSupportMultiSelections()) {
            Button button = ModuleManager.getGuiButtons().get(id);
            button.setDisable(list.getSelectionModel().getSelectedItems().size() > 1);
        }
    }

    private static void setRightClickMenu(TableView<Student> list) {
        Popup popup = new Popup();
        Button disconnectButton = new Button("断开连接");

        // Button
        disconnectButton.setOnAction(event -> {
            popup.hide();

            Student student = list.getSelectionModel().getSelectedItem();
            if (student == null) return;

            try {
                disconnect(student);
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("断开连接失败");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });

        // Popup
        popup.setAutoHide(true);
        popup.setOnAutoHide(event -> popup.hide());
        popup.getContent().addAll(disconnectButton);

        list.setOnMouseReleased(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                if (list.getSelectionModel().getSelectedItem() == null) return;
                popup.show(list, event.getScreenX(), event.getScreenY());
            }
        });
    }

    private static Node getPlaceholder() {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(0);

        Label label = new Label("当前没有在线的学生, ");
        label.setAlignment(Pos.CENTER);
        label.setGraphic(new FontIcon(BoxiconsRegular.USER_X));
        label.setGraphicTextGap(ICON_SPACER);
        hBox.getChildren().add(label);

        Hyperlink hyperlink = new Hyperlink("点击此处进行自动扫描");
        hyperlink.setOnAction(event -> {
            try {
                if (scan()) {
                    Toolkit.getDefaultToolkit().beep();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                    alert.setTitle("错误");
                    alert.setHeaderText("自动扫描失败");
                    alert.setContentText("请检查网络连接或手动输入IP地址");
                    alert.showAndWait();
                }
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("自动扫描失败");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });
        hBox.getChildren().add(hyperlink);

        return hBox;
    }

    public static Parent getUI() {
        TableView<Student> studentTable = new TableView<>(students);
        studentTable.setPlaceholder(getPlaceholder());
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        studentTable.setEditable(false);
        studentTable.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<Student>) change -> onListChanged(studentTable));

        TableColumn<Student, Integer> idColumn = new TableColumn<>("ID");
        TableColumn<Student, String> nameColumn = new TableColumn<>("主机名");
        TableColumn<Student, String> ipColumn = new TableColumn<>("IP地址");
        TableColumn<Student, Integer> portColumn = new TableColumn<>("端口号");

        idColumn.setSortable(true);
        nameColumn.setSortable(false);
        ipColumn.setSortable(false);
        portColumn.setSortable(false);

        idColumn.setMinWidth(50);
        nameColumn.setMinWidth(100);
        ipColumn.setMinWidth(100);
        portColumn.setMinWidth(50);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        ipColumn.setCellValueFactory(new PropertyValueFactory<>("ip"));
        portColumn.setCellValueFactory(new PropertyValueFactory<>("port"));
        studentTable.getColumns().addAll(idColumn, nameColumn, ipColumn, portColumn);

        setRightClickMenu(studentTable);

        return studentTable;
    }
}
