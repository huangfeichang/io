package com.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * NIO同步非阻塞模式【使用selector.select()实现阻塞】
 * 数据的读与写主要操作缓冲区ByteBuffer【在操作读写前线clear(),特别当写的情况下需要flip()重置位置】
 */
public class NIOClient1 {

    public static void main(String[] args) throws IOException {
        SocketChannel socketChannel = SocketChannel.open();
        socketChannel.connect(new InetSocketAddress("127.0.0.1", 9987));

        ByteBuffer writeBuffer = ByteBuffer.allocate(32);

        ByteBuffer read = ByteBuffer.allocate(32);

        while (true) {
            try {
                writeBuffer.clear();
                writeBuffer.put("weerrt".getBytes());
                writeBuffer.flip();
                socketChannel.write(writeBuffer);
                // 读数据
                read.clear();
                socketChannel.read(read);
                read.flip();
                System.out.println("received : " + new String(read.array()));
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }
}
