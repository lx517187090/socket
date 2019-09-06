package com.socket.core;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO输入输出参数
 */
public class IoArgs {

    private int limit = 256;

    //分配byte数组
    private byte[] byteBuff = new byte[256];

    private ByteBuffer buffer = ByteBuffer.wrap(byteBuff);

    /**
     * 从bytes写入ioArgs
     */
    public int readFrom(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.put(bytes, offset, size);
        return size;
    }

    /**
     * 从ioArg读取数据到byte数组
     */
    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
    }

    /**
     * 从channel写入ioArgs
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        //本次真实产生数据
        int bytesProduced = 0;

        while (buffer.hasRemaining()) {
            int read = channel.read(buffer);
            if (read < 0) {
                throw new IOException();
            }
            bytesProduced += read;
        }
        finishWriting();
        return bytesProduced;
    }

    /**
     * 从ioArgs写入channel
     */
    public int writeTo(SocketChannel channel) throws IOException {
        //本次真实产生数据
        int bytesProduced = 0;

        while (buffer.hasRemaining()) {
            int read = channel.write(buffer);
            if (read < 0) {
                throw new IOException();
            }
            bytesProduced += read;
        }
        return bytesProduced;
    }


    /**
     * 开始写入数据调用
     */
    public void startWriting() {
        buffer.clear();
        buffer.limit(limit);
    }

    /**
     * 写入完成操作
     */
    public void finishWriting() {
        buffer.flip();
    }

    /**
     * 设置单次写操作的容纳区间
     */
    public void limit(int limit) {
        this.limit = limit;
    }

    /**
     * 写入长度信息
     * @param total 长度
     */
    public void writeLength(int total) {
        buffer.putInt(total);
    }

    /**
     *  获取长度信息
     */
    public int readLength(){
        return buffer.getInt();
    }

    /**
     *  返回buffer容量
     */
    public int capacity() {
        return buffer.capacity();
    }

    /* *//**
     * 读数据  socketBuff ---> channel
     *//*
    public int read(SocketChannel channel) throws IOException {
        buffer.clear();
        return channel.read(buffer);
    }

    *//**
     * 写数据  channel ---> buffer
     *//*
    public int write(SocketChannel channel) throws IOException {
        return channel.read(buffer);
    }

    */

    /**
     * 丢弃换行符
     *//*
    public String bufferString() {
        return new String(byteBuff, 0, buffer.position() - 1);
    }*/




    public interface IoArgsEventListener {
        /**
         * 开始回调
         */
        void onStarted(IoArgs args);

        /**
         * 消息发送完毕回调
         */
        void onCompleted(IoArgs args);
    }
}
