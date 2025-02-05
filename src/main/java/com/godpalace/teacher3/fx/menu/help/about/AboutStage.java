package com.godpalace.teacher3.fx.menu.help.about;

import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class AboutStage extends Stage {
    private static Image icon;
    private static Parent root;

    public AboutStage() {
        super();

        try {
            if (icon == null) {
                InputStream in = AboutStage.class.getResourceAsStream("/icon/About.png");

                if (in != null) {
                    icon = new Image(in);
                    in.close();
                } else {
                    icon = TeacherGUI.getIcon();
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load icon for AboutStage", e);
            icon = TeacherGUI.getIcon();
        }

        try {
            if (root == null) {
                URL resource = getClass().getResource("/fxml/About.fxml");

                if (resource != null) {
                    root = FXMLLoader.load(resource);
                } else {
                    root = new AnchorPane();
                }
            }
        } catch (IOException e) {
            log.warn("Failed to load root for AboutStage", e);
        }

        this.setTitle("关于");
        this.getIcons().add(icon);
        this.setWidth(500);
        this.setHeight(500);
        this.setResizable(false);

        this.setScene(new SceneAutoConfigBuilder(root).build());
    }
}
