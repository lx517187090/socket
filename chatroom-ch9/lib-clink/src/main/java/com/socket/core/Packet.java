package com.socket.core;

import java.io.Closeable;

/**
 * 公共数据封装
 * 提供了类型以及基本长度定义
 */
public abstract class Packet  implements Closeable {

    /**
     * packet类型   字符串 图片
     */
    protected byte type;

    /**
     * packet长度
     */
    protected int length;

    public byte type(){
        return type;
    }

    public int length(){
        return length;
    }
}
