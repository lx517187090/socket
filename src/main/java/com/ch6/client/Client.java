package com.ch6.client;

import com.ch6.client.bean.ServerInfo;

public class Client {
    public static void main(String[] args) {
        ServerInfo info = ClientSearcher.searchServer(10000);
        System.out.println("server :" + info);
        try {
            TCPClient.linkWith(info);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
