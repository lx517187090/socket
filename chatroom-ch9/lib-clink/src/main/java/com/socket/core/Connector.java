package com.socket.core;

import com.socket.box.StringReceivePacket;
import com.socket.box.StringSendPacket;
import com.socket.impl.SocketChannelAdapter;
import com.socket.impl.async.AsyncReceiveDispatcher;
import com.socket.impl.async.AsyncSendDispatcher;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * 代表一个连接
 */
public class Connector implements Closeable, SocketChannelAdapter.OnChannelStatusListener {

    //当前连接唯一性
    private UUID key = UUID.randomUUID();

    //socketChannel
    private SocketChannel channel;

    //发送者（封装SocketChannel）
    private Sender sender;

    //接收者
    private Receive receiver;

    /**
     * 发送数据调度
     */
    private SendDispatcher sendDispatcher;

    /**
     * 接收数据调度
     */
    private ReceiveDispatcher receiveDispatcher;

    /**
     * 初始化
     *
     * @param channel
     */
    public void setup(SocketChannel channel) throws IOException {
        this.channel = channel;
        IoContext context = IoContext.get();
        SocketChannelAdapter adapter = new SocketChannelAdapter(channel, context.getIoProvider(), this);
        this.sender = adapter;
        this.receiver = adapter;
        //readNextMessage();

        sendDispatcher = new AsyncSendDispatcher(sender);

        receiveDispatcher = new AsyncReceiveDispatcher(receiver, receivePacketCallback);
        receiveDispatcher.start();
    }

    public void send(String msg) {
        StringSendPacket sendPacket = new StringSendPacket(msg);
        sendDispatcher.send(sendPacket);
    }


    /**
     * 开始读取数据
     */
   /* private void readNextMessage() {
        if (receiver != null) {
            try {
                receiveDispatcher.start();
            } catch (IOException e) {
                System.out.println("接收数据异常" + e.getMessage());
            }
        }
    }*/

    @Override
    public void close() throws IOException {
        receiveDispatcher.close();
        sendDispatcher.close();
        sender.close();
        receiver.close();
        channel.close();
    }

    @Override
    public void onChannelClose(SocketChannel channel) {

    }

    /*private IoArgs.IoArgsEventListener echoReceiveListener = new IoArgs.IoArgsEventListener() {
        @Override
        public void onStarted(IoArgs args) {

        }

        @Override
        public void onCompleted(IoArgs args) {
            onReceiveNewMessage(args.bufferString());
            //读取下一条数据
            readNextMessage();
        }
    };*/

    protected void onReceiveNewMessage(String str) {
        System.out.println(key.toString() + ": " + str);
    }


    /**
     * 接收消息回调
     */
    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = packet -> {
        if (packet instanceof StringReceivePacket){
            String msg = ((StringReceivePacket) packet).string();
            //发送消息
            onReceiveNewMessage(msg);
        }
    };
}
