package com.socket.core;

import java.io.Closeable;

/**
 * 接收者数据的调度者
 * 把一份或者多份IoArgs组合成一份packet
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
     * 接收数据后通知外层
     */
    interface ReceivePacketCallback {
        /**
         * 接收数据完成回调
         *
         * @param packet
         */
        void onReceivePacketCompleted(ReceivePacket packet);
    }
}
