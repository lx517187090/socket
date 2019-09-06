package com.socket.core;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * IO输入输出参数
 */
public class IoArgs {

    public int capacity;

    private int limit = 256;

    //分配byte数组
    private byte[] byteBuff = new byte[256];

    private ByteBuffer buffer = ByteBuffer.wrap(byteBuff);

    /**
     * 从socketChannel中读数据  socketBuff ---> channel
     */
    public int readFrom(SocketChannel channel) throws IOException {
        startWriting();
        //真实生产的数据
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.read(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        finishWriting();
        return bytesProduced;
    }

    /**
     * 开始写入数据到IoArgs
     */
    public void startWriting() {
        buffer.clear();
        //定义容纳区间
        buffer.limit();
    }

    /**
     * 写完成后调用
     */
    public void finishWriting() {
        buffer.flip();
    }

    /**
     * 设置单次写操作的容纳区间
     *
     * @param limit 区间大小
     */
    public void limit(int limit) {
        this.limit = limit;
    }

    public int capacity() {
        return capacity;
    }

    /**
     * 写数据到socketChannel  channel ---> buffer
     */
    public int writeTo(SocketChannel channel) throws IOException {
        //真实生产的数据
        int bytesProduced = 0;
        while (buffer.hasRemaining()) {
            int len = channel.write(buffer);
            if (len < 0) {
                throw new EOFException();
            }
            bytesProduced += len;
        }
        return bytesProduced;
    }

    /**
     * 读取操作  从byte数组中
     *
     * @param bytes  数组
     * @param offset 位移
     */
    public int readFrom(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.put(bytes, offset, size);
        return size;
    }

    /**
     * 写数据到byte数组
     *
     * @param bytes  数组
     * @param offset 位移
     */
    public int writeTo(byte[] bytes, int offset) {
        int size = Math.min(bytes.length - offset, buffer.remaining());
        buffer.get(bytes, offset, size);
        return size;
    }

    /**
     * 写入长度
     */
    public void writeLength(int total) {
        buffer.putInt(total);
    }

    /**
     * 读取长度
     */
    public int readLenth() {
        return buffer.getInt();
    }

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
