package com.godpalace.teacher3.fx.menu.settings;

import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.fx.menu.settings.password.PasswordSettingsStage;

public class SettingsMenu extends FXMenu {
    public SettingsMenu() {
        super("设置");

        addMenuItem("设置密码", event -> {
            PasswordSettingsStage stage = new PasswordSettingsStage();
            stage.show();
        });
    }

    @Override
    public short getSortIndex() {
        return 3;
    }
}
