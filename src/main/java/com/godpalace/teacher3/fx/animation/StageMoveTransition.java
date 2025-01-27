package com.godpalace.teacher3.fx.animation;

import javafx.animation.Transition;
import javafx.stage.Stage;
import javafx.util.Duration;

public class StageMoveTransition extends Transition {
    protected Stage stage;
    protected double startX, startY, endX, endY;

    public StageMoveTransition(Stage stage, double startX, double startY, double endX, double endY,
                               Duration duration) {
        this.stage = stage;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        setCycleDuration(duration);
    }

    @Override
    protected void interpolate(double v) {
        stage.setX(startX + (endX - startX) * v);
        stage.setY(startY + (endY - startY) * v);
    }
}
