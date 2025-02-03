package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Main;
import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.fx.message.Notification;
import io.netty.buffer.ByteBuf;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.io.IOException;
import java.net.SocketTimeoutException;

public interface Module {
    int RESPONSE_HEAD_SIZE = 4;

    short getID();
    String getName();
    String getTooltip();
    Image getStatusImage();

    Button getGuiButton();
    String getCommand();
    void cmd(String[] args) throws IOException;

    boolean isSupportMultiSelection();
    boolean isExecuteWithStudent();

    default Button createButton() {
        Button button = new Button(getName());

        button.setTooltip(new Tooltip(getTooltip()));
        button.setPrefWidth(80);
        button.setPrefHeight(80);

        return button;
    }

    default ByteBuf readResponse(Student student, short timestamp) throws SocketTimeoutException {
        short count = 0;

        while (!student.getResponses().containsKey(getID()) || !student.getResponses().get(getID()).containsKey(timestamp)) {
            try {
                synchronized (this) {
                    wait(100);
                }

                if (count++ > 70) {
                    if (Main.isRunOnCmd()) {
                        System.out.println(getName() + "获取响应超时");
                    } else {
                        Notification.show("错误", "获取响应超时: " + getName(), ToastTypes.FAIL);
                    }

                    return null;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        ByteBuf buf = student.getResponses().get(getID()).get(timestamp);
        student.getResponses().get(getID()).remove(timestamp);

        return buf;
    }
}
