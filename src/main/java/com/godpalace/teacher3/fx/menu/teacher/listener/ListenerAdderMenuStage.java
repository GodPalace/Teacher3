package com.godpalace.teacher3.fx.menu.teacher.listener;

import com.godpalace.teacher3.Main;
import com.godpalace.teacher3.NetworkListener;
import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;

@Slf4j
public class ListenerAdderMenuStage extends Stage {
    public ListenerAdderMenuStage() {
        super();

        this.setTitle("添加监听器");
        this.getIcons().add(TeacherGUI.getIcon());
        this.setWidth(400);
        this.setHeight(300);
        this.setResizable(false);
        this.setOnCloseRequest(e -> this.close());

        this.setScene(new SceneAutoConfigBuilder(initializeComponents()).css().build());
        this.requestFocus();
    }

    private Parent initializeComponents() {
        BorderPane root = new BorderPane();
        AnchorPane inputPane = new AnchorPane();
        root.setCenter(inputPane);

        ComboBox<String> ipComboBox = new ComboBox<>();
        TextField portInput = new TextField();
        Button saveButton = new Button("保存");
        Button cancelButton = new Button("取消");

        // IP地址输入框
        HBox ipBox = new HBox();
        ipBox.setPadding(new Insets(3));
        ipBox.setSpacing(10);
        ipBox.setPrefHeight(30);
        ipBox.setAlignment(Pos.CENTER_LEFT);
        AnchorPane.setTopAnchor(ipBox, 0.0);
        inputPane.getChildren().add(ipBox);

        Text ipText = new Text("IP:     ");
        ipBox.getChildren().add(ipText);

        ipComboBox.setPromptText("请选择IP地址");
        Main.getIpv4s().keySet().forEach(address -> ipComboBox.getItems().add(address.getHostAddress()));
        Main.getIpv6s().keySet().forEach(address -> ipComboBox.getItems().add(address.getHostAddress()));
        ipComboBox.getSelectionModel().selectFirst();
        ipBox.getChildren().add(ipComboBox);

        // 端口输入框
        HBox portBox = new HBox();
        portBox.setPadding(new Insets(3));
        portBox.setSpacing(10);
        portBox.setPrefHeight(30);
        portBox.setAlignment(Pos.CENTER_LEFT);
        AnchorPane.setTopAnchor(portBox, 35.0);
        inputPane.getChildren().add(portBox);

        Text portText = new Text("端口: ");
        portBox.getChildren().add(portText);

        portInput.setPromptText("请输入端口号");
        portBox.getChildren().add(portInput);

        // 按钮
        HBox buttonBox = new HBox();
        buttonBox.setPadding(new Insets(3));
        buttonBox.setSpacing(5);
        buttonBox.setPrefHeight(30);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        root.setBottom(buttonBox);

        // 保存按钮
        saveButton.setDefaultButton(true);
        buttonBox.getChildren().add(saveButton);
        saveButton.setOnAction(e -> {
            if (ipComboBox.getSelectionModel().getSelectedItem() == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("IP地址错误");
                alert.setContentText("请选择正确的IP地址");
                alert.showAndWait();

                return;
            }

            if (portInput.getText().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("端口号错误");
                alert.setContentText("请输入正确的端口号");
                alert.showAndWait();

                return;
            }

            try {
                Alert alert = getAlert(ipComboBox, portInput);
                alert.showAndWait();

                this.close();
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("端口号错误");
                alert.setContentText("请输入正确的端口号");
                alert.showAndWait();
            } catch (BindException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("端口已被占用");
                alert.setContentText("请选择其他端口");
                alert.showAndWait();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("创建监听器失败");
                alert.setContentText("请尝试重新创建监听器");
                alert.showAndWait();
            }
        });

        // 取消按钮
        cancelButton.setCancelButton(true);
        buttonBox.getChildren().add(cancelButton);
        cancelButton.setOnAction(e -> close());

        // 键盘事件
        root.setOnKeyTyped(e -> {
            switch (e.getCode()) {
                case ENTER -> saveButton.fire();
                case ESCAPE -> close();
            }
        });

        return root;
    }

    private static Alert getAlert(ComboBox<String> ipComboBox, TextField portInput) throws IOException {
        NetworkListener listener = new NetworkListener(new InetSocketAddress(
                ipComboBox.getSelectionModel().getSelectedItem(),
                Integer.parseInt(portInput.getText())), true);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setGraphic(new FontIcon(BoxiconsRegular.CHECK));
        alert.setTitle("成功");
        alert.setHeaderText("创建监听器成功");
        alert.setContentText("监听器ID：" + listener.getId());

        return alert;
    }
}
