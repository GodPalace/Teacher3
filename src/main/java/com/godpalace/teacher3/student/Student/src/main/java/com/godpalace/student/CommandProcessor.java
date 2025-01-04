package com.godpalace.student;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CommandProcessor {
    public static void handle(byte[] data) {
        System.out.println("Received data: " + new String(data));
    }
}
