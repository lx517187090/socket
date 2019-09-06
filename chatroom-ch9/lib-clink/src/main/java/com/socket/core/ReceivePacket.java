package com.socket.core;

/**
 * 接收包定义    packet
 */
public abstract class ReceivePacket extends Packet{

    /**
     * 保存消息
     * @param bytes 消息
     * @param count 保存消息长度
     */
    public abstract void save(byte [] bytes, int count);
}
