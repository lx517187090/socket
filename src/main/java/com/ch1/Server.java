package com.ch1;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;
    public Server(int port){
        try {
            this.serverSocket = new ServerSocket(port);
            System.out.println("服务端启动成功 端口: " + port);
        }catch (IOException e){
            System.out.println("服务端启动失败");
        }
    }

    public void start(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                doStart();
            }
        }).start();
    }

    private void doStart() {
        while (true){
            try {
                Socket client = serverSocket.accept();
                new ClientHandler(client).start();
            }catch (Exception e){
                System.out.println("服务端异常");
            }
        }
    }
}
