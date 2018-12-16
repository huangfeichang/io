package com.test;

import java.io.IOException;
import java.net.InetSocketAddress;
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

            /*注册一个选择器*/
            Selector selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverHandle() {
        boolean serverFlag = true;
        while (serverFlag) {
            try {
                SocketChannel client = serverSocketChannel.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
