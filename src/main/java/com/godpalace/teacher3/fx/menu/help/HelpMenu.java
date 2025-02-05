package com.godpalace.teacher3.fx.menu.help;

import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.fx.menu.help.about.AboutStage;

public class HelpMenu extends FXMenu {
    public HelpMenu() {
        super("帮助");

        addMenuItem("关于", event -> {
            AboutStage stage = new AboutStage();
            stage.show();
        });
    }

    @Override
    public short getSortIndex() {
        return 2;
    }
}
