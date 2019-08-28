package com.socket.impl.async;

import com.socket.core.*;
import com.socket.utils.CloseUtils;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsynSendDispatcher implements SendDispatcher {
    /**
     * 是否被关闭
     */
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    /**
     * 发送者
     */
    private final Sender sender;

    /**
     * 非阻塞线程安全队列
     */
    private final Queue<SendPacket> queue = new ConcurrentLinkedDeque<>();

    /**
     * 发送状态
     */
    private AtomicBoolean isSending = new AtomicBoolean();

    private IoArgs ioArgs = new IoArgs();
    /**
     * 当前发送数据
     */
    private SendPacket packetTemp;

    /**
     * 当前packet最大值
     */
    private int total;

    /**
     * 当前packet发送进度
     */
    private int position;

    public AsynSendDispatcher(Sender sender) {
        this.sender = sender;
    }

    @Override
    public void send(SendPacket packet) {
        queue.offer(packet);
        if (isSending.compareAndSet(false, true)) {
            sendNextPacket();
        }
    }

    private SendPacket tackPacket() {
        SendPacket packet = queue.poll();
        if (packet != null && packet.isCanceled()) {
            //已取消不用发送
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
            //队里额为空 取消发送状态
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
        IoArgs args = this.ioArgs;
        //开始清理
        args.startWriting();
        //完全发送完数据，发送下一个
        if (position >= total) {
            sendNextPacket();
            return;
        } else if (position == 0) {
            //首包 需要携带长度信息
            args.writeLength(total);
        }

        byte[] bytes = packetTemp.open();
        //数据写入args中
        int count = args.readFrom(bytes, position);
        position += count;

        //完成封装
        args.finishWriting();

        try {
            sender.sendAsync(args, ioArgsEventListener);
        } catch (IOException e) {
            closeAndNotify();
        }
    }

    /**
     * 关闭自己并通知外层
     */
    private void closeAndNotify() {
        CloseUtils.close(this);
    }

    @Override
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            SendPacket packet = this.packetTemp;
            if (packet != null) {
                packet = null;
                CloseUtils.close(packet);
            }
        }
    }

    @Override
    public void cancel(SendPacket packet) {

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

}
