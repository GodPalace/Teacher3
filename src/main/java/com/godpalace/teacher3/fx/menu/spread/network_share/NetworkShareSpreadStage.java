package com.godpalace.teacher3.fx.menu.spread.network_share;

import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;

@Slf4j
public class NetworkShareSpreadStage extends Stage {
    public NetworkShareSpreadStage() {
        super();

        setTitle("网络共享传播");
        getIcons().add(TeacherGUI.getIcon());
        setWidth(500);
        setHeight(500);
        setResizable(false);

        try {
            URL resource = getClass().getResource("/fxml/NetworkShareSpread.fxml");

            if (resource != null) {
                this.setScene(new SceneAutoConfigBuilder(FXMLLoader.load(resource)).css().build());
            }
        } catch (IOException e) {
            log.warn("Failed to load root for NetworkShareSpreadStage", e);
        }

        this.sizeToScene();
        this.centerOnScreen();
    }
}
