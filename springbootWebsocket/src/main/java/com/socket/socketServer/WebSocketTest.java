package com.socket.socketServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author huangfeichang.
 * @date 2018/12/18 0018.
 */
@ServerEndpoint("/socket")
@Component
public class WebSocketTest {
    private static Logger logger = LoggerFactory.getLogger(WebSocketTest.class);

    /**静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。*/
    private static AtomicInteger onlineCount = new AtomicInteger(0);

    /**concurrent包的线程安全Set，用来存放每个客户端对应的WebSocketTest对象。*/
    private static CopyOnWriteArraySet<WebSocketTest> webSocketSet = new CopyOnWriteArraySet<WebSocketTest>();

    /**ConcurrentHashMap是线程安全k-v组合集合*/
    private static ConcurrentHashMap<String, WebSocketTest> concurrentHashMap = new ConcurrentHashMap(16);

    /**与某个客户端的连接会话，需要通过它来给客户端发送数据*/
    private Session session;

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        this.session = session;
        //利用坐席号绑定唯一对应通道
        concurrentHashMap.putIfAbsent(session.getQueryString(), this);
        //在线数加1
        addOnlineCount();
        logger.info("有新连接加入！当前在线人数为" + getOnlineCount());
        try {
            sendMessage("有新连接加入！" + session.getId());
        } catch (IOException e) {
            logger.error("IO异常", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        /*移除此通道*/
        /*webSocketSet.remove(this);*/
        concurrentHashMap.remove(this.session.getQueryString());
        /*在线数减1*/
        subOnlineCount();
        logger.info("有一连接关闭！当前在线人数为" + getOnlineCount() + ",通道为：" + this.session.getQueryString());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public void onMessage(String message) throws IOException {
        logger.info("来自客户端的消息:" + message);
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 发生错误时调用
     *
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        logger.info("发生错误");
        error.printStackTrace();
    }

    /**
     * 发送消息
     *
     * @param message
     * @throws IOException
     */
    public void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }


    /**
     * @param message
     * @throws IOException
     */
    public static void sendInfo(String message) throws IOException {
        //获取唯一通道
        WebSocketTest callinSocket = concurrentHashMap.get("aa");
        //推送消息
        callinSocket.sendMessage(message);
    }

    public static synchronized int getOnlineCount() {
        return onlineCount.get();
    }

    public static synchronized void addOnlineCount() {
        WebSocketTest.onlineCount.incrementAndGet();
    }

    public static synchronized void subOnlineCount() {
        WebSocketTest.onlineCount.decrementAndGet();
    }
}
