package com.godpalace.teacher3;

import com.godpalace.teacher3.manager.ModuleManager;
import javafx.application.Application;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.net.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;

@Slf4j
public class Main {
    public static final String IPV4_MULTICAST_GROUP = "224.3.7.1";
    public static final String IPV6_MULTICAST_GROUP = "ff02::307:1";

    public static final String IPV4_RESCAN_GROUP = "224.3.7.2";
    public static final String IPV6_RESCAN_GROUP = "ff02::307:2";

    public static final int
            MAIN_PORT   = 37000,
            SCAN_PORT   = 37001,
            RESCAN_PORT = 37002;

    @Getter
    private static final HashMap<InetAddress, NetworkInterface> ipv4s = new HashMap<>();

    @Getter
    private static final HashMap<InetAddress, NetworkInterface> ipv6s = new HashMap<>();

    @Getter
    private static boolean isRunOnCmd;

    private static void initializeAll() throws Exception {
        // Initialize the database
        TeacherDatabase.initialize();

        // Initialize the addresses
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

            if (networkInterface.isUp() && !networkInterface.isLoopback() && networkInterface.supportsMulticast()) {
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();

                    if (!address.isLoopbackAddress()) {
                        if (address instanceof Inet4Address && networkInterface.supportsMulticast()) {
                            Main.ipv4s.put(address, networkInterface);
                        } else if (address instanceof Inet6Address) {
                            Main.ipv6s.put(address, networkInterface);
                        }

                        // Register a network listener for the ipv4 address
                        NetworkListener listener = new NetworkListener(
                                new InetSocketAddress(address, SCAN_PORT), false);
                        NetworkListener.getScanListeners().add(listener);

                        log.debug("Added address: {}", address.getHostAddress());
                    }
                }
            }
        }

        // Initialize the rescan listener
        RescanListener.initialize();

        // Initialize modules
        ModuleManager.initialize();
    }

    public static void main(String[] args) {
        Locale.setDefault(Locale.CHINA);

        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (args.length == 0 && !environment.isHeadlessInstance()) {
            isRunOnCmd = false;
        } else if (args[0].equals("--cmd") || args[0].equals("-c") || environment.isHeadlessInstance()) {
            isRunOnCmd = true;
        } else {
            System.out.println("未知的启动参数, 请使用 --cmd 或 -c 启动命令行模式或者直接运行程序");
            System.exit(0);
        }

        if (isRunOnCmd) {
            cmdSetupAndLunch();
        } else {
            guiSetupAndLunch();
        }
    }

    private static void guiSetupAndLunch() {
        Application.launch(TeacherGUI.class);
    }

    private static void cmdSetupAndLunch() {
        System.out.println("命令行模式启动中...");
        initialize();

        System.out.println("命令行模式启动成功!");
        new TeacherCMD(System.in, System.out).start();
    }

    protected static void initialize() {
        try {
            long start = System.currentTimeMillis();
            log.info("Starting the initialization...");

            initializeAll();
            log.info("End of initialization in {}s",
                    (System.currentTimeMillis() - start) / 1000.0F);
        } catch (Exception e) {
            log.error("Error while initializing the program", e);
            System.exit(1);
        } finally {
            System.gc();
        }
    }
}
