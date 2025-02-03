package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import io.netty.buffer.Unpooled;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LightScreenModule implements Module {
    @Override
    public short getID() {
        return 0x03;
    }

    @Override
    public String getName() {
        return "闪屏";
    }

    @Override
    public String getTooltip() {
        return "使学生屏幕闪烁";
    }

    @Override
    public Image getStatusImage() {
        return null;
    }

    @Override
    public Button getGuiButton() {
        Button button = createButton();

        button.setOnAction(e -> {
            for (Student student : StudentManager.getSelectedStudents()) {
                student.sendRequest(getID(), Unpooled.EMPTY_BUFFER);
            }
        });

        return button;
    }

    @Override
    public String getCommand() {
        return "light-screen";
    }

    @Override
    public void cmd(String[] args) {
        if (args.length != 0) {
            System.out.println("命令格式错误, 请使用格式: light-screen");
            return;
        }

        for (Student student : StudentManager.getSelectedStudents()) {
            student.sendRequest(getID(), Unpooled.EMPTY_BUFFER);
        }

        System.out.println("闪屏完成!");
    }

    @Override
    public boolean isSupportMultiSelection() {
        return true;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return true;
    }
}
