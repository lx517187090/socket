package com.socket.core;

/**
 * 发送包
 */
public abstract class SendPacket extends Packet{

    private boolean isCanceled;
    public abstract byte [] bytes();

    /**
     * 是否已取消
     */
    public boolean isCanceled(){
        return isCanceled;
    }
}
