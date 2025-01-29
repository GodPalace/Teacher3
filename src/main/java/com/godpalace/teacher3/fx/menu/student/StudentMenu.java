package com.godpalace.teacher3.fx.menu.student;

import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.fx.menu.student.build.BuilderStage;
import com.godpalace.teacher3.fx.menu.student.connect.ConnectionStage;
import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.scene.control.Alert;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.io.IOException;

public class StudentMenu extends FXMenu {
    public StudentMenu() {
        super("学生");

        addMenuItem("自动扫描", event -> {
            try {
                if (StudentManager.scan()) {
                    Notification.showNotification(
                            "扫描完成", "扫描已经完成了", ToastTypes.SUCCESS);
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                    alert.setTitle("错误");
                    alert.setHeaderText("自动扫描失败");
                    alert.setContentText("请检查网络连接或手动输入IP地址");
                    alert.showAndWait();
                }
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

        addSeparator();

        addMenuItem("构建学生端", event -> {
            BuilderStage stage = new BuilderStage();
            stage.showAndWait();
        });
    }

    @Override
    public short getSortIndex() {
        return 1;
    }
}
