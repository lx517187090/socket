package com.socket.core;

import java.nio.channels.SocketChannel;
import java.util.UUID;

/**
 * 代表一个连接
 */
public class Connector {
    //当前连接唯一性
    private UUID key =  UUID.randomUUID();
    //socketChannel
    private SocketChannel channel;
    //发送者（封装SocketChannel）
    private Sender sender;
    //接收者
    private Receive receiver;

    /**
     * 初始化
     * @param channel
     */
    public void setup(SocketChannel channel){
        this.channel = channel;
    }
}
