package com.godpalace.test;

import javafx.application.Application;
import javafx.stage.Stage;
import org.pomo.toasterfx.ToastBarToasterService;

public class TestMain extends Application {
    @Override
    public void start(Stage stage) {
        ToastBarToasterService service = new ToastBarToasterService();
        service.initialize();

    }
}
