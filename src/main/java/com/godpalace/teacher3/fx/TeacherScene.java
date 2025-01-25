package com.godpalace.teacher3.fx;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TeacherScene {
    private final Scene scene;
    private final Stage stage;

    public TeacherScene(Stage stage, Parent root) {
        this.scene = new Scene(root);
        this.stage = stage;
    }

    public TeacherScene configure() {
        return this;
    }

    public Scene build() {
        return scene;
    }
}
