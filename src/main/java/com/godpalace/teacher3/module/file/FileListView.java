package com.godpalace.teacher3.module.file;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseButton;
import javafx.stage.Popup;

import java.io.File;

public class FileListView extends ListView<File> {
    private final FileManagerModule module;
    private final Popup filePopup = getFilePopup();
    private final Popup emptyPopup = getEmptyPopup();

    public FileListView(FileManagerModule module) {
        super();

        this.module = module;

        setCellFactory(param -> new FileCell());
        setEditable(false);
        setOnMouseClicked(event -> {
            filePopup.hide();
            emptyPopup.hide();

            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                File file = getSelectionModel().getSelectedItem();

                if (file.isFile()) {
                    openFile(file);
                } else {
                    openDirectory(file);
                }
            }

            if (event.getButton().equals(MouseButton.SECONDARY)) {
                if (getSelectionModel().getSelectedItem() != null) {
                    filePopup.show(this, event.getScreenX(), event.getScreenY());
                } else {
                    emptyPopup.show(this, event.getScreenX(), event.getScreenY());
                }
            }
        });
    }

    private Popup getFilePopup() {
        Popup popup = new Popup();

        Button openButton = new Button("打开");
        // ------------------------------
        Button downloadButton = new Button("下载");
        Button renameButton = new Button("重命名");
        Button deleteButton = new Button("删除");

        openButton.setOnAction(event -> {
            File file = getSelectionModel().getSelectedItem();

            if (file.isFile()) {
                openFile(file);
            } else {
                openDirectory(file);
            }
        });

        downloadButton.setOnAction(event -> {});

        renameButton.setOnAction(event -> {});

        deleteButton.setOnAction(event -> {});

        popup.getContent().addAll(openButton, new Separator(), downloadButton, renameButton, deleteButton);

        return popup;
    }

    private Popup getEmptyPopup() {
        return null;
    }

    private void openFile(File file) {
    }

    private void openDirectory(File file) {
    }
}
