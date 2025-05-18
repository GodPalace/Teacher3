package com.godpalace.teacher3.module.file;

import javafx.scene.control.ListCell;
import javafx.scene.image.ImageView;

public class FileCell extends ListCell<RemoteFile> {
    @Override
    protected void updateItem(RemoteFile item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            ImageView view = new ImageView(item.type().getIcon());
            view.setFitHeight(32);
            view.setFitWidth(32);

            setText(item.name());
            setGraphic(view);
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
