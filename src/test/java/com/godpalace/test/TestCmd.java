package com.godpalace.test;

import io.github.rctcwyvrn.blake3.Blake3;

public class TestCmd {
    public static void main(String[] args) {
        Blake3 blake3 = Blake3.newInstance();
        String password = "password";
        blake3.update(password.getBytes());
        System.out.println(blake3.hexdigest());
    }
}
