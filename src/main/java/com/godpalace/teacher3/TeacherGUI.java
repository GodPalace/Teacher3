package com.godpalace.teacher3;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.godpalace.teacher3.fx.SceneAutoConfigBuilder;
import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.manager.MenuManager;
import com.godpalace.teacher3.manager.ModuleManager;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.Parent;
import javafx.scene.control.MenuBar;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

@Slf4j
public class TeacherGUI extends Application {
    @Getter
    private static Image icon;

    private void initializeHook() {
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

    private void initializeIcon() {
        InputStream stream = TeacherGUI.class.getResourceAsStream("/icon.png");
        if (stream == null) {
            log.error("Icon not found");
            return;
        }

        icon = new Image(stream);
    }

    @Override
    public void init() {
        initializeHook();
        initializeIcon();

        SceneAutoConfigBuilder.initializeCss();
        MenuManager.initialize();
        ModuleManager.initializeButtons();
    }

    private static Parent getContextPane() {
        SplitPane root = new SplitPane();
        root.setOrientation(Orientation.VERTICAL);
        root.setDividerPositions(0.6);

        root.getItems().add(StudentManager.getUI());
        root.getItems().add(ModuleManager.getUI());

        return root;
    }

    private static MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #EEEEEE;");

        for (FXMenu menu : MenuManager.getMenus()) {
            menuBar.getMenus().add(menu);
        }

        return menuBar;
    }

    private static Parent getRootPane() {
        BorderPane root = new BorderPane();
        Parent contextPane = getContextPane();
        MenuBar menuBar = getMenuBar();

        root.setTop(menuBar);
        root.setCenter(contextPane);

        return root;
    }

    @Override
    public void start(Stage stage) {
        stage.setTitle("Teacher v3");
        stage.getIcons().add(icon);
        stage.setWidth(816);
        stage.setHeight(600);
        stage.setMinWidth(656);
        stage.setMinHeight(560);

        Parent rootPane = getRootPane();
        stage.setScene(new SceneAutoConfigBuilder(rootPane).css().build());

        stage.show();
    }

    @Override
    public void stop() {
        log.info("Stopping the application");

        Platform.exit();
        System.exit(0);
    }
}
