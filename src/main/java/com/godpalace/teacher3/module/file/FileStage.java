package com.godpalace.teacher3.module.file;

import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import com.godpalace.teacher3.module.shell.ShellStage;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class FileStage extends Stage {
    private final FileManagerModule module;
    private static Image icon = null;
    private FileListView fileListView;

    public FileStage(FileManagerModule module) {
        super();

        this.module = module;

        if (icon == null) {
            try {
                InputStream in = ShellStage.class.getResourceAsStream("/icon/file/FileManager.png");

                if (in != null) {
                    icon = new Image(in);
                    in.close();
                } else {
                    log.warn("Failed to load icon from resource");
                    icon = TeacherGUI.getIcon();
                }
            } catch (Exception e) {
                log.error("Failed to load icon", e);
                icon = TeacherGUI.getIcon();
            }
        }

        this.setTitle("文件管理");
        this.getIcons().add(icon);
        this.setWidth(500);
        this.setHeight(400);
        this.setScene(new SceneAutoConfigBuilder(initializeComponents()).css().build());
    }

    private Parent initializeComponents() {
        BorderPane root = new BorderPane();

        fileListView = new FileListView(module);
        root.setCenter(fileListView);
        root.setTop(fileListView.getDesktopMenu());

        return root;
    }

    @Override
    public void hide() {
        fileListView.close();
        super.hide();
    }
}
