package com.ch5UdpTcp.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer {

    private final int port;
    private ClientListener mListener;

    public TCPServer(int port){
        this.port = port;
    }

    public boolean start() {
        try {
            ClientListener listener = new ClientListener(port);
            mListener = listener;
            listener.start();
        }catch (IOException e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void stop() {
        if (mListener != null){
            mListener.exit();
        }
    }

    public void broadcast() {

    }


    private static class ClientListener extends Thread{

        private ServerSocket serverSocket;
        private boolean done = false;
        public ClientListener(int port) throws IOException {
            serverSocket = new ServerSocket(port);
        }

        @Override
        public void run() {
            super.run();
            System.out.println("server start");
            do {
                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                }catch (IOException e){
                    e.printStackTrace();
                }
                ClientHandler clientHandler = new ClientHandler(socket);
                clientHandler.start();
            }while (!done);
            System.out.println("server is stop...");
        }

        public void exit(){
            done = true;
            try {
                serverSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private static class ClientHandler extends Thread{
        private Socket socket;
        private boolean flag = false;

        public ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("新客户端连接 ：" + socket.getInetAddress() + "  P : " + socket.getPort());

            try {
                PrintStream socketStream = new PrintStream(socket.getOutputStream());
                BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do {
                    String str = socketInput.readLine();
                    if ("bye".equals(str)){
                        flag = false;
                        socketStream.println("bye");
                    }else {
                        System.out.println(str);
                        socketStream.println("回送：" + str.length());
                    }
                }while (!flag);

                socketStream.close();
                socketInput.close();
            }catch (IOException e){
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
