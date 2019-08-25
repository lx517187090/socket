package com.socket.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 接收者
 */
public interface Receive extends Closeable {

    /**
     *
     * @param listener
     * @return
     * @throws IOException
     */
    boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException;
}
