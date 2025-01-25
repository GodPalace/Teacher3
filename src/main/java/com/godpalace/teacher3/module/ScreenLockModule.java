package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.listener.StudentListener;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.ByteBuffer;

@Slf4j
public class ScreenLockModule implements Module {
    private static Image LOCK_ICON = null;

    static {
        try {
            URL url = ScreenLockModule.class.getResource("/icon/ScreenLockIcon.png");
            if (url != null) {
                LOCK_ICON = new Image(url.openStream());
            }
        } catch (Exception e) {
            log.error("Failed to load lock icon", e);
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
    public Image getIcon() {
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
                    for (Student student : StudentManager.getSelectedStudents()) {
                        ByteBuffer data = ByteBuffer.allocate(2);
                        data.putChar('1');
                        data.flip();

                        sendRequest(student, data);
                    }
                } catch (Exception e) {
                    log.error("Failed to lock screen", e);
                }

                break;

            case "unlock":
            case "off":
                try {
                    for (Student student : StudentManager.getSelectedStudents()) {
                        ByteBuffer data = ByteBuffer.allocate(1);
                        data.putChar('0');
                        data.flip();

                        sendRequest(student, data);
                    }
                } catch (Exception e) {
                    log.error("Failed to unlock screen", e);
                }

                break;

            default:
                System.out.println("命令格式错误, 请使用格式: screen-lock [lock|unlock]");
        }
    }

    @Override
    public Button getGuiButton() {
        Button button = createButton();

        button.setOnAction(e -> {
            Student student = StudentManager.getFirstSelectedStudent();

            if (student != null) {
                ByteBuffer data = ByteBuffer.allocate(1);
                data.putChar((getStatus(student)? '0' : '1'));
                data.flip();

                try {
                    sendRequest(student, data);
                    setStatus(student, !getStatus(student));
                } catch (Exception ex) {
                    log.error("Failed to execute command", ex);
                }
            }
        });

        StudentManager.addListener(new StudentListener() {
            @Override
            public void onStudentSelected(Student student) {
                button.setText(getStatus(student)? "解锁屏幕" : "锁定屏幕");
            }

            @Override
            public void onStudentDeselected(Student student) {
                if (StudentManager.getSelectedStudents().isEmpty()) {
                    button.setText("锁定屏幕");
                }
            }
        });

        return button;
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
