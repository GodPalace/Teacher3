package com.godpalace.teacher3.fx.menu.student;

import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.fx.menu.student.connect.ConnectionStage;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.scene.control.Alert;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;

public class StudentMenu extends FXMenu {
    public StudentMenu() {
        super("学生");

        addMenuItem("自动扫描", event -> {
            try {
                StudentManager.scan();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                alert.setTitle("错误");
                alert.setHeaderText("自动扫描失败");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        });

        addMenuItem("直接连接", event -> {
            ConnectionStage stage = new ConnectionStage();
            stage.showAndWait();
        });
    }

    @Override
    public short getSortIndex() {
        return 1;
    }
}
