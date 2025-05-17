package com.godpalace.teacher3.fx.message;

import javafx.util.Duration;
import org.pomo.toasterfx.ToastBarToasterService;
import org.pomo.toasterfx.model.ToastParameter;
import org.pomo.toasterfx.model.impl.SingleAudio;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.net.URL;

public class Notification {
    private static ToastParameter parameter = null;
    private static ToastBarToasterService toaster = null;

    private Notification() {
    }

    public static void show(String title, String message, ToastTypes type) {
        toaster.bomb(title, message, parameter, type);
    }

    public static void initialize() {
        toaster = new ToastBarToasterService();
        toaster.initialize();

        URL resource = Notification.class.getResource("/audio/MessageAudio.mp3");

        ToastParameter.ToastParameterBuilder builder =
                ToastParameter.builder().timeout(Duration.seconds(5));
        if (resource != null) builder = builder.audio(new SingleAudio(resource));
        parameter = builder.build();
    }
}
