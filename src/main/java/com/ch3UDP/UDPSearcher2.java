package com.ch3UDP;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class UDPSearcher2 {

    private static final int PORT = 30000;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("udp UDPSearcher start");
        Listener listen = listen();

        sendBroadcast();

        System.in.read();

        List<Device> devices = listen.getDevicesAndClose();
        for (Device device : devices) {
            System.out.println("device finish " + device);
        }

        listen.exit();
    }

    private static Listener listen() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Listener listener = new Listener(PORT, countDownLatch);
        listener.start();
        countDownLatch.await();
        return listener;
    }

    private static void sendBroadcast() throws IOException {
        System.out.println("sendBroadcast start");
        //作为搜索方 让系统自动分配端口
        DatagramSocket ds = new DatagramSocket();
        String requestData = MessageCreater.buildWithPort(PORT);
        DatagramPacket requestPocket = new DatagramPacket(requestData.getBytes(), requestData.length());
        requestPocket.setAddress(InetAddress.getByName("255.255.255.255"));
        requestPocket.setPort(20000);
        ds.send(requestPocket);
        ds.close();
        System.out.println("udp sendBroadcast close");
    }

    private static class Device{
        private final int port;
        private final String ip;
        private final String sn;

        public Device(int port, String ip, String sn) {
            this.port = port;
            this.ip = ip;
            this.sn = sn;
        }

        public int getPort() {
            return port;
        }

        public String getIp() {
            return ip;
        }

        public String getSn() {
            return sn;
        }

        @Override
        public String toString() {
            return "Device{" +
                    "port=" + port +
                    ", ip='" + ip + '\'' +
                    ", sn='" + sn + '\'' +
                    '}';
        }
    }

    private static class Listener extends Thread{

        private final int listenPort;

        private final CountDownLatch countDownLatch;

        private final List<Device> devices = new ArrayList<>();

        private boolean done = false;
        private DatagramSocket ds = null;

        public Listener(int listenPort, CountDownLatch countDownLatch){
            this.listenPort = listenPort;
            this.countDownLatch = countDownLatch;
        }

        @Override
        public void run() {
            super.run();
            countDownLatch.countDown();
            try {
                ds = new DatagramSocket(listenPort);
                while (!done){
                    final byte [] buff = new byte[512];
                    DatagramPacket receive = new DatagramPacket(buff, buff.length);
                    ds.receive(receive);
                    String ip = receive.getAddress().getHostAddress();
                    String data = new String(receive.getData(), 0, receive.getLength());
                    System.out.println(data);
                    String sn = MessageCreater.parseSn(data);
                    int port = receive.getPort();
                    if (sn != null){
                        devices.add(new Device(port, ip, sn));
                    }
                }
            }catch (Exception e){

            }finally {
                close();
            }
        }

        List<Device> getDevicesAndClose(){
            done = true;
            close();
            return devices;
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
