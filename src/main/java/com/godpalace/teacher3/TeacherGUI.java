package com.godpalace.teacher3;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.manager.*;
import com.godpalace.teacher3.util.StageUtil;
import io.github.rctcwyvrn.blake3.Blake3;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
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
    private static Image icon = null;

    @Getter
    private static Image bigIcon = null;

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

        stream = TeacherGUI.class.getResourceAsStream("/IconBig.png");
        if (stream == null) {
            log.error("IconBig.png not found");
            return;
        }

        bigIcon = new Image(stream);
        stream.close();
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

        passwordField.setPromptText("请输入密码以解锁");
        passwordField.setOnAction(event -> unlockButton.fire());

        unlockButton.setOnAction(event -> {
            Blake3 blake3 = Blake3.newInstance();
            blake3.update(passwordField.getText().getBytes());
            String hash = blake3.hexdigest();

            if (hash.equals(TeacherDatabase.password)) {
                Platform.runLater(() -> {
                    isLocked = false;
                    gui.setScene(mainScene);

                    gui.setTitle("Teacher v3");
                    gui.sizeToScene();
                    gui.centerOnScreen();

                    Notification.show("提示", "教师端已解锁", ToastTypes.SUCCESS);
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
    public void init() throws Exception {
        initializeIcon();
    }

    @Override
    public void start(Stage stage) {
        // 初始化教师端加载界面
        Stage initStage = new Stage();

        initStage.setTitle("Loading...");
        initStage.getIcons().add(icon);
        initStage.initStyle(StageStyle.UNDECORATED);
        initStage.setResizable(false);
        initStage.setAlwaysOnTop(true);
        initStage.setWidth(200);
        initStage.setHeight(200);

        ImageView view = new ImageView(bigIcon);
        view.setFitWidth(initStage.getWidth());
        view.setFitHeight(initStage.getHeight());
        BorderPane pane = new BorderPane();
        pane.setCenter(view);

        initStage.setScene(new Scene(pane));
        StageUtil.setLocationToScreenCenter(initStage);
        initStage.show();

        Platform.runLater(() -> {
            // 初始化
            try {
                Main.initialize();

                initializeHook();
                Notification.initialize();
                CssManager.initializeCss();
                MenuManager.initialize();
                ModuleManager.initializeButtons();
                services = getHostServices();
            } catch (Exception e) {
                log.error("Error while initializing the application", e);
                exit(1);
            }

            // 启动GUI
            stage.setTitle("Teacher v3");
            stage.getIcons().add(icon);
            stage.setWidth(816);
            stage.setHeight(600);
            stage.setMinWidth(656);
            stage.setMinHeight(560);
            stage.setOnCloseRequest(event -> {
                if (!isLocked) {
                    exit(0);
                } else {
                    event.consume();
                }
            });

            gui = stage;

            Parent rootPane = getRootPane();
            Parent lockPane = getLockPane();

            mainScene = new SceneAutoConfigBuilder(rootPane, stage.getWidth(), stage.getHeight()).css().build();
            hideScene = new SceneAutoConfigBuilder(lockPane, stage.getWidth(), stage.getHeight()).css().build();
            stage.setScene(mainScene);

            initStage.close();
            gui.sizeToScene();
            gui.centerOnScreen();
            gui.show();

            // 添加锁定教师端快捷键
            gui.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
                if (event.getCode().equals(KeyCode.D) && event.isAltDown()) {
                    if (!isLocked) {
                        Platform.runLater(() -> {
                            isLocked = true;
                            gui.setScene(hideScene);

                            gui.setTitle("锁定");
                            gui.sizeToScene();
                            gui.centerOnScreen();

                            Notification.show("提示", "教师端已锁定", ToastTypes.INFO);
                        });
                    }
                }
            });
        });
    }

    @Override
    public void stop() {
        log.info("Stopping the application");
        exit(0);
    }

    public static void exit(int status) {
        StackPane root = new StackPane();
        HBox hBox = new HBox();

        Text text = new Text("正在等待教师端清理(退出代码: " + status + ")...");
        ProgressIndicator indicator = new ProgressIndicator();

        hBox.setAlignment(Pos.CENTER);
        StackPane.setAlignment(indicator, Pos.CENTER);
        root.getChildren().add(hBox);

        indicator.setPrefSize(10, 10);

        text.setFont(new Font(16));
        hBox.getChildren().addAll(indicator, text);

        gui.setScene(new Scene(root, gui.getWidth(), gui.getHeight()));
        gui.sizeToScene();
        gui.centerOnScreen();

        ThreadPoolManager.stop();
        ThreadPoolManager.waitTillAllTasksDone(3000);

        Platform.exit();
        System.exit(status);
    }
}
