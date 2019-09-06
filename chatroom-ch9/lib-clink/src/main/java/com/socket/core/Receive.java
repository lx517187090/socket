package com.socket.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 接收者
 */
public interface Receive extends Closeable {

    boolean receiveAsync(IoArgs ioArgs) throws IOException;

    /**
     * 外层传递监听
     */
    void setReceiveListener(IoArgs.IoArgsEventListener listener);
}
