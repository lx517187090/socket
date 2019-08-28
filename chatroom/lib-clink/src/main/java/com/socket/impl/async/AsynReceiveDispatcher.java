package com.socket.impl.async;

import com.socket.box.StringReceivePacket;
import com.socket.core.*;
import com.socket.utils.CloseUtils;
import com.sun.org.apache.xpath.internal.operations.String;

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
        registerReceive();
    }

    /**
     * 解析数据
     */
    private void registerReceive() {
        try {
            receive.receiveAsync(ioArgs);
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void stop() {

    }


    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ReceivePacket packet = this.packetTemp;
            if (packet != null) {
                packet = null;
                CloseUtils.close(packet);
            }
        }
    }

    private IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {
            //可以接收数据的大小
            int receiveSize;
            if (packetTemp == null) {
                receiveSize = 4;
            } else {
                receiveSize = Math.min(total - position, args.capacity());
            }
            args.limit(receiveSize);
        }


        /**
         * 数据到达
         */
        @Override
        public void onCompleted(IoArgs args) {
            assemblePacket(args);
            //继续接收下一条数据
            registerReceive();
        }
    };

    /**
     * 解析数据到packet
     */
    private void assemblePacket(IoArgs args) {
        if (packetTemp == null) {
            int length = args.readLenth();
            packetTemp = new StringReceivePacket(length);
            buffer = new byte[length];
            total = length;
            position = 0;
        }
        int count = args.writeTo(buffer, 0);
        if (count > 0) {
            packetTemp.save(buffer, count);
            position += count;
            if (position == total) {
                completePacket();
                packetTemp = null;
            }
        }
    }

    /**
     * 完成数据接收操作
     */
    private void completePacket() {
        ReceivePacket packet = this.packetTemp;
        CloseUtils.close(packet);
        callback.onReceivePacketCompleted(packet);
    }
}
