package com.godpalace.teacher3.fx.menu.help.about;

import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Slf4j
public class AboutStage extends Stage {
    private static Image icon = null;

    public AboutStage() {
        super();

        try {
            if (icon == null) {
                InputStream in = AboutStage.class.getResourceAsStream("/icon/menu/About.png");

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

        this.setTitle("关于");
        this.getIcons().add(icon);
        this.setResizable(false);

        try {
            URL resource = getClass().getResource("/fxml/About.fxml");

            if (resource != null) {
                this.setScene(new SceneAutoConfigBuilder(FXMLLoader.load(resource)).build());
            }
        } catch (IOException e) {
            log.warn("Failed to load root for AboutStage", e);
        }

        this.sizeToScene();
        this.centerOnScreen();
    }
}
