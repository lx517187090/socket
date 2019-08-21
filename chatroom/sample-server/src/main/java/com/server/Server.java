package com.server;

import com.constants.TCPConstants;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Server {

    /*public static void main(String[] args) {
        ServerProvider.start(TCPConstants.PORT_SERVER);
        try {
            System.in.read();
        }catch (IOException e){
            e.printStackTrace();
        }
        ServerProvider.stop();
    }*/

    /*public static void main(String[] args) {
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSuccess = tcpServer.start();
        if (!isSuccess){
            System.out.println("start TCP server failed!");
            return;
        }
        UDPProvider.start(TCPConstants.PORT_SERVER);
        try {
            System.in.read();
        }catch (IOException e){
            e.printStackTrace();
        }
        UDPProvider.stop();
        tcpServer.stop();
    }*/

    public static void main(String[] args) throws IOException {
        TCPServer tcpServer = new TCPServer(TCPConstants.PORT_SERVER);
        boolean isSuccess = tcpServer.start();
        if (!isSuccess){
            System.out.println("start TCP server failed!");
            return;
        }
        ServerProvider.start(TCPConstants.PORT_SERVER);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String str ;
        do {
            str = bufferedReader.readLine();
            tcpServer.broadcast(str);
        } while (!"0000".equals(str));
        UDPProvider.stop();
        tcpServer.stop();
    }
}
