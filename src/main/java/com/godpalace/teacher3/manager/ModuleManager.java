package com.godpalace.teacher3.manager;

import com.godpalace.teacher3.module.Module;
import com.godpalace.teacher3.util.PackageUtil;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ModuleManager {
    @Getter
    private static final ConcurrentHashMap<String, Module> modules = new ConcurrentHashMap<>();

    @Getter
    private static final ConcurrentHashMap<Short, Module> idMap = new ConcurrentHashMap<>();

    @Getter
    private static final ConcurrentHashMap<Short, Button> guiButtons = new ConcurrentHashMap<>();

    public static void initialize() throws Exception {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            List<String> classes = PackageUtil.getClassName("com.godpalace.teacher3.module");
            for (String classPath : classes) {
                String className = classPath.substring(classPath.lastIndexOf(".") + 1);
                if (className.equals("Module")) continue;

                Class<?> clazz = loader.loadClass(classPath);
                if (!Module.class.isAssignableFrom(clazz)) continue;

                Module module = (Module) clazz.getDeclaredConstructor().newInstance();
                if (modules.containsKey(module.getCommand())) {
                    log.error("Duplicate module ID: {}", module.getID());
                    continue;
                }

                modules.put(module.getCommand(), module);
                idMap.put(module.getID(), module);

                log.debug("Loading module: {}", className);
            }
        } catch (Exception e) {
            log.error("Error initializing modules", e);
            throw new Exception("Error initializing modules", e);
        }
    }

    public static void initializeButtons() {
        for (Module module : modules.values()) {
            Button button = module.getGuiButton();
            if (button == null) continue;

            button.setDisable(true);
            guiButtons.put(module.getID(), button);
        }
    }

    public static Parent getUI() {
        FlowPane root = new FlowPane();
        root.setHgap(0);
        root.setVgap(0);

        for (Button button : guiButtons.values()) {
            root.getChildren().add(button);
        }

        return root;
    }
}
