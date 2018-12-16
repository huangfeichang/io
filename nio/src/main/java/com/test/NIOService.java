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
 */
public class NIOService {
    ServerSocketChannel serverSocketChannel;
    Selector selector;

    ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(5, 5, 30, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(100));

    {
        try {
            /*打开ServerSocketChannel通道*/
            serverSocketChannel = ServerSocketChannel.open();
            /*在socket中绑定端口*/
            serverSocketChannel.socket().bind(new InetSocketAddress(StaticUtil.REGISTER_PORT));
            /*设置非阻塞模式*/
            serverSocketChannel.configureBlocking(false);

            /*注册一个选择器*/
            selector = Selector.open();
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            System.err.println("NIO通道启动成功，监听端口：" + StaticUtil.REGISTER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serverHandle() throws IOException {
        boolean serverFlag = true;
        while (serverFlag) {
            /*在没有客户端的链接时，实现阻塞，等待客户端连接】*/
            int d = selector.select();

            if (d == 0) continue;

            /*循环迭代所有通道信息*/
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey k = keys.next();
                /*通道的操作类型*/
                if (k.isAcceptable()) {
                    /*获取客户端信息*/
                    SocketChannel client = serverSocketChannel.accept();
                    if (client != null) {
                        threadPoolExecutor.execute(() -> {
                            /*设置缓冲区*/
                            ByteBuffer byteBuffer = ByteBuffer.allocate(50);
                            boolean flag = true;
                            /*循环缓冲区的内容*/
                            while (flag) {
                                /*清空缓冲区*/
                                byteBuffer.clear();
                                try {
                                    /*从还缓冲区读取内容*/
                                    int t = client.read(byteBuffer);
                                    String s = new String(byteBuffer.array(), 0, t).trim();
                                    System.err.println("从客户端接收到的数据：" + s);
                                    /*当遇到exit断开连接，client.close()*/
                                    if ("exit".equals(s)) {
                                        flag = false;
                                    }
                                    /*再次清空缓冲区*/
                                    byteBuffer.clear();
                                    /*向还缓冲区写入数据*/
                                    byteBuffer.put(s.getBytes());
                                    /*重置位置*/
                                    byteBuffer.flip();

                                    /*向客户端回应信息*/
                                    client.write(byteBuffer);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            try {
                                client.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                /*在处理完一个channel后，必须移除此通道，否则会使selector.select()进入死循环*/
                keys.remove();
            }

        }
    }

    public static void main(String[] args) throws IOException {
        new NIOService().serverHandle();
    }
}
