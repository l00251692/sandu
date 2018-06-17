package com.changyu.foryou.controller;

import java.util.HashMap;
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
	public @ResponseBody Map<String, Object> getMessageListWx(@RequestParam String user_id){

		Map<String,Object> result = new HashMap<String, Object>();
		
		JSONObject data = new JSONObject();
		
		JSONArray array = new JSONArray();
		
		JSONObject node1 = new JSONObject();
		JSONObject node2 = new JSONObject();
		
		//最新消息为node1
		node1.put("img", "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83epuibz5Qwf2IYwnGBLwbWsn8aRHXcrYKvQoVqS5Ls3fnksQfiaQMz9nJLIwJLzpBFoIPMYDKLQdDaPg/132");
		node1.put("name", "xxxdefined");
		node1.put("message", "你好啊");
		node1.put("time", "22:00");
		node1.put("count", "1");
		node1.put("id", "1");//设置消息fromid
		
		node2.put("img", "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83epuibz5Qwf2IYwnGBLwbWsn8aRHXcrYKvQoVqS5Ls3fnksQfiaQMz9nJLIwJLzpBFoIPMYDKLQdDaPg/132");
		node2.put("name", "xxxdefined2");
		node2.put("message", "你好啊的考拉合法拉好地方");
		node2.put("time", "21:00");
		node2.put("count", "2");
		node2.put("id", "1");
		
		
		array.add(node1);
		array.add(node2);
		
		data.put("list", array);
		
		System.out.println("getMessageListWx:" + data.toString());
		

      	result.put("State", "Success");
        result.put("data", data);
		
		return result;
		
	}
	
	
	@RequestMapping("/getMessageWx")
	public @ResponseBody Map<String, Object> getMessageWx(@RequestParam String user_id, @RequestParam String from_id){

		Map<String,Object> result = new HashMap<String, Object>();
		
		JSONObject data = new JSONObject();
		
		JSONArray array = new JSONArray();
		
		JSONObject node1 = new JSONObject();
		JSONObject node2 = new JSONObject();
		JSONObject node3 = new JSONObject();
		
		//最新消息为node1
		node1.put("img", "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83epuibz5Qwf2IYwnGBLwbWsn8aRHXcrYKvQoVqS5Ls3fnksQfiaQMz9nJLIwJLzpBFoIPMYDKLQdDaPg/132");
		node1.put("name", "xxxdefined");
		node1.put("me", user_id);
		node1.put("text", "你好啊");
		node1.put("time", "22:00");

		
		node2.put("img", "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83epuibz5Qwf2IYwnGBLwbWsn8aRHXcrYKvQoVqS5Ls3fnksQfiaQMz9nJLIwJLzpBFoIPMYDKLQdDaPg/132");
		node2.put("name", "xxxdefined2");
		node2.put("text", "你好啊的考拉合法拉好地方");
		node2.put("time", "21:00");
		
		node1.put("img", "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83epuibz5Qwf2IYwnGBLwbWsn8aRHXcrYKvQoVqS5Ls3fnksQfiaQMz9nJLIwJLzpBFoIPMYDKLQdDaPg/132");
		node1.put("name", "xxxdefined");
		node1.put("me", user_id);
		node1.put("text", "你好啊");
		node1.put("time", "22:00");

		
		node3.put("img", "https://wx.qlogo.cn/mmopen/vi_32/DYAIOgq83epuibz5Qwf2IYwnGBLwbWsn8aRHXcrYKvQoVqS5Ls3fnksQfiaQMz9nJLIwJLzpBFoIPMYDKLQdDaPg/132");
		node3.put("name", "xxxdefined2");
		node3.put("text", "你好啊的考拉合法拉好地方打开了回复阿考虑到好发拉的横幅"
				+ "大的老虎拉的横幅阿考虑到好发埃里克地方哈阿里客服电话卡了点发货");
		node3.put("time", "21:00");
		
		
		array.add(node1);
		array.add(node2);
		array.add(node3);
		
		data.put("message", array);
		data.put("from_name", "用户名字");
		
		System.out.println("getMessageListWx:" + data.toString());
		

      	result.put("State", "Success");
        result.put("data", data);
		
		return result;
		
	}

}
