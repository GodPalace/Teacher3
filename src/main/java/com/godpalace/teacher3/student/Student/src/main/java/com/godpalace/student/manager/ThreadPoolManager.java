package com.godpalace.student.manager;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
    private static final int THREAD_COUNT = 10;

    @Getter
    private static final ThreadPoolExecutor executor;

    @Getter
    private static final EventLoopGroup group;

    static {
        executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT * 3,
                10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        group = new NioEventLoopGroup(THREAD_COUNT);
    }

    private ThreadPoolManager() {
    }
}
