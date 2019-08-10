package com.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

@Slf4j
public class Client {

    private static final int DEFAULT_PORT = 7777;

    private static final String DEFAULT_IP = "127.0.0.1";

    private static void send(String expression){
        log.info("发送数据 IP地址 ：{} 端口号 ：{} 表达式 {}", DEFAULT_IP, DEFAULT_PORT, expression);

        send(DEFAULT_PORT, expression);
    }

    private static void send(int port, String expression) {
        Socket socket = null;

        BufferedReader in = null;

        PrintWriter out = null;

        try {
            socket = new Socket(DEFAULT_IP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(expression);
            log.info("结果为 : {}", in.readLine());
        }catch (Exception e){
            e.printStackTrace();
            log.error(e.getLocalizedMessage());
        }finally {
            if(in != null){
                try {
                    in.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(out != null){
                out.close();
            }
            if(socket != null){
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }
    }
}
