package com.godpalace.teacher3.fx.menu.student.connect;

import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.SceneAutoConfigBuilder;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class ConnectionStage extends Stage {
    public ConnectionStage() {
        super();

        this.setTitle("直接连接");
        this.getIcons().add(TeacherGUI.getIcon());
        this.setWidth(250);
        this.setHeight(60);
        this.setResizable(false);
        this.setOnCloseRequest(e -> this.close());

        this.setScene(new SceneAutoConfigBuilder(initializeComponents()).css().build());
        this.requestFocus();
    }

    private Parent initializeComponents() {
        HBox hbox = new HBox();
        Text ipText = new Text("IP:");
        TextField ipInput = new TextField();
        Button connectButton = new Button("连接");

        // 面板
        hbox.setSpacing(10);
        hbox.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                connectButton.fire();
            }
        });

        // 标签
        hbox.getChildren().add(ipText);

        // 输入框
        ipInput.setPromptText("请输入IP");
        hbox.getChildren().add(ipInput);

        // 按钮
        hbox.getChildren().add(connectButton);
        connectButton.setOnAction(e -> connect(ipInput.getText()));

        return hbox;
    }

    private void connect(String ip) {
        if (!ip.isEmpty()) {
            try {
                if (StudentManager.connect(ip) == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                    alert.setTitle("连接失败");
                    alert.setHeaderText("无法连接到" + ip);
                    alert.setContentText("请检查网络连接或输入的IP地址是否正确.");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setGraphic(new FontIcon(BoxiconsRegular.CHECK));
                    alert.setTitle("连接成功");
                    alert.setHeaderText("已连接到" + ip);
                    alert.setContentText("你可以开始操作该学生了.");
                    alert.showAndWait();
                }
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("连接失败");
                alert.setHeaderText("无法连接到" + ip);
                alert.setContentText(ex.getMessage());
                alert.showAndWait();
            } finally {
                this.close();
            }
        }
    }
}
