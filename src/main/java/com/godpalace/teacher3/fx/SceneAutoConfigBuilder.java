package com.godpalace.teacher3.fx;

import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

@Slf4j
public class SceneAutoConfigBuilder {
    private static final ArrayList<String> cssList = new ArrayList<>();

    public static void initializeCss() {
        URL unloadUrl = SceneAutoConfigBuilder.class.getResource("/css");
        if (unloadUrl == null) return;

        File unloadFile = new File(unloadUrl.getFile());
        File[] files = unloadFile.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".css")) {
                    cssList.add(file.toURI().toString());
                    log.debug("Loaded css file: {}", file.getName());
                }
            }
        }
    }

    private final Scene scene;

    public SceneAutoConfigBuilder(Parent root) {
        this.scene = new Scene(root);
    }

    public SceneAutoConfigBuilder css() {
        for (String css : cssList) {
            scene.getStylesheets().add(css);
        }

        return this;
    }

    public Scene build() {
        return scene;
    }
}
