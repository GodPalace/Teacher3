package com.godpalace.teacher3;

import lombok.Getter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    @Getter
    private static final ThreadPoolExecutor executor;

    static {
        executor = new ThreadPoolExecutor(8, 20,
                10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());
    }

    private ThreadPoolManager() {
    }
}
