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
			node.put("time", "22:00");
			node.put("count", num);
			node.put("id", concat.getConcatId());//设置消息fromid
			
			array.add(node);
		}
		
		JSONObject node2 = new JSONObject();
		node2.put("img", "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83epuibz5Qwf2IYwnGBLwbWsn8aRHXcrYKvQoVqS5Ls3fnksQfiaQMz9nJLIwJLzpBFoIPMYDKLQdDaPg/132");
		node2.put("name", "xxxdefined2");
		node2.put("message", "你好啊的考拉合法拉好地方");
		node2.put("time", "21:00");
		node2.put("count", "2");
		node2.put("id", "1");
		
		array.add(node2);
		
		data.put("list2", array);
		data.put("page", page);
		data.put("count", concatList.size());
	

      	result.put("State", "Success");
        result.put("data", data);
		
		return result;
		
	}
	
	
	@RequestMapping("/getMessageWx")
	public @ResponseBody Map<String, Object> getMessageWx(@RequestParam String user_id, @RequestParam String from_id, @RequestParam Integer page){

		Map<String,Object> result = new HashMap<String, Object>();
		
		System.out.println("getMessageWx:" + user_id + "," + from_id);
		
		JSONObject data = new JSONObject();
		
		JSONArray array = new JSONArray();

		Map<String, Object> paramMap = new HashMap<String, Object>();
		

		paramMap.put("userId", user_id);
		paramMap.put("concatId", from_id);//默认一次5条
		paramMap.put("limit", 10);
		paramMap.put("offset", page * 10);
		
		List<ChatMsg> msgList =  chatService.getChatMsg(paramMap);
		
		for(int i = msgList.size(); i >0 ; i--)
		{
			JSONObject node = new JSONObject();
			
			ChatMsg msg = msgList.get(i-1);
			
			String id = "ID_" + String.valueOf(page * 10 + msgList.size() - i );
			
			if(msg.getFromId().equals(user_id) && msg.getToId().equals(from_id))
			{
				node.put("img", msg.getFromHeadUrl());
				node.put("me", user_id);
				node.put("text", msg.getMsg());
				node.put("time", msg.getTime());
				node.put("id", id);
			}
			else if(msg.getFromId().equals(from_id) && msg.getToId().equals(user_id))
			{
				node.put("img", msg.getToHeadUrl());
				node.put("text", msg.getMsg());
				node.put("time", msg.getTime());
				node.put("id", id);
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
	
      	result.put("State", "Success");
        result.put("data", data);
		
		return result;
		
	}
	
	@RequestMapping("/sendMsgWx")
	public @ResponseBody Map<String, Object> sendMsgWx(@RequestParam String user_id, @RequestParam String to_id,@RequestParam String msg){

		Map<String,Object> result = new HashMap<String, Object>();
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		System.out.println("sendMsgWx:" + msg);
		
		paramMap.put("fromId", user_id);//默认一次5条
		paramMap.put("toId", to_id);
		paramMap.put("msg", msg);//默认一次5条
		paramMap.put("time", new Date());
		paramMap.put("toId", to_id);

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

}
