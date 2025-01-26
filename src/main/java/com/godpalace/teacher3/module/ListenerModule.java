package com.godpalace.teacher3.module;

import com.godpalace.teacher3.NetworkListener;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

@Getter
@Slf4j
public class ListenerModule implements Module {
    @Override
    public short getID() {
        return -0x01;
    }

    @Override
    public String getName() {
        return "监听器";
    }

    @Override
    public String getTooltip() {
        return "管理监听器以进行反向连接";
    }

    @Override
    public Image getStatusImage() {
        return null;
    }

    @Override
    public Button getGuiButton() {
        return null;
    }

    @Override
    public String getCommand() {
        return "listener";
    }

    private void printHelp() {
        System.out.println("""
                命令格式错误, 请使用格式: listener [option]
                
                option:
                  help - 显示此帮助信息
                
                  add [bind_ip] [bind_port] - 添加一个监听器
                  remove [id] - 删除一个监听器
                  list - 显示当前监听器列表""");
    }

    @Override
    public void cmd(String[] args) throws IOException {
        if (args.length < 1) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "help" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: listener help");
                    return;
                }

                // 显示帮助信息
                printHelp();
            }

            case "add" -> {
                if (args.length != 3) {
                    System.out.println("命令格式错误, 请使用格式: listener add [bind_ip] [bind_port]");
                    return;
                }

                String bind_ip;
                int bind_port;

                // 解析参数
                try {
                    bind_ip = args[1];
                    bind_port = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    System.out.println("命令格式错误, 请使用格式: listener add [bind_ip] [bind_port]");
                    return;
                }

                // 添加监听器
                NetworkListener listener = new NetworkListener(
                        new InetSocketAddress(bind_ip, bind_port), true);
                System.out.println("添加监听器成功, 监听器ID: " + listener.getId());
            }

            case "remove" -> {
                if (args.length != 2) {
                    System.out.println("命令格式错误, 请使用格式: listener remove [id]");
                    return;
                }

                int id;

                // 解析参数
                try {
                    id = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    System.out.println("命令格式错误, 请使用格式: listener remove [id]");
                    return;
                }

                // 删除监听器
                NetworkListener listener = NetworkListener.getListeners().remove(id);
                if (listener == null) {
                    System.out.println("不存在该监听器");
                } else {
                    listener.close();
                    System.out.println("删除监听器成功, 监听器ID: " + id);
                }
            }

            case "list" -> {
                if (args.length != 1) {
                    System.out.println("命令格式错误, 请使用格式: listener list");
                    return;
                }

                // 显示监听器列表
                System.out.println("当前监听器列表:");
                for (int id : NetworkListener.getListeners().keySet()) {
                    System.out.println("ID: " + id +
                            ", 地址: " + NetworkListener.getListeners().get(id).getAddress());
                }
            }

            default -> System.out.println("命令格式错误, 请使用格式: listener [option]");
        }
    }

    @Override
    public boolean isSupportMultiSelection() {
        return true;
    }

    @Override
    public boolean isExecuteWithStudent() {
        return false;
    }
}
