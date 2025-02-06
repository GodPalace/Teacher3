package com.godpalace.teacher3.fx.menu.settings.password;

import com.godpalace.teacher3.TeacherDatabase;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

public class PasswordSettingsController {
    private boolean isAccept = false;

    @FXML
    private PasswordField oldPasswordText;

    @FXML
    private PasswordField newPasswordText;

    @FXML
    private PasswordField newPasswordText2;

    @FXML
    private Text rules1;

    @FXML
    private Text rules2;

    @FXML
    private void newPasswordTextInput() {
        // 判断密码长度是否在6-18位之间
        int len = newPasswordText.getText().length();
        boolean isLenValid = len >= 6 && len <= 18;
        if (isLenValid) {
            rules1.setFill(Color.GREEN);
        } else {
            rules1.setFill(Color.RED);
        }

        // 判断密码是否包含字母和数字
        boolean isContainsLetterAndDigit = newPasswordText.getText().matches(".*[a-zA-Z].*") &&
                newPasswordText.getText().matches(".*\\d.*");
        if (isContainsLetterAndDigit) {
            rules2.setFill(Color.GREEN);
        } else {
            rules2.setFill(Color.RED);
        }

        isAccept = isLenValid && isContainsLetterAndDigit;
    }

    @FXML
    private void buttonAction() {
        if (oldPasswordText.getText().isEmpty() ||
                newPasswordText.getText().isEmpty() ||
                newPasswordText2.getText().isEmpty()) {
            showErrorDialog("密码不能为空！");
            return;
        }

        if (!oldPasswordText.getText().equals(TeacherDatabase.password)) {
            showErrorDialog("旧密码错误！");
            return;
        }

        if (oldPasswordText.getText().equals(newPasswordText.getText())) {
            showErrorDialog("新密码不能与旧密码相同！");
            return;
        }

        if (!newPasswordText.getText().equals(newPasswordText2.getText())) {
            showErrorDialog("两次输入的密码不一致！");
            return;
        }

        if (!isAccept) {
            showErrorDialog("密码不符合要求，请重新输入！");
            return;
        }

        TeacherDatabase.password = newPasswordText.getText();
        showSuccessDialog();

        // 关闭窗口
        ((Stage) oldPasswordText.getScene().getWindow()).close();
    }

    private static void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private static void showSuccessDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setGraphic(new FontIcon(BoxiconsRegular.CHECK));
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText("密码修改成功！");
        alert.showAndWait();
    }
}
