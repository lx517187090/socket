package com.server.handle;

import com.socket.utils.CloseUtils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private final SocketChannel socketChannel;
    private final ClientReadHandler readHandler;
    private final ClientWriteHandler writeHandler;
    private final ClientHandleCallback clientHandleCallback;
    private String clintInfo;

    public ClientHandler(SocketChannel socketChannel, ClientHandleCallback clientHandleCallback) throws IOException {
        this.socketChannel = socketChannel;
        //设置非阻塞模式
        socketChannel.configureBlocking(false);

        Selector readSelector = Selector.open();
        socketChannel.register(readSelector, SelectionKey.OP_READ);
        this.readHandler = new ClientReadHandler(readSelector);

        Selector writeSelector = Selector.open();
        socketChannel.register(writeSelector, SelectionKey.OP_WRITE);
        this.writeHandler = new ClientWriteHandler(writeSelector);

        this.clientHandleCallback = clientHandleCallback;
        this.clintInfo = socketChannel.getRemoteAddress().toString();
        System.out.println("新客户端连接  " + clintInfo);
    }

    public void exit() {
        readHandler.exit();
        writeHandler.exit();
        CloseUtils.close(socketChannel);
        System.out.println("客户端已退出" + clintInfo);
    }

    public void exitBySelf() {
        exit();
        clientHandleCallback.onSelfClosed(this);
    }

    public void send(String str) {
        writeHandler.send(str);
    }

    public void readToPrint() {
        readHandler.start();
    }

    public String getInfo() {
        return clintInfo;
    }

    class ClientWriteHandler {
        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer buffer;
        private final ExecutorService executorService;

        ClientWriteHandler(Selector selector) {
            this.selector = selector;
            this.buffer = ByteBuffer.allocate(256);
            this.executorService = Executors.newSingleThreadExecutor();
        }

        void exit() {
            done = true;
            CloseUtils.close(selector);
            executorService.shutdownNow();
        }

        void send(String str) {
            if (done) {
                return;
            }
            executorService.execute(new WriteRunnable(str));
        }

        class WriteRunnable implements Runnable {

            private final String str ;

            WriteRunnable(String str) {
                this.str = str + '\n';   //增加换行符
            }

            @Override
            public void run() {
                if (ClientWriteHandler.this.done) {
                    return;
                }
                buffer.clear();
                buffer.put(str.getBytes());
                //反转操作
                buffer.flip();
                while (!done && buffer.hasRemaining()) {
                    try {
                        int write = socketChannel.write(buffer);
                        //len = 0 合法
                        if (write < 0) {
                            System.out.println("客户端已无法发送数据");
                            ClientHandler.this.exitBySelf();
                            break;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    class ClientReadHandler extends Thread {
        private boolean done = false;
        private final Selector selector;
        private final ByteBuffer byteBuffer;

        ClientReadHandler(Selector selector) {
            this.selector = selector;
            this.byteBuffer = ByteBuffer.allocate(256);
        }

        void exit() {
            done = true;
            selector.wakeup();
            CloseUtils.close(selector);
        }

        @Override
        public void run() {
            super.run();
            try {
                do {
                    //拿到数据
                    if (selector.select() == 0) {
                        if (done) {
                            break;
                        }
                        continue;
                    }
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        if (done) {
                            break;
                        }
                        SelectionKey key = iterator.next();
                        iterator.remove();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            byteBuffer.clear();
                            int read = channel.read(byteBuffer);
                            if (read > 0) {
                                //丢弃换行符
                                String str = new String(byteBuffer.array(), 0, read - 1);
                                clientHandleCallback.onMessageArrived(ClientHandler.this, str);
                            }
                        }
                    }
                } while (!done);
            } catch (IOException e) {
                if (!done) {
                    System.out.println("连接异常关闭");
                    ClientHandler.this.exit();
                }
            } finally {
                CloseUtils.close(selector);
            }
        }
    }

    public interface ClientHandleCallback {
        //自身关闭通知
        void onSelfClosed(ClientHandler handler);

        void onMessageArrived(ClientHandler handler, String msg);
    }
}
