package com.socket.impl.async;

import com.socket.core.*;
import com.socket.utils.CloseUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsynReceiveDispatcher implements ReceiveDispatcher {

    /**
     * 是否被关闭
     */
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    private final Receive receive;

    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();

    private ReceivePacket packetTemp;

    private byte[] buffer;

    private int total;

    private int position;

    public AsynReceiveDispatcher(Receive receive, ReceivePacketCallback callback) {
        this.receive = receive;
        this.receive.setReceiveListener(ioArgsEventListener);
        this.callback = callback;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void close() throws IOException {

    }

    private IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {

        }
    };
}
