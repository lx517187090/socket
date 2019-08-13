package com.ch5UdpTcp.client;

import com.ch5UdpTcp.client.bean.ServerInfo;
import com.ch5UdpTcp.constants.UDPConstants;
import com.ch5UdpTcp.net.clink.utils.ByteUtils;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ClientSearcher {

    private static final int LISTEN_PORT = UDPConstants.PORT_CLIENT_RESPONSE;

    public static ServerInfo searchServer(int timeout) {
        System.out.println("udpSearcher start....");

        CountDownLatch receiveLatch = new CountDownLatch(1);
        Listener listener = null;
        try {
            listener = listen(receiveLatch);
            sendBroadCast();
            receiveLatch.await(timeout, TimeUnit.MILLISECONDS);
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.println("UDPSearcher Finished....");
        if (listener == null){
            return null;
        }
        List<ServerInfo> device = listener.getServerAndClose();
        if(device.size() > 0){
            return device.get(0);
        }
        return null;
    }

    private static void sendBroadCast() throws IOException {
        System.out.println("UDPSearcher sendBroadCast started ");
        DatagramSocket ds = new DatagramSocket();
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put(UDPConstants.HEADER);
        byteBuffer.putShort((short)1);
        byteBuffer.putInt(LISTEN_PORT);
        DatagramPacket requestPack = new DatagramPacket(byteBuffer.array(), byteBuffer.position() + 1);
        requestPack.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPack.setPort(UDPConstants.PORT_SERVER);
        ds.send(requestPack);
        ds.close();
        System.out.println("UDPSearcher sendBroadCast finished.. ");

    }

    private static Listener listen(CountDownLatch receiveLatch) throws InterruptedException {
        System.out.println("UDPSearcher start listen..");
        CountDownLatch startDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(LISTEN_PORT, startDownLatch, receiveLatch);
        listener.start();
        startDownLatch.await();
        return listener;
    }


    private static class Listener extends Thread{

        private final int listenPort;
        private final CountDownLatch startDownLatch;
        private final CountDownLatch receiveLatch;
        private final List<ServerInfo> serverInfoList = new ArrayList<>();
        private final byte[] buffer = new byte[128];
        private final int minLen = UDPConstants.HEADER.length + 2 + 4;
        private boolean done = false;
        private DatagramSocket ds = null;

        private Listener(int listenPort, CountDownLatch startDownLatch, CountDownLatch receiveLatch) {
            this.listenPort = listenPort;
            this.startDownLatch = startDownLatch;
            this.receiveLatch = receiveLatch;
        }

        private List<ServerInfo> getServerAndClose() {
            done = true;
            close();
            return serverInfoList;
        }

        @Override
        public void run() {
            super.run();
            //通知已启动
            startDownLatch.countDown();
            try{
                ds = new DatagramSocket(listenPort);
                DatagramPacket receivePack = new DatagramPacket(buffer, buffer.length);
                while (!done){
                    ds.receive(receivePack);
                    String ip = receivePack.getAddress().getHostAddress();
                    int port = receivePack.getPort();
                    int dataLength = receivePack.getLength();
                    byte[] data = receivePack.getData();
                    boolean isValid = dataLength >= minLen && ByteUtils.startsWith(data, UDPConstants.HEADER);
                    System.out.println("serverProvider receive from ip:" + ip +
                            "  port : " + port + "dataValid :" + isValid);
                    if (!isValid){
                        continue;
                    }
                    ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, UDPConstants.HEADER.length, dataLength);
                    final short cmd = byteBuffer.getShort();
                    final int serverPort = byteBuffer.getInt();
                    if (cmd != 2 || serverPort <= 0){
                        System.out.println("UDPSearcher receive cmd:" + cmd +
                                "  port : " + serverPort);
                        continue;
                    }
                    String sn = new String(buffer, minLen, dataLength - minLen);
                    ServerInfo info = new ServerInfo(serverPort, ip, sn);
                    serverInfoList.add(info);
                    //成功接收到一份
                    receiveLatch.countDown();

                }
            }catch (Exception e){
            }finally {
                close();
            }
            System.out.println("UDPSearcher listener finished...");
        }

        private void close() {
            if (ds != null){
                ds.close();
                ds = null;
            }
        }
    }
}
