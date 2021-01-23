package com.stanwind.wmqtt.handler.pool;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * MsgHandlerPool 消息处理池
 *
 * @author : Stan
 * @version : 1.0
 * @date :  2020-11-11 17:47
 **/
public class MsgHandlerPool {

    private static MsgHandlerPool threadPool = null;
    public static final int KEEP_ALIVE_TIME = 60;
    private static final int QUEUE_SIZE = 5000;
    private static ThreadPoolExecutor poolExecutor = null;
    private static AtomicLong tId = new AtomicLong(1L);

    private MsgHandlerPool() {
        if (poolExecutor == null) {
            int processorNum = Runtime.getRuntime().availableProcessors();
            int corePoolSize = processorNum + 32;
            int maxSize = processorNum + 32;
            poolExecutor = new ThreadPoolExecutor(corePoolSize, maxSize, KEEP_ALIVE_TIME, TimeUnit.SECONDS,
                    new LinkedBlockingQueue(QUEUE_SIZE), new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "MQTTMsgHandlerPoolThread_" + MsgHandlerPool.tId.getAndIncrement());
                    thread.setDaemon(false);
                    return thread;
                }
            });
        }
    }

    public void submitTask(Runnable task) {
        poolExecutor.submit(task);
    }

    public static class Instance {

        public Instance() {
        }

        public static MsgHandlerPool getInstance() {
            if (MsgHandlerPool.threadPool == null) {
                MsgHandlerPool.threadPool = new MsgHandlerPool();
            }

            return MsgHandlerPool.threadPool;
        }
    }

}
