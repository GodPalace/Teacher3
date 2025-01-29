package com.godpalace.teacher3.fx.builder;

import com.godpalace.teacher3.manager.CssManager;
import javafx.scene.Parent;
import javafx.scene.Scene;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SceneAutoConfigBuilder {
    private final Scene scene;

    public SceneAutoConfigBuilder(Parent root) {
        this.scene = new Scene(root);
    }

    public SceneAutoConfigBuilder css() {
        for (String css : CssManager.getCssList()) {
            scene.getStylesheets().add(css);
        }

        return this;
    }

    public Scene build() {
        return scene;
    }
}
