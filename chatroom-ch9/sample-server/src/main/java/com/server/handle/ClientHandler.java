package com.server.handle;

import com.socket.core.Connector;
import com.socket.utils.CloseUtils;

import java.io.*;
import java.nio.channels.SocketChannel;

public class ClientHandler extends Connector {

    private final ClientHandleCallback clientHandleCallback;

    private String clintInfo;

    public ClientHandler(SocketChannel socketChannel, ClientHandleCallback clientHandleCallback) throws IOException {
        this.clientHandleCallback = clientHandleCallback;
        this.clintInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("客户端连接");
        setup(socketChannel);
    }

    public void exit() {
        CloseUtils.close(this);
        System.out.println("客户端已退出" + clintInfo);
    }

    public void exitBySelf() {
        exit();
        clientHandleCallback.onSelfClosed(this);
    }

    @Override
    public void onChannelClose(SocketChannel channel) {
        super.onChannelClose(channel);
        exitBySelf();
    }

    public interface ClientHandleCallback {
        //自身关闭通知
        void onSelfClosed(ClientHandler handler);

        void onMessageArrived(ClientHandler handler, String msg);
    }

    @Override
    protected void onReceiveNewMessage(String str) {
        super.onReceiveNewMessage(str);
        clientHandleCallback.onMessageArrived(this, str);
    }
}
