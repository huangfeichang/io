package com.socket.controller;

import com.socket.socketServer.WebSocketTest;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

/**
 * @author huangfeichang.
 * @date 2018/12/18 0018.
 */
@RestController
@RequestMapping(value = "/backend/socketTest")
public class SocketTestController {
    @RequestMapping(value = "/sendMsg")
    public void send(String msg) {
        try {
            System.err.println("----------------------");
            WebSocketTest.sendInfo("rrvaawwdas");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
