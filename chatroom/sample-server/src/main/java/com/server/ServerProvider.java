package com.server;


import com.constants.UDPConstants;
import com.socket.utils.ByteUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.UUID;

public class ServerProvider {

    private static Provider PROVIDER_INSTANCE;

    public static void start(int portServer) {
        stop();
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn, portServer);
        provider.start();
        PROVIDER_INSTANCE = provider;
    }

    private static class Provider extends Thread{
        private final byte[] sn;
        private final int port;
        private boolean done = false;
        private DatagramSocket ds = null;
        //消息buffer
        final byte[] buffer = new byte[256];

        Provider(String sn, int port) {
            super();
            this.sn = sn.getBytes();
            this.port = port;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("UDPProvider start......");
            try {
                //监听30201端口
                ds = new DatagramSocket(UDPConstants.PORT_SERVER);
                //接收消息的packet
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);
                while (!done){
                    //接收
                    ds.receive(receivePack);
                    //打印接收到的信息和发送者信息
                    String clientIp = receivePack.getAddress().getHostAddress();
                    int clientPort = receivePack.getPort();
                    int clientDataLen = receivePack.getLength();
                    byte[] clientData = receivePack.getData();
                    boolean isValid = clientDataLen >= (UDPConstants.HEADER.length + 2 + 4)
                            && ByteUtils.startsWith(clientData, UDPConstants.HEADER);

                    System.out.println("serverProvider receive from ip:" + clientIp +
                            "  port : " + clientPort + "dataValid :" + isValid);
                    if (!isValid){
                        continue;
                    }
                    //解析命令与会送端口
                    int index = UDPConstants.HEADER.length;
                    short cmd = (short)((clientData[index++] << 8) | (clientData[index++] & 0xFF));
                    int responsePort = (((clientData[index++]) << 24) |
                            ((clientData[index++] & 0xFF) << 16) |
                            ((clientData[index++] & 0xFF) << 8) |
                            (clientData[index++] & 0xFF));
                    if (cmd == 1 && responsePort > 0){
                        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
                        byteBuffer.put(UDPConstants.HEADER);
                        byteBuffer.putShort((short)2);
                        byteBuffer.putInt(port);
                        byteBuffer.put(sn);
                        int len = byteBuffer.position();
                        //直接构建会送数据
                        DatagramPacket response = new DatagramPacket(buffer, len, receivePack.getAddress(), responsePort);
                        ds.send(response);
                        System.out.println("ServerProvide response to: " + clientIp +
                                "  port " + port + " responseData Length" + len);
                    }else {
                        System.out.println("ServerProvide receive cmd not support");
                    }
                }
            }catch (IOException e){
                e.printStackTrace();
            }finally {
                close();
            }
        }

        void exit(){
            done = true;
            close();
        }

        private void close() {
            if (ds != null){
                ds.close();
                ds = null;
            }
        }
    }

    static void stop() {
        if (PROVIDER_INSTANCE != null){
            PROVIDER_INSTANCE.exit();
            PROVIDER_INSTANCE = null;
        }
    }
}
