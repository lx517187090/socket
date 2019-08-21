package com.ch6.server.handle;

import com.ch6.net.clink.utils.CloseUtils;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final Socket socket;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final CloseNotify closeNotify;

    public ClientHandler(Socket socket, CloseNotify closeNotify) throws IOException {
        this.socket = socket;
        this.readHandler = new ClientReadHandler(socket.getInputStream());
        this.writeHandler = new ClientWriteHandler(socket.getOutputStream());
        this.closeNotify = closeNotify;
        System.out.println("新客户端连接 ：" + socket.getInetAddress() + "  P : " + socket.getPort());
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socket);
        System.out.println("客户端已退出");
    }

    public void exitBySelf() {
        exit();
        closeNotify.onSelfClosed(this);
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void readToPrint(){
        readHandler.start();
    }

    class ClientWriteHandler {
        private boolean done = false;
        private PrintStream printStream;
        private final ExecutorService executorService;

        ClientWriteHandler(OutputStream outputStream) {
            this.printStream = new PrintStream(outputStream);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit(){
            done = true;
            CloseUtils.close(printStream);
            executorService.shutdownNow();
        }

        void send(String str) {
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable{

            private final String str;

            WriteRunnable(String str) {
                this.str = str;
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done){
                    return;
                }
                try {
                    ClientWriteHandler.this.printStream.println(str);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }


    class ClientReadHandler extends Thread {
        private boolean done = false;
        private InputStream inputStream;

        ClientReadHandler(InputStream inputStream) {
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
                    String str = socketInput.readLine();
                    if (str == null) {
                        System.out.println("客户端已无法读取数据！");
                        //退出当前客户端
                        ClientHandler.this.exitBySelf();
                        break;
                    }
                    System.out.println(str);
                } while (!done);
            } catch (IOException e) {
                if (!done) {
                    System.out.println("连接异常关闭");
                    ClientHandler.this.exit();
                }
            } finally {
                CloseUtils.close(inputStream);
            }
        }
    }

    public interface CloseNotify{
        void onSelfClosed(ClientHandler handler);
    }
}
