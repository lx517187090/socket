package com.socket.impl;

import com.socket.core.IoArgs;
import com.socket.core.IoProvider;
import com.socket.core.Receive;
import com.socket.core.Sender;
import com.socket.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 发送与接收 实现类
 */
public class SocketChannelAdapter implements Sender, Receive, Cloneable {

    /**
     * /是否关闭
     */
    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * /具体发送者**
     */
    private final SocketChannel channel;

    /**
     * 服务提供者
     */
    private final IoProvider ioProvider;

    /**
     * channel关闭回调
     */
    private final OnChannelStatusListener listener;

    /**
     * 发送回调
     */
    private IoArgs.IoArgsEventListener receiverArgsEventListener;

    /**
     * 接受回调
     */
    private IoArgs.IoArgsEventListener senderArgsEventListener;


    private IoArgs receiveArgsTemp;

    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider,
                                OnChannelStatusListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;
        channel.configureBlocking(false);
    }

    @Override
    public boolean receiveAsync(IoArgs args) throws IOException {
        if (isClosed.get()) {
            throw new IOException("current channel is closed");
        }
        receiveArgsTemp = args;
        return ioProvider.registerInput(channel, inputCallback);
    }

    @Override
    public void setReceiveListener(IoArgs.IoArgsEventListener listener) {
        receiverArgsEventListener = listener;
    }

    /**
     * 发送实现
     *
     * @param args     数据
     * @param listener 发送状态 回调
     */
    @Override
    public boolean sendAsync(IoArgs args, IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("current channel is closed");
        }
        senderArgsEventListener = listener;
        //把当前发送数据附加到回调中
        outputCallback.setAttach(args);
        return ioProvider.registerOutput(channel, outputCallback);
    }

    @Override
    public void close() {
        if (isClosed.compareAndSet(false, true)) {
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            CloseUtils.close(channel);
            listener.onChannelClose(channel);
        }
    }

    /**
     * 接收回调实现
     */
    private final IoProvider.HandlerInputCallback inputCallback = new IoProvider.HandlerInputCallback() {

        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }
            IoArgs args = receiveArgsTemp;
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiverArgsEventListener;

            listener.onStarted(args);
            //具体的读取操作
            try {
                //读取
                if (args.readFrom(channel) > 0) {
                    //读取完成回调
                    listener.onCompleted(args);
                } else {
                    throw new IOException("cannot read any data");
                }
            } catch (IOException e) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    /**
     * 发送回调实现
     */
    private final IoProvider.HandlerOutputCallback outputCallback = new IoProvider.HandlerOutputCallback() {
        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get()) {
                return;
            }

            IoArgs args = getAttach();
            IoArgs.IoArgsEventListener listener = senderArgsEventListener;
            listener.onStarted(args);
            //具体的读取操作
            try {
                //读取
                if (args.writeTo(channel) > 0) {
                    //读取完成回调
                    listener.onCompleted(args);
                } else {
                    throw new IOException("cannot write any data");
                }
            } catch (IOException e) {
                CloseUtils.close(SocketChannelAdapter.this);
            }
        }
    };

    /**
     * 当前channel关闭回调
     */
    public interface OnChannelStatusListener {
        void onChannelClose(SocketChannel channel);
    }
}
