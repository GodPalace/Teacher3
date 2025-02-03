package com.godpalace.teacher3.module;

import com.godpalace.teacher3.Student;
import com.godpalace.teacher3.manager.StudentManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
        return "屏幕监视";
    }

    @Override
    public String getTooltip() {
        return "监视学生的屏幕";
    }

    @Override
    public Image getStatusImage() {
        return null;
    }

    @Override
    public Button getGuiButton() {
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
    public void cmd(String[] args) {
        if (args.length != 1) {
            printHelp();
            return;
        }

        if (args[0].equals("capture")) {
            ByteBuf buf = Unpooled.buffer(2);
            buf.writeShort(CAPTURING);

            for (final Student student : StudentManager.getSelectedStudents()) {
                File file = new File(System.currentTimeMillis() + "-capture.png");

                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    short timestamp = student.sendRequest(getID(), buf);
                    Thread.sleep(1000);

                    ByteBuf response = readResponse(student, timestamp);
                    if (response == null) return;

                    // 保存到本地
                    try {
                        byte[] bytes = new byte[response.readableBytes()];
                        response.readBytes(bytes);

                        BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
                        if (image != null) {
                            ImageIO.write(image, "PNG", fileOut);
                            fileOut.flush();

                            System.out.println("屏幕截图已保存到本地: " + file.getAbsolutePath());
                        } else {
                            System.out.println("无法解析屏幕截图数据");
                        }
                    } catch (IOException e) {
                        System.out.println("无法保存屏幕截图到本地: " + e.getMessage());
                    } finally {
                        response.release();
                    }
                } catch (IOException | InterruptedException e) {
                    System.out.println("\n屏幕监视模块发送请求失败: " + e.getMessage());
                    System.out.print("> ");
                }
            }

            buf.release();
        } else {
            printHelp();
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
