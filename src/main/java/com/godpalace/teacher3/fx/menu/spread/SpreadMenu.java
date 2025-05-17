package com.godpalace.teacher3.fx.menu.spread;

import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.fx.menu.spread.network_share.NetworkShareSpreadStage;

public class SpreadMenu extends FXMenu {
    public SpreadMenu() {
        super("传播");

        addMenuItem("网络共享传播", event -> {
            NetworkShareSpreadStage stage = new NetworkShareSpreadStage();
            stage.show();
        });
    }

    @Override
    public short getSortIndex() {
        return 2;
    }
}
