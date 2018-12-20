package com.test;


import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

/**
 * 【同步阻塞】【使用serverSocket.accept()实现阻塞】
 * BIO服务端数据操作
 * 接收数据，并且处理数据
 * 返回数据不需要手动输入，返回需要的信息
 * 注：发送内容必须带有换行符
 * 可以使用telnet localhost 8044测试
 */
public class BIOService {
    ServerSocket serverSocket;

    {
        try {
            /*创建一个socket的服务*/
            serverSocket = new ServerSocket(StaticUtil.REGISTER_PORT);
            System.err.println("socket服务已经启动，监听端口" + StaticUtil.REGISTER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clientServie() {
        /*设置接受状态*/
        boolean acceptFlag = true;
        while (acceptFlag) {
            try {
                /*等待客户端的链接，并返回一个链接对象【实现阻塞，等待新的链接进来】*/
                Socket client = serverSocket.accept();

                /*一个任务启动一个线程【为了保证可以链接多个客户端】*/
                /*一旦接收到一个连接请求，就可以建立socket，并在该socket上进行读写操作，
                此时不能再接收其它客户端的连接请求，只能等待同当前连接的客户端的操作执行
                完成【关闭通道】。*/
                new Thread(() -> {
                    try {
                        /*使用Scanner获取客户端发送过来的数据【相当于缓冲区，有数据会自动获取】
                        * 读数据需要使用while(true)来监听缓冲区中的数据不断的输出
                        * */
                        Scanner scanner = new Scanner(client.getInputStream());
                        //            scanner.useDelimiter("\n"); //可要可不要
                        /*设置向指定客户端返回信息*/
                        PrintStream printStream = new PrintStream(client.getOutputStream());

                        boolean msgFlag = true;
                        /*while主要是为了保证线程不死，可以一直保持通信状态【如果没有while只能交互一次】*/
                        while (msgFlag) {
                            if (scanner.hasNext()) {
                                String rd = scanner.next();
                                System.err.println("从客户端接收到的数据是：" + rd);
                                if ("exit".equals(rd)) {
                                    msgFlag = false;
                                    printStream.print(".........");
                                    /*设置通道关闭操作【不需要关闭，不需要调用此方法】*/
                                    client.close();
                                } else {
                                    /*向客户但回传信息*/
                                    System.err.println("向客户端回传信息" + rd);
                                    /*发送内容必须带有println带有换行符的【接收内容不一定要换行符】*/
                                    printStream.println("【ECHO】" + rd);
                                }
                            }
                        }
                        /*设置通道关闭操作【不需要关闭，不需要调用此方法】*/
//                        client.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        BIOService bioService = new BIOService();
        bioService.clientServie();
    }
}
