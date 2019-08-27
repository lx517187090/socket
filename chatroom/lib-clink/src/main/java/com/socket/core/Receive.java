package com.socket.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 接收者
 */
public interface Receive extends Closeable {

    /**
     * 接收消息
     */
    boolean receiveAsync(IoArgs args) throws IOException;

    /**
     * 设置接收监听
     */
    void setReceiveListener(IoArgs.IoArgsEventListener listener);
}
