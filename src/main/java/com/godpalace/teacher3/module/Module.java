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

    default ByteBuf readResponse(Student student, short timestamp) {
        long start = System.currentTimeMillis();
        long end = start + 10000;

        while (System.currentTimeMillis() - start < 10000) {
            try {
                synchronized (student.getLock()) {
                    student.getLock().wait(Math.max(0, end - System.currentTimeMillis()));
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (student.getResponses().containsKey(getID()) &&
                    student.getResponses().get(getID()).containsKey(timestamp)) {

                ByteBuf buf = student.getResponses().get(getID()).get(timestamp);
                student.getResponses().get(getID()).remove(timestamp);

                return buf;
            }
        }

        if (Main.isRunOnCmd()) {
            System.out.println("获取响应超时, 请检查网络连接或重试.");
        } else {
            Notification.show("获取响应超时", "请检查网络连接或重试.", ToastTypes.FAIL);
        }

        return null;
    }
}
