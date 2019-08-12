package com.nettyInAction.ch2;

import com.ch1.ServerBoot;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;

public class EchoService {
    private final int port;

    public EchoService(int port){
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1){
            System.out.println("Usage: " + EchoService.class.getName() + "<port>");
            return;
        }
        int port = Integer.parseInt(args[0]); //设置端口值
        new EchoService(port).start();
    }

    private void start() throws Exception {
        final EchoServiceHandler serviceHandler = new EchoServiceHandler();
        //创建evenLoop
        NioEventLoopGroup group = new NioEventLoopGroup();
        try{
            //创建bootstrap
            ServerBootstrap b = new ServerBootstrap();
            b.group(group)
                    //指定传输的channel
                    .channel(NioServerSocketChannel.class)
                    //使用指定端口设置套接字地址
                    .localAddress(new InetSocketAddress(port))
                    //添加一个EchoServiceHandler到子Channel的ChannelPipeline
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            //EchoServiceHandler被标注为@Sharable 所以我们总是可以使用同样的实例
                            ch.pipeline().addLast(serviceHandler);
                        }
                    });
            //异步的绑定服务器 调用sync方法阻塞等待直到绑定完成
            ChannelFuture f = b.bind().sync();
            //获取channel的CloseFuture，并且阻塞当前线程直到它完成
            f.channel().closeFuture().sync();
        }finally {
            //关闭EvenLoopGroup 并释放所有资源
            group.shutdownGracefully().sync();
        }
    }
}
