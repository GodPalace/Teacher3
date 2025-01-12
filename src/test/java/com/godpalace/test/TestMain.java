package com.godpalace.test;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.List;

public class TestMain {
    public static void main(String[] args) {
        try {
            NetworkInterface localInterface = NetworkInterface.getByInetAddress(InetAddress.getByName("192.168.0.108"));
            List<InterfaceAddress> addresses = localInterface.getInterfaceAddresses();

            for (InterfaceAddress address : addresses) {
                System.out.println(address.getAddress() + " " + address.getNetworkPrefixLength());
            }
        } catch (Exception e) {
        }
    }
}
