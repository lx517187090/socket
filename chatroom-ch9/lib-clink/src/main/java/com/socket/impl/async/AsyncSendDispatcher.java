package com.socket.impl.async;

import com.socket.core.IoArgs;
import com.socket.core.SendDispatcher;
import com.socket.core.SendPacket;
import com.socket.core.Sender;
import com.socket.utils.CloseUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步发送数据调度封装
 */
public class AsyncSendDispatcher implements SendDispatcher {

    private final Sender sender;

    private final Queue<SendPacket> queue = new ConcurrentLinkedQueue<>();

    /**
     * 是否被关闭
     */
    private final AtomicBoolean isClosed = new AtomicBoolean();

    private IoArgs ioArgs = new IoArgs();

    private SendPacket packetTemp;

    /**
     * 当前packet最大值
     */
    private int total;


    /**
     * 当前packet发送进度
     */
    private int position;

    /**
     * 是否发送中
     */
    private final AtomicBoolean isSending = new AtomicBoolean();

    public AsyncSendDispatcher(Sender sender) {
        this.sender = sender;
    }

    /**
     * 发送数据
     *
     * @param sendPacket 数据
     */
    @Override
    public void send(SendPacket sendPacket) {
        queue.offer(sendPacket);
        if (isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }


    private SendPacket tackPacket() {
        SendPacket packet = queue.poll();
        if (packet != null && packet.isCanceled()) {
            return tackPacket();
        }
        return packet;
    }

    private void sendNextPacket() {
        SendPacket temp = this.packetTemp;
        if (temp != null) {
            CloseUtils.close(temp);
        }
        SendPacket packet = this.packetTemp = tackPacket();
        if (packet == null) {
            //队列为空 取消发送状态
            isSending.set(false);
            return;
        }
        total = packet.length();
        position = 0;
        sendCurrentPacket();

    }

    /**
     * 真实发送packet
     */
    private void sendCurrentPacket() {
        IoArgs ioArgs = this.ioArgs;

        //开始   清理
        ioArgs.startWriting();

        if (position >= total) {
            sendNextPacket();
            return;
        } else if (position == 0) {
            //首包 需要携带长度信息
            ioArgs.writeLength(total);
        }
        byte[] bytes = packetTemp.bytes();
        //把数据写入ioArgs
        int count = ioArgs.readFrom(bytes, position);
        position += count;

        //完成封装
        ioArgs.finishWriting();

        try {
            sender.sendAsync(ioArgs, ioArgsEventListener);
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            isSending.set(false);
            SendPacket packet = this.packetTemp;
            if (packet != null) {
                packetTemp = null;
                CloseUtils.close(packet);
            }
        }
    }

    private final IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            //继续发送当前包
            sendCurrentPacket();
        }
    };

    @Override
    public void cancel(SendPacket sendPacket) {

    }


}
