package com.changyu.foryou.controller;

import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.changyu.foryou.model.Users;
import com.changyu.foryou.service.ChatService;
import com.changyu.foryou.service.UserService;
import com.changyu.foryou.tools.TimeUtil;

@Component
public class WebSocketPushHandler implements WebSocketHandler {
    private static final Set<WebSocketSession> users = new HashSet<>();
    
    @Autowired
	private ChatService chatService;
    
    @Autowired
	private UserService userService;
    
    private static final Logger logger = LoggerFactory.getLogger(WebSocketHandler.class);

    // 用户进入系统监听
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        
        users.add(session);
        String userId = session.getAttributes().get("user_id").toString();
        
        //sendMessagesToUsers(new TextMessage("今天晚上服务器维护,请注意"));
        //sendMessageToUser(userId, new TextMessage("连接成功"));
    }

    //
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // 将消息进行转化，因为是消息是json数据，可能里面包含了发送给某个人的信息，所以需要用json相关的工具类处理之后再封装成TextMessage，
        // 我这儿并没有做处理，消息的封装格式一般有{from:xxxx,to:xxxxx,msg:xxxxx}，来自哪里，发送给谁，什么消息等等
        // TextMessage msg = (TextMessage)message.getPayload();
        // 给所有用户群发消息
        //sendMessagesToUsers(msg);
        // 给指定用户群发消息
        //sendMessageToUser(userId, msg);
    	try {     	
        	JSONObject obj = JSON.parseObject(message.getPayload().toString());
        	
       
        	if(message.getPayloadLength()==0){
        		return;
        	}

        	String id = obj.getString("id");
        	String from_id = obj.getString("me");
        	String to_id = obj.getString("to");
        	String msg = obj.getString("text");
        	
        	//Date date = new Date(time);
        	
        	Map<String, Object> paramMap = new HashMap<String, Object>();
        	
        	Date date = new Date();
    		
    		paramMap.put("id", id);
    		paramMap.put("fromId", from_id);
    		paramMap.put("toId", to_id);
    		paramMap.put("msg", msg);
    		paramMap.put("time", date);
    		
    		int flag =chatService.addMsg(paramMap);	
    		
    		if(flag != 0 && flag != -1)
    		{
    			JSONObject to = new JSONObject();
    			to.put("type", "userMsg");
    			to.put("id", id);
    			to.put("msg", msg);
    			to.put("fromId", from_id);
    			to.put("toId", to_id);
    			
    			to.put("time", TimeUtil.DateformatTime(date));
    			
    			Users user =  userService.selectByUserId(from_id);
    			if(user != null)
    			{
    				to.put("fromHeadUrl", user.getImgUrl());
    				to.put("name", user.getNickname());
    			}
    			
    			TextMessage toMsg = new TextMessage(to.toString());
    			
    			sendMessageToUser(to_id, toMsg);
    		}
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
    	
		return;
    }

    // 后台错误信息处理方法
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    	
    	//logger.error("handleTransportError:" + exception.toString());

    }

    // 用户退出后的处理，不如退出之后，要将用户信息从websocket的session中remove掉，这样用户就处于离线状态了，也不会占用系统资源
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        if (session.isOpen()) {
            session.close();
            users.remove(session);
        }
    }
    
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 给所有的用户发送消息
     */
    public void sendMessagesToUsers(TextMessage message) {
        for (WebSocketSession user : users) {
            try {
                // isOpen()在线就发送
                if (user.isOpen()) {
                    user.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 发送消息给指定的用户
     */
    public void sendMessageToUser(String userId, TextMessage message) {
        for (WebSocketSession user : users) {
            if (user.getAttributes().get("user_id").equals(userId)) {
                try {
                    // isOpen()在线就发送
                    if (user.isOpen()) {
                        user.sendMessage(message);
                    }
                    
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}