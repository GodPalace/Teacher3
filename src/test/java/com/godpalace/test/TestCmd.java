package com.godpalace.test;

import java.util.Random;

/* 结果:
 * a: 277
 * b: 9723
 *
 * 事实证明: %运算符比&运算符更快
 */
public class TestCmd {
    public static boolean test() {
        long iterations = 1_000_000L;
        Random random = new Random();

        long startTime = System.currentTimeMillis();
        for (long i = 0; i < iterations; i++) {
            short result = (short) (random.nextInt() & 0xFFFF);
        }
        long andTime = System.currentTimeMillis() - startTime;

        startTime = System.currentTimeMillis();
        for (long i = 0; i < iterations; i++) {
            short result = (short) (random.nextInt() % 65536);
        }
        long modTime = System.currentTimeMillis() - startTime;

        return andTime < modTime;
    }

    public static void main(String[] args) {
        int a = 0, b = 0;

        for (int i = 0; i < 10000; i++) {
            if (test()) {
                a++;
            } else {
                b++;
            }
        }

        System.out.println("a: " + a);
        System.out.println("b: " + b);
    }
}
