package com.godpalace.teacher3.manager;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class CssManager {
    @Getter
    private static final LinkedList<String> cssList = new LinkedList<>();

    public static void initializeCss() throws IOException {
        URL cssUrl = CssManager.class.getResource("/css/global");
        if (cssUrl == null) {
            log.warn("No css files found in /css/global directory.");
            return;
        }

        if (cssUrl.getProtocol().equals("file")) {
            File cssFile = new File(URLDecoder.decode(cssUrl.getFile(), StandardCharsets.UTF_8));
            File[] files = cssFile.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".css")) {
                        cssList.add(file.toURI().toString());
                        log.debug("Loaded css file: {}", file.getName());
                    }
                }
            }
        } else if (cssUrl.getProtocol().equals("jar")) {
            String[] split = URLDecoder.decode(cssUrl.getFile(), StandardCharsets.UTF_8).split("!");
            String jarPath = split[0].substring(split[0].indexOf("/"));
            String resPath = split[1].substring(1);

            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(resPath) && entryName.endsWith(".css")) {
                    String s = URLDecoder.decode(cssUrl.toString(), StandardCharsets.UTF_8);
                    String cssUrlString = s.substring(0, s.lastIndexOf("!") + 2) + entryName;
                    cssList.add(cssUrlString);

                    log.debug("Loaded css file: {}", entryName.substring(entryName.lastIndexOf("/") + 1));
                }
            }

            jarFile.close();
        }
    }
}
