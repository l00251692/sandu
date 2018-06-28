package com.changyu.foryou.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.changyu.foryou.model.ChatMsg;
import com.changyu.foryou.model.ConcatMsg;
import com.changyu.foryou.model.Order;
import com.changyu.foryou.model.Users;
import com.changyu.foryou.service.ChatService;
import com.changyu.foryou.service.UserService;
import com.changyu.foryou.tools.Constants;
import com.changyu.foryou.tools.PayUtil;
import com.changyu.foryou.tools.StringUtil;
import com.changyu.foryou.tools.TimeUtil;

@Controller
@RequestMapping("/chat")
public class ChatController {
	
	@Autowired
	private ChatService chatService;
	
	@Autowired
	private UserService userService;
	
	private static final Logger LOGGER = Logger.getLogger(ChatController.class);
    
    /**
	 * 获得微信支付参数
	 * 
	 * @param phoneId
	 * @param foodId
	 * @param foodCount
	 * @param foodSpecial
	 * @return
	 */
	@RequestMapping("/getMessageListWx")
	public @ResponseBody Map<String, Object> getMessageListWx(@RequestParam String user_id, @RequestParam Integer page){

		Map<String,Object> result = new HashMap<String, Object>();
		
		int unReadCount = 0;
		
		JSONObject data = new JSONObject();
		
		JSONArray array = new JSONArray();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		

		paramMap.put("userId", user_id);//默认一次5条
		paramMap.put("limit", 10);
		paramMap.put("offset", page * 10);//默认一次5条

		List<ConcatMsg>  concatList= chatService.getRecentConcat(paramMap);
		
		for(ConcatMsg concat: concatList)
		{
			JSONObject node = new JSONObject();
			
			Users user = userService.selectByUserId(concat.getConcatId());
			
			if(user != null)
			{
				node.put("img", user.getImgUrl());
				node.put("name", user.getNickname());
			}
			
			Map<String, Object> paramMap2 = new HashMap<String, Object>();
			paramMap2.put("userId", user_id);
			paramMap2.put("concatId", concat.getConcatId());

			int num = chatService.getUnReadMsgNum(paramMap2);	
			
			node.put("message", concat.getMsg());
			node.put("time", TimeUtil.DateformatTime(concat.getTime()));
			node.put("count", num);
			node.put("id", concat.getConcatId());//设置消息fromid
			
			unReadCount = unReadCount + num;
			
			array.add(node);
		}
		
		data.put("list2", array);
		data.put("page", page);
		data.put("count", concatList.size());
		data.put("unReadCount", unReadCount);
	

      	result.put("State", "Success");
        result.put("data", data);
		
		return result;
		
	}
	
	
	@RequestMapping("/getMessageWx")
	public @ResponseBody Map<String, Object> getMessageWx(@RequestParam String user_id, @RequestParam String from_id, @RequestParam Integer page){

		Map<String,Object> result = new HashMap<String, Object>();
			
		JSONObject data = new JSONObject();
		
		JSONArray array = new JSONArray();

		Map<String, Object> paramMap = new HashMap<String, Object>();
		

		paramMap.put("userId", user_id);
		paramMap.put("concatId", from_id);
		paramMap.put("limit", 10);
		paramMap.put("offset", page * 10);
		
		List<ChatMsg> msgList =  chatService.getChatMsg(paramMap);
		
		for(int i = msgList.size(); i >0 ; i--)
		{
			JSONObject node = new JSONObject();
			
			ChatMsg msg = msgList.get(i-1);
			
			
			if(msg.getFromId().equals(user_id) && msg.getToId().equals(from_id))
			{
				node.put("img", msg.getFromHeadUrl());
				node.put("me", user_id);
				node.put("text", msg.getMsg());
				node.put("time", TimeUtil.DateformatTime(msg.getTime()));
				node.put("id", msg.getId());
			}
			else if(msg.getFromId().equals(from_id) && msg.getToId().equals(user_id))
			{
				node.put("img", msg.getFromHeadUrl());
				node.put("text", msg.getMsg());
				node.put("time", TimeUtil.DateformatTime(msg.getTime()));
				node.put("id", msg.getId());
			}
		
			array.add(node);
		}
		
		Users user = userService.selectByUserId(from_id);
		
		if(user != null)
		{
			data.put("concat_name", user.getNickname());
		}
		
		data.put("list", array);
		data.put("count", array.size());
		data.put("page", page);
		data.put("newId", msgList.get(0).getId());
	
      	result.put("State", "Success");
        result.put("data", data);
		
		return result;
		
	}
	
	@RequestMapping("/sendMsgWx")
	public @ResponseBody Map<String, Object> sendMsgWx(@RequestParam String user_id, @RequestParam String to_id,@RequestParam String msg){

		Map<String,Object> result = new HashMap<String, Object>();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		paramMap.put("fromId", user_id);//默认一次5条
		paramMap.put("toId", to_id);
		paramMap.put("msg", msg);//默认一次5条
		paramMap.put("time", new Date());

		int flag =chatService.addMsg(paramMap);
		
		if(flag != 0 && flag != -1)
		{
			result.put("State", "Success");
	        result.put("data", "ok");
		}
		else{
			result.put("State", "Fail");
	        result.put("info", "消息发送失败");
		}
		return result;	
	}
	
	
	@RequestMapping("/setMsgReadWx")
	public @ResponseBody Map<String, Object> setMsgReadWx(@RequestParam String user_id, @RequestParam String concat_id){

		Map<String,Object> result = new HashMap<String, Object>();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		paramMap.put("fromId", concat_id);
		paramMap.put("toId", user_id);
		paramMap.put("isRead", 1);
		


		int flag =chatService.setMsgRead(paramMap);

		result.put("State", "Success");
	    result.put("data", "ok");

		return result;	
	}

}
