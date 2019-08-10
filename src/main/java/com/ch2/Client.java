package com.ch2;

import java.io.*;
import java.net.*;

public class Client {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket();

        //设置超时时间
        socket.setSoTimeout(3000);
        //连接本地 端口2000 超时时间3000
        socket.connect(new InetSocketAddress(Inet4Address.getByName("192.168.1.14"), 2000), 3000);

        System.out.println("已发起服务器连接 ， 并进入后续流程");

        System.out.println("客户端信息:" + socket.getLocalAddress() + "port : "+socket.getLocalPort());

        System.out.println("服务端信息:" + socket.getInetAddress() + "port:" + socket.getPort());

        try {
            todo(socket);
        } catch (Exception e){
            System.out.println("异常关闭");
        }
        socket.close();
        System.out.println("客户端已退出");
    }

    /**
     * 发送数据
     * @param client socket
     * @throws IOException io异常
     */
    private static void todo(Socket client) throws IOException{
        // 构建键盘输入流
        InputStream in = System.in;
        BufferedReader input = new BufferedReader(new InputStreamReader(in));

        //得到socket输出流 并转换为打印流
        OutputStream outputStream = client.getOutputStream();
        PrintStream stream = new PrintStream(outputStream);

        //得到socket输入流 并转换为BufferedReader
        InputStream inputStream = client.getInputStream();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        boolean flag = true;
        do {
            //从键盘读取一行
            String str = input.readLine();
            //发送到服务器
            stream.println(str);

            //从服务器读取一行
            String echo = bufferedReader.readLine();
            if ("bye".equals(echo)){
                flag = false;
            }else {
                System.out.println(echo);
            }
        } while (flag);
        input.close();
        bufferedReader.close();
    }
}
