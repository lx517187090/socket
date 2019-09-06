package com.socket.box;

import com.socket.core.ReceivePacket;

import java.io.IOException;

/**
 * 字符串接收packet
 */
public class StringReceivePacket extends ReceivePacket {

    /**
     * 装载数据buffer
     */
    private byte[] buffer;


    /**
     * 存放数据坐标
     */
    private int position;

    public StringReceivePacket(int len){
        buffer = new byte[len];
        length = len;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes, 0, buffer, position, count);
        position += count;
    }

    public  String string(){
        return new String(buffer);
    }

    @Override
    public void close() throws IOException {

    }
}
