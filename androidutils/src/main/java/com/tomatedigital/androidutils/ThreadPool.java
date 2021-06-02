package com.tomatedigital.androidutils;

import androidx.annotation.NonNull;


import com.tomatedigital.utils.general.ThreadPoolExpandableExecutor;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPool {


    /**
     * Creates a thread pool that creates new threads as needed, but
     * will reuse previously constructed threads when they are
     * available.  These pools will typically improve the performance
     * of programs that execute many short-lived asynchronous tasks.
     * Calls to {@code execute} will reuse previously constructed
     * threads if available. If no existing thread is available, a new
     * thread will be created and added to the pool. Threads that have
     * not been used for sixty seconds are terminated and removed from
     * the cache. Thus, a pool that remains idle for long enough will
     * not consume any resources. Note that pools with similar
     * properties but different details (for example, timeout parameters)
     * may be created using {@link ThreadPoolExecutor} constructors.
     *
     * @return the newly created thread pool
     */
    private static ThreadPoolExecutor pool;

    public static void startPool(int min, int max, int secs, @NonNull String prefix) {
        pool = new ThreadPoolExpandableExecutor(min, max, secs, TimeUnit.SECONDS, new ThreadFactory() {
            private final ThreadGroup group;
            private final AtomicInteger threadNumber = new AtomicInteger(1);

            {
                SecurityManager s = System.getSecurityManager();
                group = (s != null) ? s.getThreadGroup() :
                        Thread.currentThread().getThreadGroup();
            }

            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread t = new Thread(group, r,
                        prefix + "-" + threadNumber.getAndIncrement(),
                        0);
                if (t.isDaemon())
                    t.setDaemon(false);
                if (t.getPriority() != Thread.NORM_PRIORITY)
                    t.setPriority(Thread.NORM_PRIORITY);
                return t;
            }
        });

    }


    public static void run(@NonNull Runnable r) {
        ThreadPool.pool.execute(r);
    }

    public static Executor getExecutor() {
        return pool;
    }

}
