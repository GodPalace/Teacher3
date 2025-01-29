package com.godpalace.student.manager;

import com.godpalace.student.StudentDatabase;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

@Slf4j
public class DllManager {
    private static final String outPath = System.getenv("TEMP") +
            File.separator + "StudentDll_" + System.currentTimeMillis();

    private static void releaseDll(URL url, File outFile) throws Exception {
        ReadableByteChannel inChannel = Channels.newChannel(url.openStream());
        FileChannel outChannel = FileChannel.open(outFile.toPath(),
                StandardOpenOption.WRITE, StandardOpenOption.CREATE);

        ByteBuffer buffer = ByteBuffer.allocateDirect(40960);
        while (inChannel.read(buffer) != -1) {
            buffer.flip();
            outChannel.write(buffer);
            buffer.clear();
        }

        inChannel.close();
        outChannel.close();
    }

    private static void releaseUnloadDll() throws Exception {
        URL dllUrl = DllManager.class.getResource("/dll/unload");
        if (dllUrl == null) {
            log.warn("No css files found in /dll/unload directory.");
            return;
        }

        if (dllUrl.getProtocol().equals("file")) {
            File dllFile = new File(URLDecoder.decode(dllUrl.getFile(), StandardCharsets.UTF_8));
            File[] files = dllFile.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".dll")) {
                        File outFile = new File(outPath, file.getName());
                        releaseDll(file.toURI().toURL(), outFile);

                        log.debug("Loaded dll file: {}", file.getName());
                    }
                }
            }
        } else if (dllUrl.getProtocol().equals("jar")) {
            String[] split = URLDecoder.decode(dllUrl.getPath(), StandardCharsets.UTF_8).split("!");
            String jarPath = split[0].substring(split[0].indexOf("/"));
            String resPath = split[1].substring(1);

            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(resPath) && entryName.endsWith(".dll")) {
                    String s = dllUrl.toString();
                    String dllUrlString = s.substring(0, s.lastIndexOf("!") + 2) + entryName;
                    String name = entryName.substring(entryName.lastIndexOf("/") + 1);

                    File outFile = new File(outPath, name);
                    releaseDll(new URL(dllUrlString), outFile);

                    log.debug("Loaded dll file: {}", name);
                }
            }

            jarFile.close();
        }
    }

    private static void releaseLoadDll() throws Exception {
        URL dllUrl = DllManager.class.getResource("/dll/load");
        if (dllUrl == null) {
            log.warn("No css files found in /dll/load directory.");
            return;
        }

        if (dllUrl.getProtocol().equals("file")) {
            File dllFile = new File(URLDecoder.decode(dllUrl.getFile(), StandardCharsets.UTF_8));
            File[] files = dllFile.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".dll")) {
                        File outFile = new File(outPath, file.getName());
                        releaseDll(file.toURI().toURL(), outFile);
                        System.load(outFile.getAbsolutePath());

                        log.debug("Loaded dll file: {}", file.getName());
                    }
                }
            }
        } else if (dllUrl.getProtocol().equals("jar")) {
            String[] split = URLDecoder.decode(dllUrl.getPath(), StandardCharsets.UTF_8).split("!");
            String jarPath = split[0].substring(split[0].indexOf("/"));
            String resPath = split[1].substring(1);

            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                if (entryName.startsWith(resPath) && entryName.endsWith(".dll")) {
                    String s = dllUrl.toString();
                    String dllUrlString = s.substring(0, s.lastIndexOf("!") + 2) + entryName;
                    String name = entryName.substring(entryName.lastIndexOf("/") + 1);

                    File outFile = new File(outPath, name);
                    releaseDll(new URL(dllUrlString), outFile);
                    System.load(outFile.getAbsolutePath());

                    log.debug("Loaded dll file: {}", name);
                }
            }

            jarFile.close();
        }
    }

    private static void deleteDir(File dir) {
        try {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();

                if (files != null) {
                    for (File file : files) {
                        deleteDir(file);
                    }
                }
            }

            dir.delete();
        } catch (Exception ignored) {
        }
    }

    public static void initialize() throws Exception {
        if (!StudentDatabase.lastDllOutPath.isEmpty()) {
            deleteDir(new File(StudentDatabase.lastDllOutPath));
            log.debug("Deleted old dll directory: {}", StudentDatabase.lastDllOutPath);
        }

        new File(outPath).mkdirs();
        StudentDatabase.lastDllOutPath = outPath;

        releaseUnloadDll();
        releaseLoadDll();
    }
}
