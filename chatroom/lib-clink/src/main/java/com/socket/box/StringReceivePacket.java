package com.socket.box;

import com.socket.core.ReceivePacket;

import java.io.IOException;

/**
 * 接收String
 * Byte数组--->String
 */
public class StringReceivePacket extends ReceivePacket {

    /**
     * 真实装载数据buffer
     */
    private byte[] buffer;

    /**
     * 坐标
     */
    private int position;

    public StringReceivePacket(int len) {
        buffer = new byte[len];
        length = len;
    }

    @Override
    public void save(byte[] bytes, int count) {
        System.arraycopy(bytes, 0, buffer, position, count);
        position += count;
    }


    public String string(){
        return new String(buffer);
    }

    @Override
    public void close() throws IOException {

    }
}
