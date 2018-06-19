package com.changyu.foryou.serviceImpl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.changyu.foryou.mapper.ChatMapper;
import com.changyu.foryou.model.ChatMsg;
import com.changyu.foryou.model.ConcatMsg;
import com.changyu.foryou.service.ChatService;

@Service("chatService")
public class ChatServiceImpl implements ChatService {
	
	private ChatMapper chatMapper;         //操作用户信息

	@Autowired
	public void setUsersMapper(ChatMapper chatMapper) {
		this.chatMapper = chatMapper;
	}
	
	public int addMsg(Map<String, Object> paramMap) {
		return chatMapper.insertSelective(paramMap);
	}
	
	public List<ConcatMsg> getRecentConcat(Map<String, Object> paramMap) {
		return chatMapper.getRecentConcat(paramMap);
	}
	
	public int getUnReadMsgNum(Map<String, Object> paramMap){
		
		return chatMapper.getUnReadMsgNum(paramMap);
	}
	
	public List<ChatMsg> getChatMsg(Map<String, Object> paramMap){
		return chatMapper.getChatMsg(paramMap);
	}
	
	public int setMsgRead(Map<String, Object> paramMap){
		return chatMapper.updateMsgRead(paramMap);
	}

}
