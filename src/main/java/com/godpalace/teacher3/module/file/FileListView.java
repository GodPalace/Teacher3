package com.godpalace.teacher3.module.file;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.manager.ModuleManager;
import com.godpalace.teacher3.manager.StudentManager;
import com.godpalace.teacher3.module.shell.ShellModule;
import javafx.collections.FXCollections;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.io.File;
import java.util.List;

@Slf4j
public class FileListView extends ListView<RemoteFile> {
    private final FileManagerModule module;
    private final Student student;

    private Popup currentPopup = null;

    public FileListView(FileManagerModule module) {
        super();

        this.module = module;
        this.student = StudentManager.getFirstSelectedStudent();

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
                        openDirectory(file);
                    }
                }

                event.consume();
                return;
            }

            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 1) {
                if (currentPopup != null) {
                    currentPopup.hide();
                    currentPopup = null;

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
                }

                if (currentPopup != null) {
                    currentPopup.setAutoHide(true);
                    currentPopup.show(this, event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            }
        });

        refreshFiles();
    }

    public void close() {
        if (currentPopup != null) {
            currentPopup.hide();
            currentPopup = null;
        }
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
     * 刷新
     * ----------
     * 新建文件夹
     * 新建文件
     * ----------
     * 上传到学生端
     */
    protected Parent getDesktopMenu() {
        Button backButton =     new Button("<");
        Button refreshButton =  new Button("刷新");
        Button newMkdirButton = new Button("新建文件夹");
        Button newFileButton =  new Button("新建文件");
        Button uploadButton =   new Button("上传文件到学生端");

        // 返回
        backButton.setOnAction(event -> {
            module.cd(student, "..");
            refreshFiles();
        });

        // 刷新
        refreshButton.setOnAction(event -> refreshFiles());

        // 新建文件夹
        newMkdirButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setGraphic(new FontIcon(BoxiconsRegular.DIRECTIONS));
            dialog.setTitle("新建文件夹");
            dialog.setHeaderText("请输入文件夹名称:");
            dialog.setContentText("文件夹名称:");

            dialog.showAndWait().ifPresent(name -> {
                if (name.trim().isEmpty()) return;

                module.newDir(student, name);
                refreshFiles();
            });
        });

        // 新建文件
        newFileButton.setOnAction(event -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setGraphic(new FontIcon(BoxiconsRegular.FILE));
            dialog.setTitle("新建文件");
            dialog.setHeaderText("请输入文件名称:");
            dialog.setContentText("文件名称:");

            dialog.showAndWait().ifPresent(name -> {
                if (name.trim().isEmpty()) return;

                module.newFile(student, name);
                refreshFiles();
            });
        });

        // 上传文件
        uploadButton.setOnAction(event -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("选择文件");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("所有文件", "*.*"));

            List<File> paths = fileChooser.showOpenMultipleDialog(getScene().getWindow());

            if (paths != null) {
                for (File file : paths) {
                    module.upload(student, file.getAbsolutePath());
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("上传成功");
                alert.setHeaderText("文件已上传到学生端");
                alert.showAndWait();
            }
        });

        return new HBox(backButton, refreshButton, newMkdirButton, newFileButton, uploadButton);
    }

    private void openFileOnLocal(RemoteFile file) {
    }

    private void openFileOnRemote(RemoteFile file) {
        ((ShellModule) ModuleManager.getIdMap().get((short) 0x04)).runShell(student, "start \"" + file.path() + "\"", new ShellModule.Listener() {
            @Override
            public void onShellEnd() {
                Notification.show("打开成功", "文件已在学生端打开", ToastTypes.SUCCESS);
            }

            @Override
            public void onShellError() {
                Notification.show("打开失败", "文件在学生端打开失败", ToastTypes.FAIL);
            }
        });
    }

    private void openDirectory(RemoteFile file) {
        module.cd(student, file.path());
        refreshFiles();

        scrollTo(0);
    }

    private void refreshFiles() {
        try {
            List<RemoteFile> files = module.list(student);
            setItems(FXCollections.observableArrayList(files));
        } catch (Exception e) {
            log.error("Error in refreshFiles", e);
        }
    }
}
