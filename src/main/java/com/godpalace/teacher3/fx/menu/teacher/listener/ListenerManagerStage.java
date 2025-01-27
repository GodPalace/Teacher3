package com.godpalace.teacher3.fx.menu.teacher.listener;

import com.godpalace.teacher3.NetworkListener;
import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.SceneAutoConfigBuilder;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ListenerManagerStage extends Stage {
    private ListView<NetworkListener> listenerList;

    public ListenerManagerStage() {
        super();

        this.setTitle("监听器管理");
        this.getIcons().add(TeacherGUI.getIcon());
        this.setWidth(600);
        this.setHeight(400);
        this.setResizable(false);
        this.setOnCloseRequest(event -> this.close());

        this.setScene(new SceneAutoConfigBuilder(initializeComponents()).css().build());
        this.requestFocus();
    }

    private Parent initializeComponents() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(5));

        listenerList = new ListView<>();
        Button addButton = new Button("添加");
        Button removeButton = new Button("移除");

        // 监听器显示面板
        listenerList.setEditable(false);
        root.setCenter(listenerList);
        refresh();
        listenerList.setOnMouseReleased(event -> {
            SelectionModel<NetworkListener> selectionModel = listenerList.getSelectionModel();
            removeButton.setDisable(selectionModel.getSelectedItem() == null);
        });

        // 按钮面板
        HBox buttonBox = new HBox();
        buttonBox.setSpacing(10);
        buttonBox.setPadding(new Insets(3));
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(addButton, removeButton);
        root.setBottom(buttonBox);

        // 按钮事件
        addButton.setOnAction(event -> {
            ListenerAdderMenuStage stage = new ListenerAdderMenuStage();
            stage.showAndWait();

            refresh();
        });

        removeButton.setDisable(true);
        removeButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("移除监听器");
            alert.setHeaderText("确认移除?");
            alert.setContentText("移除后将无法恢复, 请确认是否要移除?");
            alert.showAndWait();

            if (alert.getResult() == ButtonType.OK) {
                try {
                    NetworkListener selectedItem = listenerList.getSelectionModel().getSelectedItem();
                    selectedItem.close();
                    NetworkListener.getListeners().remove(selectedItem.getId());
                } catch (IOException e) {
                    Alert alert1 = new Alert(Alert.AlertType.ERROR);
                    alert1.setTitle("移除监听器");
                    alert1.setHeaderText("移除失败");
                    alert1.setContentText("移除失败, 请重试.");
                    alert1.showAndWait();
                }

                refresh();
                removeButton.setDisable(true);
            }
        });

        return root;
    }

    private void refresh() {
        listenerList.getItems().setAll(NetworkListener.getListeners().values());
    }
}
