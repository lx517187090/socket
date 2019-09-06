package com.socket.impl.async;

import com.socket.box.StringReceivePacket;
import com.socket.core.*;
import com.socket.utils.CloseUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 异步接收数据调度封装
 */
public class AsyncReceiveDispatcher implements ReceiveDispatcher {


    /**
     * 是否被关闭
     */
    private final AtomicBoolean isClosed = new AtomicBoolean(false);


    /**
     * 接收者
     */
    private final Receive receive;

    private final ReceivePacketCallback callback;

    private IoArgs ioArgs = new IoArgs();

    private ReceivePacket packetTemp;

    /**
     * 接收buffer    在写入pocket
     */
    private byte[] buffer;

    /**
     * packet最大值
     */
    private int total;

    /**
     * 接受位置
     */
    private int position;

    public AsyncReceiveDispatcher(Receive receive, ReceivePacketCallback callback) {
        this.receive = receive;
        this.receive.setReceiveListener(ioArgsEventListener);
        this.callback = callback;
    }

    /**
     * 开始接收
     */
    @Override
    public void start() {
        registerReceive();
    }

    /**
     * 注册接收数据
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
            if (packetTemp != null){
                packetTemp = null;
                CloseUtils.close(packet);
            }
        }
    }


    private IoArgs.IoArgsEventListener ioArgsEventListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {
            int receiveSize;
            if (packetTemp == null) {
                //接收头部长度信息
                receiveSize = 4;
            } else {
                receiveSize = Math.min(total - position, args.capacity());
            }
            //设置本次接收数据大小
            args.limit(receiveSize);
        }

        @Override
        public void onCompleted(IoArgs args) {
            //解析数据
            assemblePacket();
            //继续接收下一条数据
            registerReceive();
        }
    };

    /**
     * 解析数据到packet
     */
    private void assemblePacket() {
        if (packetTemp == null) { //解析长度
            int length = ioArgs.readLength();
            packetTemp = new StringReceivePacket(length);
            buffer = new byte[length];
            total = length;
            position = 0;
        }
        int count = ioArgs.writeTo(buffer, 0);
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
     * 完成packet
     */
    private void completePacket() {
        ReceivePacket packetTemp = this.packetTemp;
        CloseUtils.close(packetTemp);
        callback.onReceivePacketCompleted(packetTemp);
    }
}
