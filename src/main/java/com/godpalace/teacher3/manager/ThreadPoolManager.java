package com.godpalace.teacher3.manager;

import com.godpalace.teacher3.TeacherDatabase;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ThreadPoolManager {
    @Getter
    private static final ThreadPoolExecutor executor;

    @Getter
    private static final EventLoopGroup group;

    static {
        int THREAD_COUNT = TeacherDatabase.THREAD_COUNT;

        executor = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT * 4, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
        group = new NioEventLoopGroup(THREAD_COUNT);
    }

    public static void stop() {
        group.shutdownGracefully();
        executor.shutdown();
    }

    public static void waitTillAllTasksDone() {
        try {
            while (executor.getActiveCount() > 0) {
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            log.error("Error while waiting for executor to terminate", e);
        }
    }

    private ThreadPoolManager() {
    }
}
