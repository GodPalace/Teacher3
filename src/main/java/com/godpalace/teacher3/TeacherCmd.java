package com.godpalace.teacher3;

import com.godpalace.teacher3.module.Module;
import com.godpalace.teacher3.module.ModuleManager;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Scanner;

@Slf4j
public class TeacherCmd {
    private static final StringBuilder helpString = new StringBuilder();
    private final Scanner scanner;

    static {
        helpString.append("命令列表:\n");
        helpString.append("  help - 显示命令列表\n");
        helpString.append("  wait [seconds] - 等待指定秒数\n");
        helpString.append("  exit - 退出教师端\n");
        helpString.append("\n");

        for (Module module : ModuleManager.getModules().values()) {
            String command = module.getCommand().trim();
            if (command.isEmpty()) continue;

            helpString.append("  ")
                    .append(command)
                    .append(" - ").append(module.getTooltip()).append("\n");
        }
    }

    public TeacherCmd(InputStream inputStream, OutputStream outputStream) {
        scanner = new Scanner(inputStream);
        System.setOut(new PrintStream(outputStream));
    }

    public static void printHelp() {
        System.out.println(helpString);
    }

    public void start() {
        while (true) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                int index = input.indexOf(" ");
                String cmd = input;
                if (index > 0) cmd = input.substring(0, index);

                String[] args = new String[0];
                if (input.indexOf(" ") > 0)
                    args = input.substring(input.indexOf(" ") + 1).split(" ");

                switch (cmd) {
                    case "help" -> {
                        printHelp();
                        continue;
                    }

                    case "wait" -> {
                        int seconds = Integer.parseInt(args[0]);

                        try {
                            synchronized (this) {
                                wait(seconds * 1000L);
                            }
                        } catch (InterruptedException e) {
                            System.out.println("等待中断");
                        }

                        continue;
                    }

                    case "exit" -> {
                        System.out.println("正在退出教师端...");
                        System.exit(0);
                    }
                }

                HashMap<String, Module> modules = ModuleManager.getModules();
                if (modules.containsKey(cmd)) {
                    Module module = modules.get(cmd);

                    if (module.isExecuteWithStudent() &&
                            StudentManager.getSelectedStudents().size() > 1 &&
                            !module.isSupportMultiSelection()) {

                        System.out.println("该模块不支持多选");
                        continue;
                    }

                    try {
                        if (module.isExecuteWithStudent() &&
                                StudentManager.getFirstSelectedStudent() == null) {
                            System.out.println("请先选择学生");
                            continue;
                        }

                        module.cmd(args);
                    } catch (IOException e) {
                        System.out.println("命令执行失败: " + e.getMessage());
                    }
                } else {
                    System.out.println("未知命令: " + cmd);
                }
            } catch (NoSuchElementException e) {
                break;
            } catch (Exception e) {
                System.out.println("教师端发生错误: " + e.getMessage());
                log.error("TeacherCmd error", e);

                break;
            }
        }

        scanner.close();
    }
}
