package com.socket.impl;

import com.socket.core.IoArgs;
import com.socket.core.IoProvider;
import com.socket.core.Receive;
import com.socket.core.Sender;
import com.socket.utils.CloseUtils;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicBoolean;

public class SocketChannelAdapter implements Sender, Receive, Cloneable {
    //是否关闭
    private final AtomicBoolean isClosed = new AtomicBoolean(false);
    private final SocketChannel channel;
    private final IoProvider ioProvider;
    private final OnChannelStatusListener listener;

    private IoArgs.IoArgsEventListener receiverArgsEventListener;
    private IoArgs.IoArgsEventListener senderArgsEventListener;

    public SocketChannelAdapter(SocketChannel channel, IoProvider ioProvider,
                                OnChannelStatusListener listener) throws IOException {
        this.channel = channel;
        this.ioProvider = ioProvider;
        this.listener = listener;
        channel.configureBlocking(false);
    }

    @Override
    public boolean receiveAsync(IoArgs.IoArgsEventListener listener) throws IOException {
        if (isClosed.get()) {
            throw new IOException("current channel is closed");
        }
        receiverArgsEventListener = listener;

        return ioProvider.registerInput(channel, inputCallback);
    }

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
    public void close() throws IOException {
        if (isClosed.compareAndSet(false, true)) {
            ioProvider.unRegisterInput(channel);
            ioProvider.unRegisterOutput(channel);
            CloseUtils.close(channel);
            listener.onChannelClose(channel);
        }
    }

    private final IoProvider.HandlerInputCallback inputCallback = new IoProvider.HandlerInputCallback() {
        @Override
        protected void canProviderInput() {
            if (isClosed.get()) {
                return;
            }
            IoArgs args = new IoArgs();
            IoArgs.IoArgsEventListener listener = SocketChannelAdapter.this.receiverArgsEventListener;
            if (listener != null) {
                listener.onStarted(args);
            }
            //具体的读取操作
            try {
                //读取
                if (args.read(channel) > 0 && listener != null) {
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

    private final IoProvider.HandlerOutputCallback outputCallback = new IoProvider.HandlerOutputCallback() {
        @Override
        protected void canProviderOutput(Object attach) {
            if (isClosed.get()) {
                return;
            }
            //TODO
            senderArgsEventListener.onCompleted(null);
        }
    };

    public interface OnChannelStatusListener {
        void onChannelClose(SocketChannel channel);
    }
}
