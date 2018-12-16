package com.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class NIOClient {

    SocketChannel socketChannel;

    {
        try {
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(StaticUtil.REGISTER_ADDRESS, StaticUtil.REGISTER_PORT));
            System.err.println("向服务端链接成功，地址：" + StaticUtil.REGISTER_ADDRESS);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsgToServerHandle() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(50);
        boolean flag = true;
        while (flag) {
            try {
                /*向服务端发送信息*/
                byteBuffer.clear();
                System.err.println("请输入需要发送的内容：");
                String r = readContent();

                byteBuffer.put(r.getBytes());
                byteBuffer.flip();
                socketChannel.write(byteBuffer);


                /*读取信息*/
                byteBuffer.clear();
                int t = socketChannel.read(byteBuffer);
                byteBuffer.flip();
                String s = new String(byteBuffer.array(), 0, t);
                System.err.println("服务端回传信息：" + s);

                byteBuffer.clear();


                /*断开服务*/
                if ("exit".equals(r) || "exit".equals(s)) {
                    flag = false;
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private String readContent() {
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNext()) {
            return scanner.next() ;
        }
        return "default";

    }

    public static void main(String[] args) {
        new NIOClient().sendMsgToServerHandle();
    }
}
