package com.server;


import com.server.handle.ClientHandler;
import com.socket.utils.CloseUtils;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TCPServer {
    private final int port;
    private ClientListener listener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();
    private Selector selector;
    private ServerSocketChannel server;

    TCPServer(int port){
        this.port = port;
    }

    public boolean start() {
        try {
            this.selector = Selector.open();
            this.server = ServerSocketChannel.open();
            //设置之为非阻塞
            server.configureBlocking(false);
            //绑定到本地本地端口
            server.bind(new InetSocketAddress(port));
            //注册客户端连接到达监听
            server.register(selector, SelectionKey.OP_CONNECT);
            ClientListener listener = new ClientListener();
            this.listener = listener;
            listener.start();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void stop() {
        if (listener != null){
            listener.exit();
        }
        CloseUtils.close(server);
        CloseUtils.close(selector);
        synchronized (TCPServer.this){
            for (ClientHandler clientHandler : clientHandlerList) {
                clientHandler.exit();
            }
            clientHandlerList.clear();
        }
    }

    void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    private class ClientListener extends Thread{
        private boolean done = false;

        @Override
        public void run() {
            super.run();
            Selector selector = TCPServer.this.selector;
            System.out.println("server start");
            do {
                SocketChannel socketChannel;
                try {
                    if(selector.select() == 0){
                        if (done){
                            break;
                        }
                        continue;
                    }
                    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                    while (it.hasNext()){
                        if (done){
                            break;
                        }
                        SelectionKey key = it.next();
                        it.remove();
                        //检查当前key的状态是否是我们关注的状态
                        //客户端到达状态
                        if (key.isAcceptable()){
                            ServerSocketChannel channel = (ServerSocketChannel) key.channel();
                            //非阻塞拿到客户端
                            socketChannel = channel.accept();
                            try {
                                ClientHandler clientHandler = new ClientHandler(socketChannel,
                                        handler -> clientHandlerList.remove(handler));
                                //读取数据并打印
                                clientHandler.readToPrint();
                                synchronized (TCPServer.this){
                                    clientHandlerList.add(clientHandler);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("客户端连接异常" + e.getMessage());
                            }
                        }
                    }
                    //socket = serverSocket.accept();
                }catch (IOException e){
                    e.printStackTrace();
                }
                /*try {
                    ClientHandler clientHandler = new ClientHandler(socket,
                            handler -> clientHandlerList.remove(handler));
                    //读取数据并打印
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常" + e.getMessage());
                }*/
            }while (!done);
            System.out.println("server is stop...");
        }

        void exit(){
            done = true;
            //唤醒当前阻塞
            selector.wakeup();
        }
    }

}
