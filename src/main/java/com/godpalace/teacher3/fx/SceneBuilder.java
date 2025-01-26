package com.godpalace.teacher3.fx;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneBuilder {
    private final Scene scene;

    public SceneBuilder(Parent root) {
        this.scene = new Scene(root);
    }

    public SceneBuilder css() {
        scene.getStylesheets().add("/css/Button.css");
        scene.getStylesheets().add("/css/Menu.css");
        scene.getStylesheets().add("/css/ListView.css");

        return this;
    }

    public Scene build() {
        return scene;
    }
}
