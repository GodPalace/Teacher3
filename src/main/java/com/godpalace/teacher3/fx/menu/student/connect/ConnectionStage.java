package com.godpalace.teacher3.fx.menu.student.connect;

import com.godpalace.teacher3.fx.SceneBuilder;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ConnectionStage extends Stage {
    public ConnectionStage() {
        super();

        this.setTitle("直接连接");
        this.setWidth(250);
        this.setHeight(60);
        this.setResizable(false);
        this.setOnCloseRequest(e -> this.close());

        this.setScene(new SceneBuilder(initializeComponents()).css().build());
        this.requestFocus();
    }

    private Parent initializeComponents() {
        HBox hbox = new HBox();
        hbox.setSpacing(10);

        // IP
        Text ipText = new Text("IP:");
        hbox.getChildren().add(ipText);

        TextField ipInput = new TextField();
        ipInput.setPromptText("请输入IP");
        hbox.getChildren().add(ipInput);

        // 按钮
        Button connectButton = new Button("连接");
        hbox.getChildren().add(connectButton);
        connectButton.setOnAction(e -> {
            String ip = ipInput.getText();

            if (!ip.isEmpty()) {
                try {
                    StudentManager.connect(ip);
                } catch (IOException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("连接失败");
                    alert.setHeaderText("无法连接到" + ip);
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                } finally {
                    this.close();
                }
            }
        });

        return hbox;
    }
}
