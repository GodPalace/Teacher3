package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

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

        byte[] bytes = args[0].trim().getBytes();
        ByteBuf request = Unpooled.buffer(bytes.length);
        request.writeBytes(bytes);

        try {
            for (Student student : StudentManager.getSelectedStudents()) {
                student.sendRequest(getID(), request);
            }

            System.out.println("消息发送成功!");
        } catch (Exception ex) {
            System.out.println("消息发送失败: " + ex.getMessage());
        } finally {
            request.release();
        }
    }

    @Override
    public void onGuiButtonAction() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.getDialogPane().setGraphic(new FontIcon(BoxiconsRegular.MESSAGE_SQUARE_DETAIL));
        dialog.setGraphic(new FontIcon(BoxiconsRegular.MESSAGE_SQUARE_DETAIL));
        dialog.setTitle("发送消息");
        dialog.setHeaderText("请输入要发送的消息:");
        dialog.showAndWait();

        String message = dialog.getResult();
        if (message == null || message.isEmpty()) return;

        byte[] bytes = message.getBytes();
        ByteBuf request = Unpooled.buffer(bytes.length);
        request.writeBytes(bytes);

        for (Student student : StudentManager.getSelectedStudents()) {
            student.sendRequest(getID(), request);
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setGraphic(new FontIcon(BoxiconsRegular.CHECK_CIRCLE));
        alert.setTitle("消息发送成功");
        alert.setHeaderText("消息发送成功!");
        alert.show();

        request.release();
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
