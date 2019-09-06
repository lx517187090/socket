package com.socket.core;

import java.io.InputStream;

/**
 * 发送包
 */
public abstract class SendPacket<T extends InputStream> extends Packet<T> {

    private boolean isCanceled;

    //public abstract byte [] bytes();

    /**
     * 是否已取消
     */
    public boolean isCanceled() {
        return isCanceled;
    }

}
