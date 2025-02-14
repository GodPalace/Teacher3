package com.godpalace.teacher3.module.file;

import javafx.scene.control.ListCell;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;

public class FileCell extends ListCell<File> {
    @Override
    protected void updateItem(File item, boolean empty) {
        super.updateItem(item, empty);

        if (!empty && item != null) {
            setText(item.getName());
            setGraphic(new FontIcon((item.isDirectory()? BoxiconsRegular.FILE : BoxiconsRegular.DIRECTIONS)));
        } else {
            setText(null);
            setGraphic(null);
        }
    }
}
