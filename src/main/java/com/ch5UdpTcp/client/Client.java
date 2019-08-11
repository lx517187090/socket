package com.ch5UdpTcp.client;

import com.ch5UdpTcp.client.bean.ServerInfo;

public class Client {
    public static void main(String[] args) {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("server :" + info);
    }
}
