package com.ch3UDP;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.UUID;

/**
 * UDP服务提供者
 */
public class UDPProvider2 {

    public static void main(String[] args) throws IOException {
        String sn = UUID.randomUUID().toString();
        Provider provider = new Provider(sn);
        provider.start();
        System.in.read();
        provider.exit();
    }

    private static class Provider extends Thread{

        private final String sn;

        private boolean done = false;

        private DatagramSocket ds = null;

        public Provider(String sn){
            this.sn = sn;
        }
        @Override
        public void run() {
            super.run();
            System.out.println("udp provider start");

            try {
                //作为接收者 制定一个端口用于接收数据
                ds = new DatagramSocket(20000);
                while (!done){
                    //构建接收实体
                    final byte[] buf= new byte[512];
                    DatagramPacket receivePack = new DatagramPacket(buf, buf.length);
                    //接收
                    ds.receive(receivePack);
                    String data = new String(receivePack.getData(), 0, receivePack.getLength());
                    System.out.println("udp provider receive data     " + data + "  address :" + receivePack.getAddress().getHostAddress()
                            + "  port : " + receivePack.getPort());
                    int responsePort = MessageCreater.parsePort(data);
                    if (responsePort != -1){
                        String resp = MessageCreater.buildWithSn(sn);
                        DatagramPacket respPocket = new DatagramPacket(resp.getBytes(), resp.length(), receivePack.getAddress(), responsePort);
                        ds.send(respPocket);
                    }
                }
            } catch (Exception e) {
            }finally {
                close();
            }
            System.out.println("udp provider close");
        }

        private void close(){
            if (ds != null){
                ds.close();
                ds = null;
            }
        }
        void exit(){
            done = true;
            close();
        }
    }

}
