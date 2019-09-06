package com.socket.core;

import java.io.Closeable;

/**
 * 接收数据调度者
 * 实现 一份或者多份ioArgs转换为一份完整packet
 */
public interface ReceiveDispatcher extends Closeable {

    /**
     * 开始接收数据
     */
    void start();

    /**
     * 停止接收数据
     */
    void stop();

    /**
     * 接收数据回调
     */
    interface ReceivePacketCallback{

        /**
         * 接收数据完成回调
         * @param receivePacket
         */
        void onReceivePacketCompleted(ReceivePacket receivePacket);
    }
}
