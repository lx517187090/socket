package com.ch4TCP;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.*;

public class Server {
    private static final int PORT = 20000;

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = createServerSocket();

        initServerSocket(serverSocket);

        System.out.println("服务器准备就绪。。。");
        System.out.println("服务器信息 :" + serverSocket.getInetAddress() + " P : " + serverSocket.getLocalPort());

        for (;;){
            Socket socket = serverSocket.accept();
            ClientHandler handler = new ClientHandler(socket);
            handler.start();
        }
    }

    private static class ClientHandler extends Thread{
        private Socket socket;


        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("客户端连接 :" + socket.getInetAddress() + " P :" + socket.getPort());


            try {
                OutputStream outputStream = socket.getOutputStream();
                InputStream inputStream = socket.getInputStream();

                byte[] buff = new byte[128];
                int read = inputStream.read(buff);
                if (read > 0){
                    System.out.println("读取数据长度 : " + read + " 数据为" + Array.getByte(buff, 0));//new String(buff, 0 , read));
                    outputStream.write(buff, 0 ,read);
                }else {
                    System.out.println("未收到数据" + read);
                    outputStream.write(new byte[]{0});
                }

            } catch (IOException e) {
                e.printStackTrace();
            }finally {
                try {
                    socket.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static void initServerSocket(ServerSocket serverSocket) throws SocketException {
        serverSocket.setReuseAddress(true);

        serverSocket.setReceiveBufferSize(64 * 1024 * 1024);

        serverSocket.setPerformancePreferences(1, 1, 1);
    }

    private static ServerSocket createServerSocket() throws IOException {
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 50);
        return serverSocket;
    }
}
