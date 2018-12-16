package com.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class NIOService {
    ServerSocketChannel serverSocketChannel;

    {
        try {
            /*打开ServerSocketChannel通道*/
            serverSocketChannel = ServerSocketChannel.open();
            /*在socket中绑定端口*/
            serverSocketChannel.socket().bind(new InetSocketAddress(StaticUtil.REGISTER_PORT));
            /*设置非阻塞模式*/
            serverSocketChannel.configureBlocking(false);

            System.err.println("NIO通道启动成功，监听端口：" + StaticUtil.REGISTER_PORT);

            /*注册一个选择器*/
            /*Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);*/
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverHandle() {
        boolean serverFlag = true;
        while (serverFlag) {
            try {
                SocketChannel client = serverSocketChannel.accept();
                if (client != null) {
                    System.err.println("------>" + client);
                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                    int l = client.read(byteBuffer);
                    while (true) {
                        if (l > 0) {
                            byteBuffer.clear();
                            byteBuffer.flip();
                            System.err.println("从客户端获取到的数据：" + new String(byteBuffer.array(), "UTF-8"));
                        }

                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        new NIOService().serverHandle();
    }
}
