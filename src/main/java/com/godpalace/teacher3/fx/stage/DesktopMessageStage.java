package com.godpalace.teacher3.fx.stage;

import com.godpalace.teacher3.fx.SceneAutoConfigBuilder;
import com.godpalace.teacher3.fx.animation.StageMoveTransition;
import com.godpalace.teacher3.manager.ThreadPoolManager;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class DesktopMessageStage {
    @Setter
    @Getter
    private String message;

    @Setter
    @Getter
    private long delay;

    @Getter
    private boolean isClosed = false;

    private final Stage stage;
    private final int startX;
    private final int startY;

    public DesktopMessageStage() {
        this("");
    }

    public DesktopMessageStage(String message) {
        this.message = message;
        this.delay = 2000;

        stage = new Stage();
        stage.setTitle("通知");
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.setWidth(250);
        stage.setHeight(150);

        BorderPane borderPane = new BorderPane();
        Label titleLabel = new Label("通知");
        Label label = new Label(message);
        Button button = new Button("关闭");

        titleLabel.setFont(new Font(13));
        titleLabel.setAlignment(Pos.CENTER_LEFT);
        borderPane.setTop(titleLabel);

        label.setFont(new Font(16));
        label.setAlignment(Pos.CENTER);
        borderPane.setCenter(label);

        button.setOnAction(event -> close());
        button.setPrefWidth(stage.getWidth());
        borderPane.setBottom(button);

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        startX = toolkit.getScreenSize().width;
        startY = (int) (toolkit.getScreenSize().height - stage.getHeight() - 30);

        stage.setX(startX);
        stage.setY(startY);
        stage.setScene(new SceneAutoConfigBuilder(borderPane).css().build());
    }

    public void show() {
        stage.show();
        stage.toFront();
        Toolkit.getDefaultToolkit().beep();

        StageMoveTransition transition = new StageMoveTransition(
                stage, startX, startY, startX - stage.getWidth() - 30, startY,
                Duration.seconds(0.5));
        transition.setOnFinished(event -> ThreadPoolManager.getExecutor().execute(() -> {
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                log.error("InterruptedException", e);
            }

            Platform.runLater(this::close);
        }));

        transition.play();
    }

    public void close() {
        if (isClosed) return;
        isClosed = true;

        StageMoveTransition transition = new StageMoveTransition(
                stage, stage.getX(), stage.getY(), startX, startY,
                Duration.seconds(0.5));
        transition.setOnFinished(event -> stage.close());
        transition.play();
    }
}
