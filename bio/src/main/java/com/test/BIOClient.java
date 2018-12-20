package com.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * BIO客户端操作
 * 接收数据并读取操作
 * 发送数据使用键盘输入
 * 注：发送内容必须带有换行符
 */
public class BIOClient {
    private static final BufferedReader KEYBOARD_INPUT = new BufferedReader(new InputStreamReader(System.in)) ;
    Socket socket;

    {
        try {
            /*与服务端建立连接*/
            socket = new Socket(StaticUtil.REGISTER_ADDRESS, StaticUtil.REGISTER_PORT);
            System.err.println("链接的服务端信息地址：" + StaticUtil.REGISTER_ADDRESS + ",端口：" + StaticUtil.REGISTER_PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void serviceService() {
        try {
            /*接收服务端返回的数据
            * 【相当于缓冲区，有数据会自动获取】
            * 读数据需要使用while(true)来监听缓冲区中的数据不断的输出
            * */
            Scanner scanner = new Scanner(socket.getInputStream());
//            scanner.useDelimiter("\n"); //可要可不要
            /*向服务端写数据*/
            PrintStream printStream = new PrintStream(socket.getOutputStream());

            boolean flag = true;
            /*while为了可以一直交互，要不然一次就终止*/
            while (flag) {
                System.err.println("请输入需要发送的内容：");
                String r = readContent();
                /*发送内容必须使用println带有换行符【接收内容不一定要换行符】*/
                printStream.println(r);
                if ("e".equals(r)) {
                    flag = false;
                }
                if (scanner.hasNext()) {
                    System.err.println("==========================");
                    System.err.println(scanner.next());
                }
            }
            socket.close();
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
        new BIOClient().serviceService();
    }
}
