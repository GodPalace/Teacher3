package com.godpalace.teacher3;

import com.godpalace.teacher3.module.ModuleManager;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
public class Main {
    private static void initializeAll() throws Exception {
        // Initialize the database
        TeacherDatabase.initialize();

        // Initialize modules
        ModuleManager.initialize();
    }

    public static void main(String[] args) {
        try {
            log.info("Starting the initialization...");
            initializeAll();
            log.info("Starting the program...");
        } catch (Exception e) {
            log.error("Error while initializing the program", e);
            System.exit(1);
        }

        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        if (args.length == 0 && !environment.isHeadlessInstance())
            guiLunch();
        else if (args[0].equals("--cmd") || args[0].equals("-c") || environment.isHeadlessInstance())
            cmdLunch();
        else {
            System.out.println("未知的启动参数, 请使用 --cmd 或 -c 启动命令行模式");
            System.exit(-1);
        }
    }

    private static void guiLunch() {
    }

    private static void cmdLunch() {
        System.out.println("======命令行模式启动成功======");

        TeacherCmd cmd = new TeacherCmd();
        cmd.start();
    }
}
