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
 * 【小故事】：
 * 打电话给饭店,我要吃排骨。服务员说:好的,然后我去干其他事了,过了半个小时,我又打电话问,好了吗。
 * 又回答:还在做。不必等着 ,可以做其他事,有问必答
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
            /*在没有客户端的链接时，实现阻塞，等待客户端连接设置3s一次如果不设置一直阻塞*/
            int d = selector.select(3000);

            if (d == 0) {
                /*在等待新的通道进来之前 可以异步执行其他任务【非阻塞在此实现】*/
                continue;
            }

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
                            accessData(client);
                        });
                    }
                }
                /*在处理完一个channel后，必须移除此通道，否则会使selector.select()进入死循环*/
                keys.remove();
            }

        }
    }

    private void accessData(SocketChannel client) {
        /*设置缓冲区
        * 读数据需要使用while(true)来监听缓冲区中的数据不断的输出
        * */
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
    }

    public static void main(String[] args) throws IOException {
        new NIOService().serverHandle();
    }
}
