package com.socket.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 公共数据的封装
 * 提供了类型以及基本的长度的定义
 */
public abstract class Packet implements Closeable {

    /**
     * 发送数据的类型
     */
    protected byte type;

    /**
     * 发送数据的长度
     */
    protected int length;


    public byte type(){
        return type;
    }

    public int length(){
        return length;
    }
}
