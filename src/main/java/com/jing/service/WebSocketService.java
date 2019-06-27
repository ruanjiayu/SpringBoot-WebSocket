package com.jing.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jing.dto.MessageDTO;
import com.jing.dto.UserSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**可以使用@Service或者@Component*/
@Service
@Slf4j
/**此注解相当于设置访问URL*/
@ServerEndpoint("/userSession/{username}")
/**
 * @author ruanjiayu
 * @dateTime 2019/6/27 13:43
 */
public class WebSocketService {

    /**websocket封装的session,信息推送，就是通过它来信息推送*/
    private Session session;

    /**用线程安全的CopyOnWriteArraySet来存放客户端连接的信息*/
    private static CopyOnWriteArraySet<UserSession> webSockets =new CopyOnWriteArraySet<>();


    @OnOpen
    public void onOpen(Session session, @PathParam(value="username") String username) {
        this.session = session;
        webSockets.add(new UserSession(username, session));
        log.info("【websocket消息】有新的连接，用户：{}，总数为: {}", username, webSockets.size());
    }

    @OnClose
    public void onClose() {
        webSockets.forEach(client ->{
            if (client.getSession().getId().equals(session.getId())) {
                webSockets.remove(client);
                log.info("【websocket消息】,{} 断开连接，有连接断开总数为: {}", client.getUsername(), webSockets.size());
            }
        });
    }

    /**
     *
     * 发生错误时触发
     * @param error
     */
    @OnError
    public void onError(Throwable error) {
        webSockets.forEach(client ->{
            if (client.getSession().getId().equals(session.getId())) {
                webSockets.remove(client);
                log.error("【websocket消息】发生异常, 用户：{}", client.getUsername());
                error.printStackTrace();
            }
        });
    }

    /**
     * 收到消息时候触发
     * @param message
     */
    @OnMessage
    public void onMessage(String message) {
        log.info("【websocket消息】收到客户端消息: {}", message);
        MessageDTO messageDTO = JSON.parseObject(message, MessageDTO.class);
        webSockets.forEach(client -> {
            if(client.getUsername().equals(messageDTO.getUsername())){
                try {
                    client.getSession().getBasicRemote().sendText("【服务端已经收到消息】:"+ messageDTO.getContent());
                    log.info("【websocket消息】给单用户发送消息");
                } catch (IOException e) {
                    log.error("【websocket发送消息异常】:{}",e);
                }

            }
        });
    }

    /**
     * DEFAULT NULL
     * @param message
     */
    public void sendAllMessage(String message) {
        webSockets.forEach(client ->{
            client.getSession().getAsyncRemote().sendText(message);
            log.info("【websocket消息】广播消息: {}", message);
        });
    }

    /**
     * 单点消息
     * @param username
     * @param message
     */
    public void sendOneMessage(String username, String message) {
        webSockets.forEach(client ->{
            if (username.equals(client.getUsername())) {
                try {
                    client.getSession().getBasicRemote().sendText(message);
                    log.info("【websocket消息】给单用户发消息: {}", username);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***
     * 获取所有在线用户的用户名
     * @return
     */
    public Set<String> getOnLineUsername(){
        Set set = new HashSet<String>();
        webSockets.forEach(client ->{
            set.add(client.getUsername());
        });
        return set;
    }

}
