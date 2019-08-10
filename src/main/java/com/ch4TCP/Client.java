package com.ch4TCP;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.*;

public class Client {

    private static final int PORT = 20000;
    private static final int LOCAL_PORT = 20001;

    public static void main(String[] args) throws IOException {
        Socket socket = createSocket();

        initSocket(socket);

        socket.connect(new InetSocketAddress(Inet4Address.getLocalHost(), PORT), 3000);

        System.out.println("已发起客户端连接，并进入后续流程");

        System.out.println("客户端信息: " + socket.getLocalAddress() + "  P :" + socket.getLocalPort());

        System.out.println("服务端信息 : " + socket.getInetAddress() + "  P : " + socket.getPort());

        try {
            todo(socket);
        }catch (Exception e){
            System.out.println("异常关闭");
        }

        socket.close();
        System.out.println("客户端已退出");
    }

    private static void todo(Socket socket) throws IOException {
        //得到socket输出流
        OutputStream outputStream = socket.getOutputStream();
        //得到socket输入流
        InputStream inputStream = socket.getInputStream();
        byte [] buf = new byte[128];
        //发送数据
        outputStream.write(new byte[]{1});
        //接收数据
        int read = inputStream.read(buf);
        if (read > 0){
            System.out.println("收到数量:" + read + "数据为 " + Array.getByte(buf, 0));
        }else {
            System.out.println("没有收到数量:" + read);
        }

        outputStream.close();
        inputStream.close();
    }

    private static void initSocket(Socket socket) throws SocketException {
        //设置读取超时时间为3s
        socket.setSoTimeout(3000);
        //是否复用未完全关闭的我socket地址 ， 对于指定bind操作后的套接字有效
        socket.setReuseAddress(true);

        //是否开启Nagle算法
        socket.setTcpNoDelay(false);

        //是否需要在长时间无数据响应时发送确认数据（类似心跳包），时间大约是2分钟
        socket.setKeepAlive(true);

        //对于close关闭操作行为进行怎样的处理 默认false 0
        //false 0 默认情况，关闭时立即返回，底层系统接管输出流，将缓冲区内的数据发送完成
        //true 0 关闭时立即返回，缓冲区数据直接抛弃，直接发送RST结束命令到对方，并无需经过2MSL等待
        //true 200 关闭时最长阻塞200毫秒随后按照第二种情况
        socket.setSoLinger(true, 20);

        //是否让紧急数据内敛默认false；紧急数据通过socket.sendUrgentDate(1)发送
        socket.setOOBInline(true);

        socket.setReceiveBufferSize(64 * 104 * 1024);
        socket.setSendBufferSize(64 * 1024 * 1024);

        //设置性能参数 短链接 延迟 带宽的相对重要性
        socket.setPerformancePreferences(1 ,1 ,1);
    }

    private static Socket createSocket() throws IOException {
        Socket socket = new Socket();
        socket.bind(new InetSocketAddress(Inet4Address.getLocalHost(), LOCAL_PORT));
        return socket;
    }
}
