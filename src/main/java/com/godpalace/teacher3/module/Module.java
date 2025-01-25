package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.text.Font;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Module {
    Button button = new Button();
    int RESPONSE_HEAD_SIZE = 4;

    short getID();
    String getName();
    String getTooltip();
    Image getIcon();

    Button getGuiButton();
    String getCommand();
    void cmd(String[] args) throws IOException;

    boolean isSupportMultiSelection();
    boolean isExecuteWithStudent();

    default Button createButton() {
        button.setText(getName());
        button.setFont(new Font(10));
        button.setTooltip(new Tooltip(getTooltip()));
        button.setPrefWidth(80);
        button.setPrefHeight(80);

        return button;
    }

    default void sendRequest(Student student, ByteBuffer data) throws IOException {
        sendRequest(student, data.array());
    }

    default void sendRequest(Student student, byte[] bytes) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(6 + bytes.length);
        buffer.putShort(getID());
        buffer.putInt(bytes.length);
        buffer.put(bytes);
        buffer.flip();
        student.getChannel().write(buffer);
    }

    default void setStatus(Student student, boolean status) {
        student.getStatus()[getID()] = status;
    }

    default boolean getStatus(Student student) {
        return student.getStatus()[getID()];
    }
}
