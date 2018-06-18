package com.changyu.foryou.service;

import java.util.List;
import java.util.Map;

import com.changyu.foryou.model.ChatMsg;
import com.changyu.foryou.model.ConcatMsg;

public interface ChatService {
	
	public int addMsg(Map<String, Object> paramMap);
	
	public List<ConcatMsg> getRecentConcat(Map<String, Object> paramMap);
	
	public int getUnReadMsgNum(Map<String, Object> paramMap);
	
	public List<ChatMsg> getChatMsg(Map<String, Object> paramMap);

}
