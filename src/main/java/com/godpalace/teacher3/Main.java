package com.godpalace.teacher3;

import com.godpalace.teacher3.manager.ModuleManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;

@Slf4j
public class Main {
    public static final int
            MAIN_PORT = 37000,
            SCAN_PORT = 37001;

    @Getter
    private static final HashMap<InetAddress, NetworkInterface> addresses = new HashMap<>();

    @Getter
    private static boolean isHeadless;

    private static void initializeAll() throws Exception {
        // Initialize the database
        TeacherDatabase.initialize();

        // Initialize the addresses
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();

                if (!address.isLoopbackAddress() && address.isSiteLocalAddress()) {
                    Main.addresses.put(address, networkInterface);

                    // Register a network listener for the address
                    NetworkListener listener = new NetworkListener(
                            new InetSocketAddress(address, SCAN_PORT));
                    NetworkListener.getScanListeners().add(listener);

                    log.debug("Added address: {}", address.getHostAddress());
                }
            }
        }

        // Initialize the Listener
        NetworkListener.manage();

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

        if (args.length == 0 && !environment.isHeadlessInstance()) {
            isHeadless = false;
            guiLunch();
        } else if (args[0].equals("--cmd") || args[0].equals("-c") || environment.isHeadlessInstance()) {
            isHeadless = true;
            cmdLunch();
        } else {
            System.out.println("未知的启动参数, 请使用 --cmd 或 -c 启动命令行模式或者直接运行程序");
            System.exit(0);
        }
    }

    private static void guiLunch() {
    }

    private static void cmdLunch() {
        System.out.println("======命令行模式启动成功======");
        new TeacherCmd(System.in, System.out).start();
    }
}
