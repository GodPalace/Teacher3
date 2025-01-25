package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;

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
    public Image getIcon() {
        return null;
    }

    @Override
    public Button getGuiButton() {
        Button button = createButton();

        button.setOnAction(e -> {
            for (Student student : StudentManager.getSelectedStudents()) {
                try {
                    sendRequest(student, ByteBuffer.allocate(0));
                } catch (IOException ex) {
                    log.error("学生{}闪屏失败", student.getName(), ex);
                }
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
            try {
                sendRequest(student, ByteBuffer.allocate(0));
            } catch (IOException ex) {
                System.out.println("学生" + student.getName() + "闪屏失败");
            }

            System.out.println("闪屏完成!");
        }
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
