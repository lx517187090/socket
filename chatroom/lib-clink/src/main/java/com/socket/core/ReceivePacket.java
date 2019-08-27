package com.socket.core;

/**
 * 接收包的定义
 */
public abstract class ReceivePacket extends Packet{

    /**
     * 接收数据
     * @param bytes byte数组
     * @param count 接收数据长度
     */
    public abstract void save(byte[] bytes, int count);



}
