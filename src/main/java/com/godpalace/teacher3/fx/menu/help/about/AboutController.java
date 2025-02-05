package com.godpalace.teacher3.fx.menu.help.about;

import com.godpalace.teacher3.TeacherGUI;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import org.kordamp.ikonli.javafx.FontIcon;

public class AboutController {
    @FXML
    private FontIcon github;

    @FXML
    public void initialize() {
        github.setOnMouseClicked(event -> {
            if (TeacherGUI.getServices() != null) {
                TeacherGUI.getServices().showDocument("https://github.com/godpalace/Teacher3");
            }
        });

        github.setCursor(Cursor.HAND);
    }
}
