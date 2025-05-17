package com.godpalace.teacher3.module.shell;

import com.godpalace.teacher3.TeacherGUI;
import com.godpalace.teacher3.fx.builder.SceneAutoConfigBuilder;
import com.godpalace.teacher3.manager.StudentManager;
import com.godpalace.teacher3.manager.ThreadPoolManager;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.InputStream;
import java.util.ArrayList;

@Slf4j
public class ShellStage extends Stage {
    private static final ArrayList<String> commands = new ArrayList<>();
    private static Image icon = null;

    private final ShellModule shellModule;

    private int commandIndex = 0;

    public ShellStage(ShellModule shellModule) {
        super();

        this.shellModule = shellModule;

        if (icon == null) {
            try {
                InputStream in = ShellStage.class.getResourceAsStream("/icon/shell/Shell.png");

                if (in != null) {
                    icon = new Image(in);
                    in.close();
                } else {
                    log.warn("Failed to load icon from resource");
                    icon = TeacherGUI.getIcon();
                }
            } catch (Exception e) {
                log.error("Failed to load icon", e);
                icon = TeacherGUI.getIcon();
            }
        }

        this.setTitle("远程命令行");
        this.getIcons().add(icon);
        this.setWidth(500);
        this.setHeight(400);
        this.setScene(new SceneAutoConfigBuilder(initializeComponents())
                .customizeCss("/css/partial/shell/TextArea.css")
                .customizeCss("/css/partial/shell/TextField.css")
                .build());
    }

    private Parent initializeComponents() {
        BorderPane root = new BorderPane();

        TextArea shellTextArea = new TextArea();
        TextField inputTextField = new TextField();

        shellTextArea.setContextMenu(new ContextMenu());
        shellTextArea.setEditable(false);
        root.setCenter(shellTextArea);
        shellTextArea.setOnMouseClicked(e -> {
            if (e.getButton().equals(MouseButton.SECONDARY)) {
                String selectedText = shellTextArea.getSelectedText();

                if (selectedText != null && !selectedText.isEmpty()) {
                    Clipboard clipboard = Clipboard.getSystemClipboard();

                    ClipboardContent content = new ClipboardContent();
                    content.putString(selectedText);

                    clipboard.setContent(content);
                }

                shellTextArea.deselect();
                inputTextField.requestFocus();

                e.consume();
            }
        });

        inputTextField.setContextMenu(new ContextMenu());
        inputTextField.setPromptText("请输入命令");
        root.setBottom(inputTextField);

        inputTextField.setOnKeyPressed(e -> {
            if (e.getCode().equals(KeyCode.UP)) {
                if (commandIndex > 0) {
                    commandIndex--;
                    inputTextField.setText(commands.get(commandIndex));
                    inputTextField.end();
                }
                
                e.consume();
            } else if (e.getCode().equals(KeyCode.DOWN)) {
                if (commandIndex < commands.size() - 1) {
                    commandIndex++;
                    inputTextField.setText(commands.get(commandIndex));
                    inputTextField.end();
                }
                
                e.consume();
            }
        });
        inputTextField.setOnAction(e -> {
            inputTextField.setDisable(true);

            ThreadPoolManager.getExecutor().execute(() -> {
                String input = inputTextField.getText();
                inputTextField.clear();

                if (!input.isEmpty()) commands.add(input);
                commandIndex = commands.size();

                if (input.equals("cls")) {
                    shellTextArea.clear();
                }

                shellModule.runShell(StudentManager.getFirstSelectedStudent(),
                        input, new ShellModule.Listener() {
                            @Override
                            public void onShellResult(String result) {
                                Platform.runLater(() -> {
                                    shellTextArea.appendText(result + "\n");
                                    shellTextArea.end();
                                });
                            }

                            @Override
                            public void onShellEnd() {
                                Platform.runLater(() -> {
                                    shellTextArea.appendText("\n");
                                    inputTextField.requestFocus();
                                });
                            }

                            @Override
                            public void onShellError() {
                                Platform.runLater(() -> {
                                    Alert alert = new Alert(Alert.AlertType.ERROR);
                                    alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
                                    alert.setTitle("错误");
                                    alert.setHeaderText("命令执行失败");
                                    alert.setContentText("请检查输入命令是否正确");
                                    alert.showAndWait();

                                    onShellEnd();
                                });
                            }
                        });

                inputTextField.setDisable(false);
            });
        });

        return root;
    }
}
