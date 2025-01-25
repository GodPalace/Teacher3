package com.godpalace.teacher3;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.godpalace.teacher3.fx.TeacherScene;
import com.godpalace.teacher3.manager.ModuleManager;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.SplitPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TeacherGUI extends Application {
    @Override
    public void init() {
        try {
            GlobalScreen.registerNativeHook();
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    GlobalScreen.unregisterNativeHook();
                } catch (Exception e) {
                    log.error("Error while unregistering the native hook", e);
                }
            }));
        } catch (Exception e) {
            log.error("Error while registering the native hook", e);
        }
    }

    private static Parent getRootPane() {
        SplitPane root = new SplitPane();
        root.setOrientation(Orientation.VERTICAL);
        root.setDividerPositions(0.6);

        root.getItems().add(StudentManager.getUI());
        root.getItems().add(ModuleManager.getUI());

        return root;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Teacher v3");
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setMinWidth(640);
        stage.setMinHeight(560);

        Parent rootPane = getRootPane();
        stage.setScene(new TeacherScene(stage, rootPane).configure().build());

        stage.show();
    }

    @Override
    public void stop() {
    }
}
