package com.ch5UdpTcp.constants;

public class UDPConstants {
    //公共头部
    public static byte[] HEADER = new byte[]{7, 7, 7, 7, 7, 7, 7, 7};
    //服务器固化udp接收端口
    public static int PORT_SERVER =30201;
    //客户端回送端口
    public static int PORT_CLIENT_RESPONSE = 30202;

}
