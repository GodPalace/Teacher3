package com.godpalace.test;

import java.net.InetAddress;

public class Test {
    public static void main(String[] args) throws Exception {
        InetAddress ip = InetAddress.getByName("fe80::35dc:7920:8abc:90b1%10");
        System.out.println(ip.isReachable(1000));
    }
}
