package com.godpalace.teacher3.fx.menu;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public abstract class FXMenu extends Menu {
    public FXMenu(String title) {
        super(title);
    }

    protected void addMenuItem(String title, EventHandler<ActionEvent> event) {
        MenuItem item = new MenuItem(title);
        item.setOnAction(event);
        this.getItems().add(item);
    }

    public abstract short getSortIndex();
}
