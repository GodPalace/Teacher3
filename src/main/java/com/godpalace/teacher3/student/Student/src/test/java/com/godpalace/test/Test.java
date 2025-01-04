package com.godpalace.test;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Test {
    public static void main(String[] args) throws Exception {
        short a = 0x01, b = 0x02;

        SocketChannel channel = SocketChannel.open(new InetSocketAddress("192.168.1.20", 37000));

        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.putShort(a);
        buffer.flip();
        channel.write(buffer);

        Thread.sleep(5000);

        buffer = ByteBuffer.allocate(2);
        buffer.putShort(b);
        buffer.flip();
        channel.write(buffer);

        Thread.sleep(1000);
        channel.close();
    }
}
