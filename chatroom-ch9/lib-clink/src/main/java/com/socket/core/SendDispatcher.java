package com.socket.core;

import java.io.Closeable;

/**
 * 发送数据调度者
 * 缓存所有需要发送的数据  通过队列对数据进行发送
 * 并且在发送数据时 实现对数据的包装
 */
public interface SendDispatcher extends Closeable {

    /**
     * 发送数据
     * @param sendPacket 数据
     */
    void send(SendPacket sendPacket);


    /**
     * 取消发送
     * @param sendPacket 数据
     */
    void cancel(SendPacket sendPacket);
}
