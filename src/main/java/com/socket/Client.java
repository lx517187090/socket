package com.socket;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client {

	public static void main(String[] args) throws Exception {
		//1 初始化socket
		Socket socket = new Socket();
		// 超时时间
		socket.setSoTimeout(3000);
		//连接本地 端口2000 超时时间3000
		socket.connect(new InetSocketAddress(InetAddress.getLocalHost(), 2000),3000);

		System.out.println("已发起服务器连接， 并进入后续流程");
		System.out.println("客户端信息：" + socket.getLocalAddress() + "P :" + socket.getLocalPort());
		System.out.println("服务器信息：" + socket.getInetAddress() + "P :" + socket.getPort());
		try {
			//发送数据
			todo(socket);
		}catch (Exception e){
			System.out.println("异常关闭");
		}
		//释放资源
		socket.close();
		System.out.println("客户端已经退出");
	}

	/**
	 * 发送数据
	 * @param client
	 * @throws IOException
	 */
	private static void todo(Socket client) throws IOException {
		//构建基础数据输入流
		InputStream in = System.in;
		BufferedReader input = new BufferedReader(new InputStreamReader(in));

		//获取socket输出流 并转换位打印流
		OutputStream outputStream = client.getOutputStream();
		PrintStream socketPrintStream = new PrintStream(outputStream);
		//获取socket输入流 并转换为bufferReader
		InputStream inputStream = client.getInputStream();
		BufferedReader socketBufferReader = new BufferedReader(new InputStreamReader(inputStream));

		boolean flag = true;
		while (flag){
			//从键盘读取一行
			String str = input.readLine();
			//发送数据
			socketPrintStream.println(str);

			//从服务器读取一行
			String echo = socketBufferReader.readLine();
			if("bye".equalsIgnoreCase(echo)){
				flag = false;
			}else {
				System.out.println(echo);
			}
		}
		socketPrintStream.close();
		socketBufferReader.close();
	}
}
