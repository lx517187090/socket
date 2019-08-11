package com.ch5UdpTcp.server;

import com.ch5UdpTcp.constants.TCPConstants;

import java.io.IOException;

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

    public static void main(String[] args) {
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
    }
}
