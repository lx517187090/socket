package com.ch5UdpTcp.server;

import com.ch5UdpTcp.server.handle.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPServer {
    private final int port;
    private ClientListener mListener;
    private List<ClientHandler> clientHandlerList = new ArrayList<>();

    TCPServer(int port){
        this.port = port;
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    void stop() {
        if (mListener != null){
            mListener.exit();
        }
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.exit();
        }
        clientHandlerList.clear();
    }

    void broadcast(String str) {
        for (ClientHandler clientHandler : clientHandlerList) {
            clientHandler.send(str);
        }
    }

    private class ClientListener extends Thread{
        private ServerSocket serverSocket;
        private boolean done = false;
        ClientListener(int port) throws IOException {
            serverSocket = new ServerSocket(port);
        }
        @Override
        public void run() {
            super.run();
            System.out.println("server start");
            do {
                Socket socket;
                try {
                    socket = serverSocket.accept();
                }catch (IOException e){
                    continue;
                }
                try {
                    ClientHandler clientHandler = new ClientHandler(socket,
                            handler -> clientHandlerList.remove(handler));
                    //读取数据并打印
                    clientHandler.readToPrint();
                    clientHandlerList.add(clientHandler);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("客户端连接异常" + e.getMessage());
                }
            }while (!done);
            System.out.println("server is stop...");
        }

        void exit(){
            done = true;
            try {
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

}
