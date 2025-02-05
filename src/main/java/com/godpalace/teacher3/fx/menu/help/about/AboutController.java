package com.godpalace.teacher3.fx.menu.help.about;

import com.godpalace.teacher3.TeacherGUI;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import org.kordamp.ikonli.boxicons.BoxiconsLogos;
import org.kordamp.ikonli.javafx.FontIcon;

public class AboutController {
    @FXML
    private Button githubButton;

    @FXML
    public void initialize() {
        githubButton.setGraphic(new FontIcon(BoxiconsLogos.GITHUB));
        githubButton.setOnAction(event -> {
            if (TeacherGUI.getServices() != null) {
                TeacherGUI.getServices().showDocument("https://github.com/godpalace/Teacher3");
            }
        });
    }
}
