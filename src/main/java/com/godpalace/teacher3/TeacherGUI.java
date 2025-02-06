package com.godpalace.teacher3;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeInputEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.manager.CssManager;
import com.godpalace.teacher3.manager.MenuManager;
import com.godpalace.teacher3.manager.ModuleManager;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class TeacherGUI extends Application {
    @Getter
    private static Stage gui;

    @Getter
    private static Image icon;

    @Getter
    private static boolean isLocked = false;

    @Getter
    private static HostServices services = null;

    @Getter
    private Scene mainScene;

    @Getter
    private Scene hideScene;

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

    private void initializeIcon() throws IOException {
        InputStream stream = TeacherGUI.class.getResourceAsStream("/Icon.png");
        if (stream == null) {
            log.error("Icon.png not found");
            return;
        }

        icon = new Image(stream);
        stream.close();
    }

    @Override
    public void init() throws Exception {
        initializeHook();
        initializeIcon();

        CssManager.initializeCss();
        MenuManager.initialize();
        ModuleManager.initializeButtons();

        services = getHostServices();
    }

    private Parent getContextPane() {
        SplitPane root = new SplitPane();
        root.setOrientation(Orientation.VERTICAL);
        root.setDividerPositions(0.6);

        root.getItems().add(StudentManager.getUI());
        root.getItems().add(ModuleManager.getUI());

        return root;
    }

    private MenuBar getMenuBar() {
        MenuBar menuBar = new MenuBar();
        menuBar.setStyle("-fx-background-color: #EEEEEE;");

        for (FXMenu menu : MenuManager.getMenus()) {
            menuBar.getMenus().add(menu);
        }

        return menuBar;
    }

    private Parent getRootPane() {
        BorderPane root = new BorderPane();
        Parent contextPane = getContextPane();
        MenuBar menuBar = getMenuBar();

        root.setTop(menuBar);
        root.setCenter(contextPane);

        return root;
    }

    private Parent getLockPane() {
        StackPane root = new StackPane();
        HBox hBox = new HBox();

        PasswordField passwordField = new PasswordField();
        Button unlockButton = new Button("解锁");

        passwordField.setPromptText("请输入密码以解锁教师端");
        passwordField.setOnAction(event -> unlockButton.fire());

        unlockButton.setOnAction(event -> {
            if (passwordField.getText().equals(TeacherDatabase.password)) {
                Platform.runLater(() -> {
                    isLocked = false;
                    gui.setScene(mainScene);

                    gui.sizeToScene();
                    gui.centerOnScreen();

                    Notification.show("提升", "教师端已解锁", ToastTypes.SUCCESS);
                });
            } else {
                passwordField.setPromptText("密码错误，请重新输入");
                passwordField.setBorder(new Border(new BorderStroke(
                        Color.RED, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            }

            passwordField.clear();
        });

        hBox.setAlignment(Pos.CENTER);
        hBox.setSpacing(10);
        hBox.getChildren().addAll(passwordField, unlockButton);

        root.getChildren().add(hBox);

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
        stage.setOnCloseRequest(event -> exit(0));
        gui = stage;

        Parent rootPane = getRootPane();
        Parent lockPane = getLockPane();

        mainScene = new SceneAutoConfigBuilder(rootPane, stage.getWidth(), stage.getHeight()).css().build();
        hideScene = new SceneAutoConfigBuilder(lockPane, stage.getWidth(), stage.getHeight()).css().build();
        stage.setScene(mainScene);

        gui.show();

        GlobalScreen.addNativeKeyListener(new NativeKeyListener() {
            @Override
            public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
                if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_D &&
                        nativeEvent.getModifiers() == NativeInputEvent.ALT_L_MASK) {

                    if (!isLocked) {
                        Platform.runLater(() -> {
                            isLocked = true;
                            gui.setScene(hideScene);

                            gui.sizeToScene();
                            gui.centerOnScreen();

                            Notification.show("提升", "教师端已锁定", ToastTypes.INFO);
                        });
                    }
                }
            }
        });
    }

    @Override
    public void stop() {
        log.info("Stopping the application");
        exit(0);
    }

    public static void exit(int status) {
        StackPane root = new StackPane();
        Text text = new Text("正在退出教师端(退出代码: " + status + ")...");

        text.setFont(new Font(16));

        StackPane.setAlignment(text, Pos.CENTER);
        root.getChildren().add(text);

        gui.setScene(new Scene(root, gui.getWidth(), gui.getHeight()));
        gui.sizeToScene();
        gui.centerOnScreen();

        Platform.exit();
        System.exit(status);
    }
}
