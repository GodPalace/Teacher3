package com.godpalace.student;

import java.net.InetAddress;
import java.net.NetworkInterface;

public record Interface(NetworkInterface iface, InetAddress addr) {
}
