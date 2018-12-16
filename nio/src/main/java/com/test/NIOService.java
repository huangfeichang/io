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
        int serverFlag = 0;
        System.err.println("-----");
        while ((serverFlag = selector.select()) > 0) {
            System.err.println("------>" + serverFlag);
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey k = keys.next();
                if (k.isAcceptable()) {
                    SocketChannel client = serverSocketChannel.accept();
                    System.err.println("-------------" + client);
                    if (client != null) {
                        threadPoolExecutor.execute(() -> {
                            ByteBuffer byteBuffer = ByteBuffer.allocate(50);
                            boolean flag = true;
                            while (flag) {
                                byteBuffer.clear();
                                try {
                                    int t = client.read(byteBuffer);
                                    String s = new String(byteBuffer.array(), 0, t).trim();
                                    System.err.println("从客户端接收到的数据：" + s);
                                    if ("exit".equals(s)) {
                                        flag = false;
                                    }
                                    byteBuffer.clear();
                                    byteBuffer.put(s.getBytes());
                                    byteBuffer.flip();

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
                keys.remove();
            }

        }
    }

    public static void main(String[] args) throws IOException {
        new NIOService().serverHandle();
    }
}
