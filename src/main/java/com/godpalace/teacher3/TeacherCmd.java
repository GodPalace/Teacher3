package com.godpalace.teacher3;

import com.godpalace.teacher3.module.Module;
import com.godpalace.teacher3.module.ModuleManager;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Scanner;

@Slf4j
public class TeacherCmd {
    private static final StringBuilder helpString = new StringBuilder();

    private final Scanner scanner;

    static {
        helpString.append("命令列表:\n");

        for (Module module : ModuleManager.getModules().values()) {
            helpString.append("  ")
                    .append(module.getCommand())
                    .append(" - ").append(module.getTooltip()).append("\n");
        }
    }

    public TeacherCmd() {
        scanner = new Scanner(System.in);
    }

    public static void printHelp() {
        System.out.println(helpString);
    }

    public void start() {
        while (true) {
            try {
                System.out.print("> ");
                String input = scanner.nextLine();
                if (input.isEmpty()) continue;

                switch (input) {
                    case "help" -> {
                        printHelp();
                        continue;
                    }

                    case "exit" -> {
                        System.out.println("正在退出教师端...");
                        System.exit(0);
                    }
                }

                int index = input.indexOf(" ");
                String cmd = input;
                if (index > 0) cmd = input.substring(0, index);

                String[] args = new String[0];
                if (input.indexOf(" ") > 0)
                    args = input.substring(input.indexOf(" ") + 1).split(" ");

                HashMap<String, Module> modules = ModuleManager.getModules();
                if (modules.containsKey(cmd)) {
                    Module module = modules.get(cmd);

                    if (module.isExecuteWithStudent()) {
                        if (StudentManager.getSelectedStudents().size() > 1 &&
                                !module.isSupportMultiSelection()) {
                            System.out.println("该模块不支持多选");
                            continue;
                        }

                        for (Student student : StudentManager.getSelectedStudents()) {
                            if (student.isAlive()) {
                                module.cmd(args);
                            } else {
                                System.out.println(student.getName() + "已离线");
                                StudentManager.deselectStudent(student);
                                StudentManager.removeStudent(student);
                            }
                        }
                    } else {
                        module.cmd(args);
                    }
                } else {
                    System.out.println("未知命令: " + cmd);
                }
            } catch (Exception e) {
                System.out.println("教师端发生错误: " + e.getMessage());
                System.exit(-1);
            }
        }
    }
}
