package com.godpalace.teacher3.fx.pane;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.ModuleManager;
import com.godpalace.teacher3.module.Module;
import javafx.geometry.Insets;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;

import java.util.concurrent.atomic.AtomicBoolean;

public class StudentStatusPane extends FlowPane {
    public StudentStatusPane(Student student) {
        super();

        this.setHgap(0);
        this.setVgap(0);
        this.setPadding(new Insets(1));

        short i = 0;
        for (AtomicBoolean status : student.getStatus()) {
            if (status.get()) {
                Module module = ModuleManager.getIdMap().get(i);

                Image icon = module.getStatusImage();
                if (icon == null) continue;

                ImageView view = new ImageView(icon);
                view.setOnMouseEntered(event -> view.setEffect(new Glow()));
                view.setOnMouseExited(event -> view.setEffect(null));
                view.setOnMouseClicked(event -> {
                    if (event.getButton().equals(MouseButton.BACK)) {
                        Tooltip tooltip = new Tooltip(module.getName());
                        tooltip.setAutoHide(true);
                        tooltip.show(view, event.getScreenX(), event.getScreenY());
                    }
                });
                this.getChildren().add(view);
            }

            i++;
        }
    }
}
