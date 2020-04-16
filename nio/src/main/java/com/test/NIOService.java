package com.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * NIO同步非阻塞模式【使用selector.select()实现阻塞】
 * 数据的读与写主要操作缓冲区ByteBuffer【在操作读写前线clear(),特别当写的情况下需要flip()重置位置】
 */
public class NIOService {
    ServerSocketChannel serverSocketChannel;
    Selector selector;

    {
        try {
            /*打开ServerSocketChannel通道*/
            serverSocketChannel = ServerSocketChannel.open();
            /*在socket中绑定端口*/
            serverSocketChannel.socket().bind(new InetSocketAddress(9987));
            /*设置非阻塞模式*/
            serverSocketChannel.configureBlocking(false);

            /*注册一个选择器*/
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.err.println("NIO通道启动成功，监听端口：9987");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverHandle() throws IOException {
        /*在没有客户端的链接时，实现阻塞，等待客户端连接】*/
        while (selector.select() > 0) {
            /*循环迭代所有通道信息*/
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey selectionKey = keys.next();
                keys.remove();
                /*通道的操作类型*/
                if (selectionKey.isAcceptable()) {
                    // 创建新的连接，并且把连接注册到selector上，而且，
                    // 声明这个channel只对读操作感兴趣。
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (selectionKey.isReadable()) {
                    SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
                    ByteBuffer readBuff = ByteBuffer.allocate(1024);
                    ByteBuffer write = ByteBuffer.allocate(1024);
                    readBuff.clear();
                    socketChannel.read(readBuff);
                    readBuff.flip();
                    System.out.println("received : " + new String(readBuff.array()));
//                    selectionKey.interestOps(SelectionKey.OP_WRITE);

                    // 写信息
                    write.clear();
                    write.put("hurn sma d".getBytes());
                    write.flip();
                    socketChannel.write(write);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

        }
    }

    public static void main(String[] args) throws IOException {
        new NIOService().serverHandle();
    }
}
