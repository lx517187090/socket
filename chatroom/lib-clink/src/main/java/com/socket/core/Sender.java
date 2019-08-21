package com.socket.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 发送者
 */
public interface Sender extends Closeable {

    boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException;
}
