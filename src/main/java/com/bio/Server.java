package com.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * BIO服务端编码
 */
@Slf4j
public class Server {

    private static final int DEFAULT_PORT = 7777;

    //单例的ServerSocket
    private static ServerSocket serverSocket;

    //
    public static void start() throws IOException{
        //
        start(DEFAULT_PORT);
    }

    public synchronized static void start(int port) throws IOException{
        if (serverSocket != null){
            return;
        }
        try {
            serverSocket = new ServerSocket(port);
            log.info("服务端启动 端口号: port {}", port);
            while (true){
                Socket socket = serverSocket.accept();
                new Thread(new ServerHandler(socket)).start();
            }
        }finally {
            if (serverSocket != null){
                log.info("服务端已关闭， 端口号 {}", port);
                serverSocket.close();
            }
        }
    }

    public static void main(String[] args) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Server.start();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
