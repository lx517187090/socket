package com.ch3UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * UDP服务提供者
 */
public class UDPProvider {

    public static void main(String[] args) throws IOException {
        System.out.println("udp provider start");

        //作为接收者 制定一个端口用于接收数据
        DatagramSocket ds = new DatagramSocket(20000);

        //构建接收实体
        final byte[] buf= new byte[512];
        DatagramPacket receivePack = new DatagramPacket(buf, buf.length);

        //接收
        ds.receive(receivePack);

        String ip = receivePack.getAddress().getHostAddress();
        int port = receivePack.getPort();
        int length = receivePack.getLength();
        String data = new String(receivePack.getData(), 0, length);

        System.out.println("udp provider receive data     " + data + "  address :" + ip + "  port : " + port);

        String resp = "receive data length :" + length;
        DatagramPacket respPocket = new DatagramPacket(resp.getBytes(), resp.length(), receivePack.getAddress(), receivePack.getPort());
        ds.send(respPocket);
        System.out.println("udp provider close");
    }

}
