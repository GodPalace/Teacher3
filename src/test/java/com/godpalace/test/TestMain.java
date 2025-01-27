package com.godpalace.test;

import com.godpalace.teacher3.fx.stage.DeputyStage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class TestMain extends Application {
    @Override
    public void start(Stage stage) {
        Label label = new Label("Hello World");
        Label label2 = new Label("Hello World2");

        stage.setWidth(300);
        stage.setHeight(200);
        stage.setScene(new Scene(label));
        stage.show();

        DeputyStage deputyStage = new DeputyStage("Deputy Stage");
        deputyStage.setScene(new Scene(label2));
        deputyStage.show(stage);
    }
}
