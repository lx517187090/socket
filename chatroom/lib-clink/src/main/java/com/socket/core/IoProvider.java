package com.socket.core;

import java.io.Closeable;
import java.nio.channels.SocketChannel;

/**
 * 提供者
 */
public interface IoProvider extends Closeable {

    /**
     * 注册输入
     *
     * @param channel  从channel中异步读取数据
     * @param callback 当channel可读时回调
     * @return zz
     */
    boolean registerInput(SocketChannel channel, HandlerInputCallback callback);

    /**
     * 注册输出
     */
    boolean registerOutput(SocketChannel channel, HandlerOutputCallback callback);

    /**
     * 取消输入注册
     *
     * @param channel channel
     */
    void unRegisterInput(SocketChannel channel);

    /**
     * 取消输出注册
     *
     * @param channel channel
     */
    void unRegisterOutput(SocketChannel channel);

    /**
     * 输入回调
     */
    abstract class HandlerInputCallback implements Runnable {
        @Override
        public void run() {
            canProviderInput();
        }

        /**
         * 当前能够提供输入
         */
        protected abstract void canProviderInput();
    }

    /**
     * 输出回调
     */
    abstract class HandlerOutputCallback implements Runnable {

        private Object attach;

        @Override
        public final void run() {
            canProviderOutput(attach);
        }

        protected abstract void canProviderOutput(Object attach);

        public final <T> T getAttach() {
            T attach = (T)this.attach;
            return attach;
        }

        public void setAttach(IoArgs args) {
            this.attach = args;
        }
    }
}
