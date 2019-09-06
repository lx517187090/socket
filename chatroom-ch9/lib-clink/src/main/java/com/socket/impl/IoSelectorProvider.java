package com.socket.impl;

import com.socket.core.IoProvider;
import com.socket.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
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


    //是否处于input中
    private final AtomicBoolean inReqInput = new AtomicBoolean(false);


    //是否处于Output中
    private final AtomicBoolean inReqOut = new AtomicBoolean(false);


    //读取selector
    private final Selector readSelector;


    private final HashMap<SelectionKey, Runnable> inputCallbackMap = new HashMap<>();

    private final HashMap<SelectionKey, Runnable> outputCallbackMap = new HashMap<>();

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

    /**
     * 输入操作
     */
    private void startWrite() {
        Thread thread = new Thread("clink IoSelectorProvide writeSelector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (writeSelector.select() == 0) {
                            waitSelection(inReqOut);
                            continue;
                        }
                        Set<SelectionKey> selectionKeys = writeSelector.selectedKeys();
                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                handleSelection(selectionKey, SelectionKey.OP_WRITE, outputCallbackMap, outputHandlePool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }

        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    private void startRead() {
        Thread thread = new Thread("clink IoSelectorProvide readSelector Thread") {
            @Override
            public void run() {
                while (!isClosed.get()) {
                    try {
                        if (readSelector.select() == 0) {
                            waitSelection(inReqInput);
                            continue;
                        }
                        Set<SelectionKey> selectionKeys = readSelector.selectedKeys();
                        for (SelectionKey selectionKey : selectionKeys) {
                            if (selectionKey.isValid()) {
                                handleSelection(selectionKey, SelectionKey.OP_READ, inputCallbackMap, inputHandlePool);
                            }
                        }
                        selectionKeys.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        thread.setPriority(Thread.MAX_PRIORITY);
        thread.start();
    }

    @Override
    public boolean registerInput(SocketChannel channel, HandlerInputCallback callback) {
        return registerSelection(channel, readSelector, SelectionKey.OP_READ, inReqInput, inputCallbackMap, callback) != null;
    }

    @Override
    public boolean registerOutput(SocketChannel channel, HandlerOutputCallback callback) {
        return registerSelection(channel, writeSelector, SelectionKey.OP_WRITE, inReqOut, outputCallbackMap, callback) != null;
    }

    @Override
    public void unRegisterInput(SocketChannel channel) {
        unRegisterSelection(channel, readSelector, inputCallbackMap);
    }

    @Override
    public void unRegisterOutput(SocketChannel channel) {
        unRegisterSelection(channel, writeSelector, outputCallbackMap);
    }

    /**
     * 等待监听
     */
    private static void waitSelection(final AtomicBoolean locker){
        synchronized (locker){
            if (locker.get()){
                try {
                    locker.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 注册监听
     *
     * @param channel channel
     * @param selector      选择器
     * @param registerOps   注册监听类型
     * @param locker        锁
     * @param map           处理map
     * @param runnable      处理类
     */
    private static SelectionKey registerSelection(SocketChannel channel, Selector selector,
                          int registerOps, AtomicBoolean locker,
                          HashMap<SelectionKey, Runnable> map, Runnable runnable) {
        synchronized (locker) {
            locker.set(true);
            try {
                //唤醒当前selector  让selector不处于 select()状态
                selector.wakeup();

                SelectionKey key = null;
                if (channel.isRegistered()){
                    //查询是否注册过
                    key = channel.keyFor(selector);
                    if (key != null){
                        key.interestOps(key.readyOps() | registerOps);
                    }
                }
                if (key == null){
                    key = channel.register(selector, registerOps);
                    map.put(key, runnable);
                }
                return key;
            } catch (ClosedChannelException e) {
                return null;
            } finally {
                //解除锁定状态
                locker.set(false);
                try {
                    locker.notify();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     *  解除注册
     * @param channel 通道
     * @param selector 选择器
     * @param map map
     */
    private static void unRegisterSelection(SocketChannel channel, Selector selector, Map<SelectionKey, Runnable> map){
        if (channel.isRegistered()){
            SelectionKey key = channel.keyFor(selector);
            if (key != null){
                key.cancel();
                map.remove(key);
                selector.wakeup();
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            inputHandlePool.shutdownNow();
            outputHandlePool.shutdownNow();
            inputCallbackMap.clear();
            outputCallbackMap.clear();
            readSelector.wakeup();
            writeSelector.wakeup();
            CloseUtils.close(readSelector, writeSelector);
        }
    }

    /**
     * 处理消息
     */
    private void handleSelection(SelectionKey key, int keyOps,
                                 HashMap<SelectionKey, Runnable> map,
                                 ExecutorService pool) {
        //重点  把key取消注册
        key.interestOps(key.readyOps() & ~keyOps);

        Runnable runnable = null;
        try {
            runnable = map.get(key);
        } catch (Exception e) {

        }

        if (runnable != null && !pool.isShutdown()) {
            pool.execute(runnable);
        }
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
