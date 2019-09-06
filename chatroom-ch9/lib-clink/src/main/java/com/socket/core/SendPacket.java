package com.socket.core;

/**
 * 发送packet
 */
public abstract class SendPacket extends Packet{

    private boolean isCanceled;
    /**
     * 发送内容
     */
    public abstract byte[] bytes();

    /**
     * 是否已取消
     */
    public boolean isCanceled(){
        return isCanceled;
    }
}
