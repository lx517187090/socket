package com.client;


import com.client.bean.ServerInfo;
import com.socket.utils.CloseUtils;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class TCPClient {

    public static void linkWith(ServerInfo info) throws IOException {
        Socket socket = new Socket();
        socket.setSoTimeout(3000);
        socket.connect(new InetSocketAddress(Inet4Address.getByName(info.getAddress()), info.getPort()));
        System.out.println("已发起服务器连接");
        System.out.println("客户端信息 " + socket.getLocalAddress() + " P: " + socket.getLocalPort());
        System.out.println("服务端信息 " + socket.getInetAddress() + " P :" + socket.getPort());
        try {
            ReadHandler readHandler = new ReadHandler(socket.getInputStream());
            readHandler.start();
            write(socket);
            //退出操作
            readHandler.exit();
        }catch (Exception e){
            e.printStackTrace();
        }
        socket.close();
        System.out.println("客户端已退出");
    }

    private static void write(Socket socket) throws IOException {
        InputStream in = System.in;
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));
        PrintStream socketPrintStream = new PrintStream(socket.getOutputStream());
        do {
            String str = bufferedReader.readLine();
            socketPrintStream.println(str);
            if ("0000".equalsIgnoreCase(str)){
                break;
            }
        }while (true);
        socketPrintStream.close();
    }

    static class ReadHandler extends Thread {
        private boolean done = false;
        private InputStream inputStream;

        ReadHandler(InputStream inputStream) {
            this.inputStream = inputStream;
        }

        void exit(){
            done = true;
            CloseUtils.close(inputStream);
        }

        @Override
        public void run() {
            super.run();
            try {
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(inputStream));
                do {
                    String str;
                    try {
                        str = socketInput.readLine();
                    }catch (SocketTimeoutException e){
                        continue;
                    }
                    if (str == null) {
                        System.out.println("客户端已无法读取数据！");
                        break;
                    }
                    System.out.println(str);
                } while (!done);
            } catch (IOException e) {
                if (!done) {
                    System.out.println("连接异常关闭");
                }
            } finally {
                CloseUtils.close(inputStream);
            }
        }
    }
}
