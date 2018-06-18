package com.changyu.foryou.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.changyu.foryou.model.ChatMsg;
import com.changyu.foryou.model.ConcatMsg;

public interface ChatMapper {
	
	
	int insertSelective(Map<String, Object> paramMap);
	
	public List<ConcatMsg> getRecentConcat(Map<String, Object> paramMap);
	
	public int getUnReadMsgNum(Map<String, Object> paramMap);

	public List<ChatMsg> getChatMsg(Map<String, Object> paramMap);
}
