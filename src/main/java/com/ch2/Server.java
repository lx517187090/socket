package com.ch2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int port = 2000;
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket= new ServerSocket(port);
        System.out.println("服务器准备就绪");
        //等待客户端连接
        for (;;){
            //得到客户端
            Socket accept = serverSocket.accept();
            ClientHandler clientHandler = new ClientHandler(accept);
            clientHandler.start();
        }
    }


    private static class ClientHandler extends Thread{
        private Socket socket;

        private boolean flag = true;

        ClientHandler(Socket socket){
            this.socket = socket;
        }

        @Override
        public void run() {
            super.run();
            System.out.println("新客户端连接 :" + socket.getInetAddress() + "P : " + socket.getPort());
            try {
                //得到打印流 用于数据输出 服务器会送数据使用
                PrintStream socketOutput = new PrintStream(socket.getOutputStream());

                //得到输入流，用于接收数据
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                do {
                    String str = bufferedReader.readLine();
                    if ("bye".equals(str)){
                        flag = false;
                        socketOutput.println("bye");
                    }else {
                        System.out.println(str);
                        socketOutput.println("回送: " + str.length());
                    }
                }while (flag);
                socketOutput.close();
                bufferedReader.close();
            }catch (Exception e){
                System.out.println("连接异常断开。。。。");
            }finally {
                try {
                    socket.close();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            System.out.println("客户端已经退出 "+ socket.getInetAddress() + "P : " + socket.getPort());
        }
    }
}
