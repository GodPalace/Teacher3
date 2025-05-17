package com.godpalace.teacher3.module.file;

import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.Separator;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

public class FileListView extends ListView<RemoteFile> {
    private final FileManagerModule module;

    private Popup currentPopup = null;

    public FileListView(FileManagerModule module) {
        super();

        this.module = module;

        setCellFactory(param -> new FileCell());
        setEditable(false);
        setOnMouseClicked(event -> {
            // 双击打开文件
            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                RemoteFile file = getSelectionModel().getSelectedItem();

                if (file != null) {
                    if (file.type() == RemoteFileType.FILE) {
                        openFileOnLocal(file);
                    } else {
                        openDirectoryOnLocal(file);
                    }
                }

                event.consume();
                return;
            }

            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                if (currentPopup != null) {
                    currentPopup.hide();
                    currentPopup = null;

                    event.consume();
                    return;
                }
            }

            if (event.getButton().equals(MouseButton.SECONDARY)) {
                RemoteFile file = getSelectionModel().getSelectedItem();

                if (currentPopup != null) {
                    currentPopup.hide();
                }

                if (file != null) {
                    if (file.type() == RemoteFileType.FILE) {
                        currentPopup = getFilePopup();
                    } else {
                        currentPopup = getDirectoryPopup();
                    }
                } else {
                    currentPopup = getDesktopPopup();
                }

                currentPopup.setAutoHide(true);
                currentPopup.show(this, event.getScreenX(), event.getScreenY());
                event.consume();
            }
        });
    }

    /* 菜单:
     * 在本地打开
     * 在学生端打开
     * ----------
     * 下载到本地
     * ----------
     * 复制路径
     * 重命名
     * 删除
     * 属性
     * */
    private Popup getFilePopup() {
        Popup popup = new Popup();

        Button openLocalButton = new Button("在本地打开");
        Button openRemoteButton = new Button("在学生端打开");
        Button downloadButton = new Button("下载到本地");
        Button copyPathButton = new Button("复制路径");
        Button renameButton = new Button("重命名");
        Button deleteButton = new Button("删除");
        Button propertiesButton = new Button("属性");

        VBox buttonBox = new VBox(openLocalButton, openRemoteButton, new Separator(), downloadButton, new Separator(), copyPathButton, renameButton, deleteButton, propertiesButton);
        popup.getContent().add(buttonBox);

        return popup;
    }

    /* 菜单:
     * 打开
     * ----------
     * 下载到本地
     * ----------
     * 复制路径
     * 重命名
     * 删除
     * 属性
     * */
    private Popup getDirectoryPopup() {
        Popup popup = new Popup();

        Button openButton = new Button("打开");
        Button downloadButton = new Button("下载到本地");
        Button copyPathButton = new Button("复制路径");
        Button renameButton = new Button("重命名");
        Button deleteButton = new Button("删除");
        Button propertiesButton = new Button("属性");

        VBox buttonBox = new VBox(openButton, new Separator(), downloadButton, new Separator(), copyPathButton, renameButton, deleteButton, propertiesButton);
        popup.getContent().add(buttonBox);

        return popup;
    }

    /* 菜单:
     * 新建文件夹
     * 新建文件
     * ----------
     * 上传到学生端
     */
    private Popup getDesktopPopup() {
        Popup popup = new Popup();

        Button newFolderButton = new Button("新建文件夹");
        Button newFileButton = new Button("新建文件");
        Button uploadButton = new Button("上传到学生端");

        VBox buttonBox = new VBox(newFolderButton, newFileButton, new Separator(), uploadButton);
        popup.getContent().add(buttonBox);

        return popup;
    }

    private void openFileOnLocal(RemoteFile file) {
    }

    private void openDirectoryOnLocal(RemoteFile file) {
    }

    private void openFileOnRemote(RemoteFile file) {
    }

    private void openDirectoryOnRemote(RemoteFile file) {
    }
}
