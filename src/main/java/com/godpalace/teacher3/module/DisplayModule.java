package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.StudentManager;
import com.godpalace.teacher3.ThreadPoolManager;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

@Slf4j
public class DisplayModule implements Module {
    private static final short CAPTURING = 0x01;
    private static final short START     = CAPTURING + 1;
    private static final short STOP      = START + 1;

    @Override
    public short getID() {
        return 0x08;
    }

    @Override
    public String getName() {
        return "屏幕监视模块";
    }

    @Override
    public String getTooltip() {
        return "监视学生的屏幕";
    }

    @Override
    public BufferedImage getIcon() {
        return null;
    }

    @Override
    public JButton getGuiButton() {
        return createButton();
    }

    @Override
    public String getCommand() {
        return "display";
    }

    private static void printHelp() {
        System.out.println("""
                    屏幕监视模块命令格式: display [option]
                    
                    option:
                      help - 显示此帮助信息
                    
                      capture - 获取屏幕截图并保存到本地""");
    }

    @Override
    public void cmd(String[] args) throws IOException {
        if (args.length != 1) {
            printHelp();
            return;
        }

        if (args[0].equals("capture")) {
            ByteBuffer buffer = ByteBuffer.allocate(2);
            buffer.putShort(CAPTURING);
            buffer.flip();

            for (final Student student : StudentManager.getSelectedStudents()) {
                ThreadPoolManager.getExecutor().execute(() -> {
                    File file = new File(System.currentTimeMillis() + "-capture.png");

                    try (FileOutputStream fileOut = new FileOutputStream(file)) {
                        sendRequest(student, buffer);

                        ByteBuffer response = ByteBuffer.allocate(4);
                        while (student.getChannel().read(response) != 4) Thread.yield();
                        response.flip();
                        int size = response.getInt();

                        ByteBuffer data = ByteBuffer.allocate(size);
                        while (student.getChannel().read(data) != size) Thread.yield();
                        data.flip();

                        // 保存到本地
                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(data.array()));
                        ImageIO.write(image, "PNG", fileOut);
                        fileOut.flush();

                        System.out.println("屏幕截图已保存到本地: " + file.getAbsolutePath());
                    } catch (IOException e) {
                        log.error("屏幕监视模块发送请求失败", e);
                    } finally {
                        System.out.print("> ");
                    }
                });
            }
        } else {
            printHelp();
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