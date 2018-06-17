package com.changyu.foryou.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.changyu.foryou.mapper.ChatMapper;
import com.changyu.foryou.service.ChatService;

@Service("chatService")
public class ChatServiceImpl implements ChatService {
	
	private ChatMapper chatMapper;         //操作用户信息

	@Autowired
	public void setUsersMapper(ChatMapper chatMapper) {
		this.chatMapper = chatMapper;
	}

}
