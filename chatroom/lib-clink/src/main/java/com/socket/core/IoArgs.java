package com.socket.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO输入输出参数
 */
public class IoArgs {
    //分配byte数组
    private byte[] byteBuff = new byte[256];

    private ByteBuffer buffer = ByteBuffer.wrap(byteBuff);

    /**
     * 读数据  socketBuff ---> channel
     */
    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    /**
     *  写数据  channel ---> buffer
     */
    public int write(SocketChannel channel) throws IOException {
        return channel.read(buffer);
    }

    /**
     * 丢弃换行符
     */
    public String bufferString(){
        return new String(byteBuff, 0, buffer.position() - 1);
    }

    public interface IoArgsEventListener{
        void onStarted(IoArgs args);

        void onCompleted(IoArgs args);
    }
}
