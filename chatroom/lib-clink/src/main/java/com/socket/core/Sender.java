package com.socket.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 发送者
 */
public interface Sender extends Closeable {

    /**
     *  异步发送
     * @param args 数据
     * @param listener 发送状态 回调
     * @return
     * @throws IOException
     */
    boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException;
}
