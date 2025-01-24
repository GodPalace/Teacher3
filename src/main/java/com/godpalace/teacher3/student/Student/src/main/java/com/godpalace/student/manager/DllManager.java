package com.godpalace.student.manager;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.StandardOpenOption;

@Slf4j
public class DllManager {
    private static void releaseUnloadDll() throws Exception {
        URL unloadUrl = DllManager.class.getResource("/dll/unload");
        if (unloadUrl == null) return;

        File unloadFile = new File(unloadUrl.getFile());
        File[] files = unloadFile.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".dll")) {
                    File outFile = new File(System.getenv("TEMP"), file.getName());
                    URL url = file.toURI().toURL();

                    try (ReadableByteChannel inChannel = Channels.newChannel(url.openStream());
                         FileChannel outChannel = FileChannel.open(outFile.toPath(),
                                 StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {

                        ByteBuffer buffer = ByteBuffer.allocateDirect(409600);
                        while (inChannel.read(buffer) != -1) {
                            buffer.flip();
                            outChannel.write(buffer);
                            buffer.clear();
                        }

                        log.debug("Load unload dll: {}", file.getName());
                    } catch (Exception e) {
                        log.error("Failed to load dll: {}, cause: {}", file.getName(), e.getMessage());
                    }
                }
            }
        }
    }

    private static void releaseLoadDll() throws Exception {
        URL unloadUrl = DllManager.class.getResource("/dll/load");
        if (unloadUrl == null) return;

        File unloadFile = new File(unloadUrl.getFile());
        File[] files = unloadFile.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".dll")) {
                    File outFile = new File(System.getenv("TEMP"), file.getName());
                    URL url = file.toURI().toURL();

                    try {
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

                        System.load(outFile.getAbsolutePath());
                        log.debug("Load dll: {}", file.getName());
                    } catch (Exception e) {
                        log.error("Failed to load dll: {}, cause: {}", file.getName(), e.getMessage());
                    }
                }
            }
        }
    }

    public static void initialize() throws Exception {
        releaseUnloadDll();
        releaseLoadDll();
    }
}
