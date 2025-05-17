package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Main;
import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
public class ScreenLockModule implements Module {
    private static Image LOCK_ICON = null;

    public ScreenLockModule() {
        if (!Main.isRunOnCmd()) {
            try {
                InputStream in = ScreenLockModule.class.getResourceAsStream("/moduleIcon/ScreenLockIcon.png");

                if (in != null) {
                    LOCK_ICON = new Image(in);
                    in.close();
                } else {
                    log.error("Failed to load lock icon");
                }
            } catch (IOException e) {
                log.error("Failed to load lock icon", e);
            }
        }
    }

    @Override
    public short getID() {
        return 0x01;
    }

    @Override
    public String getName() {
        return "屏幕锁定";
    }

    @Override
    public String getTooltip() {
        return "使学生屏幕黑屏";
    }

    @Override
    public Image getStatusImage() {
        return LOCK_ICON;
    }

    @Override
    public String getCommand() {
        return "screen-lock";
    }

    @Override
    public void cmd(String[] args) {
        if (args.length != 1) {
            System.out.println("命令格式错误, 请使用格式: screen-lock [lock|unlock]");
            return;
        }

        switch (args[0]) {
            case "lock":
            case "on":
                try {
                    ByteBuf request = Unpooled.buffer(2);
                    request.writeShort((short) 1);

                    for (Student student : StudentManager.getSelectedStudents()) {
                        student.sendRequest(getID(), request);
                    }

                    request.release();
                } catch (Exception e) {
                    log.error("Failed to lock screen", e);
                }

                break;

            case "unlock":
            case "off":
                try {
                    ByteBuf request = Unpooled.buffer(2);
                    request.writeShort((short) 0);

                    for (Student student : StudentManager.getSelectedStudents()) {
                        student.sendRequest(getID(), request);
                    }

                    request.release();
                } catch (Exception e) {
                    log.error("Failed to unlock screen", e);
                }

                break;

            default:
                System.out.println("命令格式错误, 请使用格式: screen-lock [lock|unlock]");
        }
    }

    @Override
    public Button createButton() {
        Button button = Module.super.createButton();

        StudentManager.getSelectedStudents().addListener((ListChangeListener<Student>) change -> {
            Student student = StudentManager.getFirstSelectedStudent();
            button.setText((student != null && student.getStatus(getID())? "解锁屏幕" : "锁定屏幕"));
        });

        return button;
    }

    @Override
    public void onGuiButtonAction() {
        Student student = StudentManager.getFirstSelectedStudent();

        if (student != null) {
            ByteBuf data = Unpooled.buffer(2);
            data.writeShort((short) (!student.getStatus(getID()) ? 1 : 0));

            try {
                student.sendRequest(getID(), data);
                student.setStatus(getID(), !student.getStatus(getID()));
            } catch (Exception ex) {
                log.error("Failed to execute command", ex);
            } finally {
                data.release();
            }
        }
    }

    @Override
    public boolean isSupportMultiSelection() {
        return false;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return true;
    }
}
