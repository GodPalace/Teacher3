package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class MessageModule implements Module {
    @Override
    public short getID() {
        return 0x02;
    }

    @Override
    public String getName() {
        return "发送消息";
    }

    @Override
    public String getTooltip() {
        return "发送一条消息给学生, 并在学生屏幕中央显示3秒";
    }

    @Override
    public Image getStatusImage() {
        return null;
    }

    @Override
    public String getCommand() {
        return "message";
    }

    @Override
    public void cmd(String[] args) {
        if (args.length != 1) {
            System.out.println("命令格式错误, 请使用格式: message <消息>");
            return;
        }

        try {
            byte[] bytes = args[0].trim().getBytes();
            ByteBuffer data = ByteBuffer.allocate(bytes.length);
            data.put(bytes);
            data.flip();

            int count = 0;
            for (Student student : StudentManager.getSelectedStudents()) {
                try {
                    sendRequest(student, data);
                    count++;
                } catch (IOException ex) {
                    System.out.println("发送消息到学生[" + student.getName() + "]失败: " + ex.getMessage());
                }
            }

            System.out.println("消息发送成功! 共有" + count + "个学生发送成功.");
        } catch (Exception ex) {
            System.out.println("消息发送失败: " + ex.getMessage());
        }
    }

    @Override
    public Button getGuiButton() {
        Button button = createButton();

        button.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setGraphic(new FontIcon(BoxiconsRegular.MESSAGE_SQUARE_DETAIL));
            dialog.setTitle("发送消息");
            dialog.setHeaderText("请输入要发送的消息:");
            dialog.showAndWait();

            String message = dialog.getResult();
            if (message == null || message.isEmpty()) return;

            byte[] bytes = message.getBytes();
            ByteBuffer data = ByteBuffer.allocate(bytes.length);
            data.put(bytes);
            data.flip();

            int count = 0;
            for (Student student : StudentManager.getSelectedStudents()) {
                try {
                    sendRequest(student, data);
                    count++;
                } catch (IOException ex) {
                    log.error("发送消息到学生[{}]失败", student.getName(), ex);
                }
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setGraphic(new FontIcon(BoxiconsRegular.CHECK_CIRCLE));
            alert.setTitle("消息发送成功");
            alert.setHeaderText("共有" + count + "个学生发送成功.");
            alert.show();
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
