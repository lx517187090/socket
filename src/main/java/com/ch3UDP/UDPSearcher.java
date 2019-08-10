package com.ch3UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UDPSearcher {

    public static void main(String[] args) throws IOException {
        System.out.println("udp UDPSearcher start");

        //作为接收者 制定一个端口用于接收数据
        DatagramSocket ds = new DatagramSocket();

        String requestData = "hello world";
        DatagramPacket requestPocket = new DatagramPacket(requestData.getBytes(), requestData.length());
        requestPocket.setAddress(InetAddress.getLocalHost());
        requestPocket.setPort(20000);
        ds.send(requestPocket);
        //构建接收实体
        final byte[] buf= new byte[512];
        DatagramPacket receivePack = new DatagramPacket(buf, buf.length);
        //接收
        ds.receive(receivePack);

        int length = receivePack.getLength();
        String ip = receivePack.getAddress().getHostAddress();
        String data = new String(receivePack.getData(), 0, length);
        int port = receivePack.getPort();
        System.out.println("udp UDPSearcher receive data" + data + "  address :" + ip + "  port : " + port);
        System.out.println("udp UDPSearcher close");
    }
}
