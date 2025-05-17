package com.godpalace.teacher3.util;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class StageUtil {
    private static final Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

    public static void setLocationToScreenCenter(Stage stage) {
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }
}
