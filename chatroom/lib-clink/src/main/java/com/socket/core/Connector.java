package com.socket.core;

import com.socket.box.StringReceivePacket;
import com.socket.box.StringSendPacket;

import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * 代表一个连接
 */
public class Connector {
    //当前连接唯一性
    private UUID key = UUID.randomUUID();
    //socketChannel
    private SocketChannel channel;
    //发送者（封装SocketChannel）
    private Sender sender;
    //接收者
    private Receive receiver;

    private SendDispatcher sendDispatcher;

    private ReceiveDispatcher receiveDispatcher;
    /**
     * 初始化
     *
     * @param channel
     */
    public void setup(SocketChannel channel) {
        this.channel = channel;
    }

    /**
     * 发送者调度
     * @param msg
     */
    public void send(String msg) {
        SendPacket packet = new StringSendPacket(msg);
        sendDispatcher.send(packet);
    }

    protected void onReceiveNewMessage(String msg){
        System.out.println(key.toString() + msg);
    }

    private ReceiveDispatcher.ReceivePacketCallback receivePacketCallback = packet -> {
        if (packet instanceof StringReceivePacket){
            String msg = ((StringReceivePacket) packet).string();
            onReceiveNewMessage(msg);
        }
    };
}
