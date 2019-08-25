package com.socket.impl;

import com.socket.core.IoProvider;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 服务提供者
 */
public class IoSelectorProvider implements IoProvider {

    //是否关闭
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    //读取selector
    private final Selector readSelector;
    //写出selector
    private final Selector writeSelector;
    //读线程池
    private final ExecutorService inputHandlePool;
    //写线程池
    private final ExecutorService outputHandlePool;

    public IoSelectorProvider() throws IOException {
        readSelector = Selector.open();
        writeSelector = Selector.open();
        inputHandlePool = Executors.newFixedThreadPool(4, new IoProviderThreadFactory("IoProvider-Input-Thread-"));
        outputHandlePool = Executors.newFixedThreadPool(4, new IoProviderThreadFactory("IoProvider-Output-Thread-"));
        //开始输入输出监听
        startRead();
        startWrite();
    }

    private void startWrite() {
        Thread thread = new Thread();
    }

    private void startRead() {

    }

    @Override
    public boolean registerInput(SocketChannel channel, HandlerInputCallback callback) {
        return false;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandlerOutputCallback callback) {
        return false;
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {

    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {

    }

    @Override
    public void close() throws IOException {

    }

    static class IoProviderThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        IoProviderThreadFactory(String namePrefix) {
            SecurityManager s = System.getSecurityManager();
            this.group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            this.namePrefix = namePrefix;
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}
