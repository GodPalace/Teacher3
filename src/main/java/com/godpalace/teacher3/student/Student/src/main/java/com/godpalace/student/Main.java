package com.godpalace.student;

import com.godpalace.student.module.ModuleManager;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

@Slf4j
public class Main {
    public static final int
            MAIN_PORT = 37000,
            SCAN_PORT = 37001;

    @Getter
    private static final ArrayList<NetworkCore> cores = new ArrayList<>();

    public static NetworkCore getDefaultCore() {
        return cores.get(0);
    }

    @Getter
    private static final HashMap<InetAddress, NetworkInterface> addresses = new HashMap<>();

    private static void initializeAll() throws Exception {
        // Initialize the dll
        URL url = Main.class.getResource("/dll/Student3Dll.dll");
        if (url != null) {
            File dll = new File(System.getenv("TEMP"), "Student3Dll.dll");
            dll.deleteOnExit();

            InputStream in = url.openStream();
            FileOutputStream out = new FileOutputStream(dll);

            byte[] buffer = new byte[4096];
            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();

            System.load(dll.getAbsolutePath());
        }

        // Initialize the database
        StudentDatabase.initialize();

        // Initialize the server
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            NetworkInterface networkInterface = interfaces.nextElement();
            Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress address = addresses.nextElement();
                if (!address.isLoopbackAddress() && address instanceof Inet4Address) {
                    Main.addresses.put(address, networkInterface);

                    NetworkCore core = new NetworkCore(address, MAIN_PORT);
                    core.start();
                    cores.add(core);

                    log.debug("Initialized core at {}", address.getHostAddress());
                }
            }
        }
        NetworkCore.manage();

        // Initialize the modules
        ModuleManager.initialize();
    }

    public static void main(String[] args) {
        // Initialize all
        try {
            log.info("Starting the initialization...");
            initializeAll();
            log.info("Starting the program...");
        } catch (Exception e) {
            log.error("Error while initializing the program", e);
            System.exit(1);
        }
    }
}
