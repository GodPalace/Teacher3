package com.godpalace.teacher3.fx.menu.student.build;

import com.godpalace.teacher3.NetworkListener;
import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.awt.*;
import java.io.File;
import java.net.InetSocketAddress;

public class BuilderStage extends Stage {
    private static String lastPath = System.getProperty("user.home");

    private String ip = null;
    private int port = -1;

    public BuilderStage() {
        super();

        this.setTitle("构建学生端");
        this.getIcons().add(TeacherGUI.getIcon());
        this.setWidth(400);
        this.setHeight(300);
        this.setResizable(false);

        this.setScene(new SceneAutoConfigBuilder(initializeComponents()).css().build());
    }

    private Parent initializeComponents() {
        BorderPane root = new BorderPane();

        // 学生端类型选择
        HBox typeBox = new HBox();
        Label typeLabel = new Label("学生端类型: ");
        typeLabel.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> typeComboBox = new ComboBox<>();

        typeBox.setAlignment(Pos.CENTER_LEFT);
        typeBox.setSpacing(10);
        typeBox.setPadding(new Insets(3));
        typeComboBox.getItems().addAll("标准学生端", "带反向连接的学生端", "带反向连接的学生端(高级)");
        typeComboBox.setPromptText("请选择学生端类型");
        typeComboBox.setOnAction(e -> {
            switch (typeComboBox.getSelectionModel().getSelectedIndex()) {
                // 标准学生端
                case 0 -> root.setCenter(getStandardStudentPane());

                // 带反向连接的学生端
                case 1 -> root.setCenter(getReverseConnectStudentPane());

                // 带反向连接的学生端(高级)
                case 2 -> root.setCenter(getReverseConnectExStudentPane());
            }
        });
        typeBox.getChildren().addAll(typeLabel, typeComboBox);
        root.setTop(typeBox);

        // 构建按钮
        HBox buttonBox = new HBox();
        Button generateButton = new Button("构建");

        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(3));
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.getChildren().add(generateButton);
        generateButton.setOnAction(e -> {
            if (ip == null || port == -1) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("请填写完整且正确的信息");
                alert.show();

                return;
            }

            // 选择保存路径
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择保存路径");
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("JAR文件", "*.jar"));
            fileChooser.setInitialFileName("Student.jar");
            fileChooser.setInitialDirectory(new File(lastPath));
            File file = fileChooser.showSaveDialog(this);
            if (file == null) return;
            lastPath = file.getParent();
            this.close();

            // 构建学生端
            try {
                InetSocketAddress address;
                if (ip.isEmpty() && port == 0) address = null;
                else address = new InetSocketAddress(ip, port);

                StudentManager.build(address, file);

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setGraphic(new FontIcon(BoxiconsRegular.CHECK));
                alert.setTitle("成功");
                alert.setHeaderText("构建成功");
                alert.setContentText("学生端已保存至" + file.getAbsolutePath());
                alert.show();

                Toolkit.getDefaultToolkit().beep();
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("构建失败");
                alert.setContentText(ex.getMessage());
                alert.show();
            }
        });
        root.setBottom(buttonBox);

        return root;
    }

    // 标准学生端
    private Parent getStandardStudentPane() {
        ip = "";
        port = 0;

        return new AnchorPane();
    }

    // 带反向连接的学生端
    private Parent getReverseConnectStudentPane() {
        AnchorPane root = new AnchorPane();

        // 监听器选择框
        HBox hBox = new HBox();
        Label label = new Label("监听器: ");
        ComboBox<NetworkListener> comboBox = new ComboBox<>();

        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setPrefHeight(30.0);
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(3));
        hBox.getChildren().addAll(label, comboBox);
        comboBox.getSelectionModel().selectFirst();
        comboBox.setItems(FXCollections.observableArrayList(NetworkListener.getListeners().values()));
        comboBox.setOnAction(e -> {
            NetworkListener listener = comboBox.getSelectionModel().getSelectedItem();
            if (listener == null) return;

            ip = listener.getAddress().getAddress().getHostAddress();
            port = listener.getAddress().getPort();
        });
        root.getChildren().add(hBox);

        return root;
    }

    // 带反向连接的学生端(高级)
    private Parent getReverseConnectExStudentPane() {
        AnchorPane root = new AnchorPane();

        // IP输入框
        HBox ipBox = new HBox();
        Label ipLabel = new Label("IP:     ");
        TextField ipInput = new TextField();

        AnchorPane.setTopAnchor(ipBox, 0.0);
        ipBox.setAlignment(Pos.CENTER_LEFT);
        ipBox.setPrefHeight(30.0);
        ipBox.setSpacing(10);
        ipBox.setPadding(new Insets(3));
        ipBox.getChildren().addAll(ipLabel, ipInput);
        ipInput.setPromptText("请输入IP地址");
        ipInput.textProperty().addListener((observable, oldValue, newValue) -> ip = newValue);
        root.getChildren().add(ipBox);

        // 端口输入框
        HBox portBox = new HBox();
        Label portLabel = new Label("端口: ");
        TextField portTextField = new TextField();

        AnchorPane.setTopAnchor(portBox, 30.0);
        portBox.setAlignment(Pos.CENTER_LEFT);
        portBox.setPrefHeight(30.0);
        portBox.setSpacing(10);
        portBox.setPadding(new Insets(3));
        portBox.getChildren().addAll(portLabel, portTextField);
        portTextField.setPromptText("请输入端口号");
        portTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.matches("\\d*") && !newValue.isEmpty()) {
                port = Integer.parseInt(newValue);
            } else {
                port = -1;
            }
        });
        root.getChildren().add(portBox);

        return root;
    }
}
