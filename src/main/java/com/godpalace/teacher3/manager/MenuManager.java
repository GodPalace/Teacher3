package com.godpalace.teacher3.manager;

import com.godpalace.teacher3.fx.menu.FXMenu;
import com.godpalace.teacher3.util.PackageUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Slf4j
public class MenuManager {
    @Getter
    private static final ArrayList<FXMenu> menus = new ArrayList<>();

    public static void initialize() {
        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            List<String> classes = PackageUtil.getClassName("com.godpalace.teacher3.fx.menu");
            for (String classPath : classes) {
                String className = classPath.substring(classPath.lastIndexOf(".") + 1);
                if (className.equals("FXMenu")) continue;

                Class<?> clazz = loader.loadClass(classPath);
                if (!FXMenu.class.isAssignableFrom(clazz)) continue;

                FXMenu menu = (FXMenu) clazz.getConstructor().newInstance();
                menus.add(menu);

                log.debug("Loaded menu: {}", menu.getText());
            }
        } catch (Exception e) {
            log.error("Error initializing menus", e);
        }

        menus.sort(Comparator.comparingInt(FXMenu::getSortIndex));
    }
}
