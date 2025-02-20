package com.godpalace.teacher3.fx.menu.teacher;

import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.fx.menu.teacher.listener.ListenerManagerStage;

public class TeacherMenu extends FXMenu {
    public TeacherMenu() {
        super("Teacher");

        addMenuItem("监听器管理", event -> {
            ListenerManagerStage stage = new ListenerManagerStage();
            stage.showAndWait();
        });

        addSeparator();

        addMenuItem("退出", event -> TeacherGUI.exit(0));
    }

    @Override
    public short getSortIndex() {
        return 0;
    }
}
