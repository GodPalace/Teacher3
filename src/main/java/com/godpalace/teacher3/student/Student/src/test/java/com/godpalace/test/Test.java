package com.godpalace.test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class Test {
    public static void main(String[] args) throws Exception {
        short cmd = 0x03;

        SocketChannel channel = SocketChannel.open(
                new InetSocketAddress("192.168.0.111", 37000));

        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if (input.equals("exit")) break;

            byte[] b = input.getBytes();

            ByteBuffer buffer = ByteBuffer.allocate(6 + b.length);
            buffer.putShort(cmd);
            buffer.putInt(b.length);
            buffer.put(b);

            buffer.flip();
            channel.write(buffer);
            buffer.clear();
        }

        Thread.sleep(1000);
        channel.close();
    }
}
