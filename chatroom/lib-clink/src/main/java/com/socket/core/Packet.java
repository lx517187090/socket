package com.socket.core;

import java.io.Closeable;
import java.io.IOException;

/**
 * 公共数据的封装
 * 提供了类型以及基本的长度的定义
 */
public abstract class Packet<T extends Closeable> implements Closeable {

    private T stream;

    /**
     * 发送数据的类型
     */
    protected byte type;

    /**
     * 发送数据的长度
     */
    protected long length;


    public byte type(){
        return type;
    }

    public long length(){
        return length;
    }


    protected abstract T createStream();

    protected void closeStream(T string)throws IOException {
        stream.close();
    }

    public final T open() {
        if (stream == null) {
            stream = createStream();
        }
        return stream;
    }

    @Override
    public final void close() throws IOException {
        if (stream != null) {
            closeStream(stream);
            stream = null;
        }
    }
}
