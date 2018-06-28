package com.changyu.foryou.tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class ToolUtil {
	
	public static Boolean isNearBy(String from_longitude,String from_latitude, String to_longitude,String to_latitude){
		
		////////////////1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid //////////////// 
		//请求参数 
		String params = "mode=walking"  
				+ "&from=" + from_latitude + "," + from_longitude 
				+ "&to=" + to_latitude + "," + to_longitude
				+ "&key=" + Constants.QQMAPKEY; 
		//发送请求 
		String sr = HttpRequest.sendGet("http://apis.map.qq.com/ws/distance/v1/", params); 
		
		System.out.println("isNearByOrder enter");
		
		//获取会话密钥（session_key） 
		int status = JSONObject.parseObject(sr).getIntValue("status");
		
		if(status == 0)
		{
			JSONObject jsonObject = JSONObject.parseObject(sr).getJSONObject("result");		
			JSONArray infoArray = jsonObject.getJSONArray("elements");
			
			if(infoArray.size() > 0)
			{
				float distance = infoArray.getJSONObject(0).getFloatValue("distance");
				
				if(distance != -1 && distance < 5*1000)
				{
					return true;
				}
				
				return false;
			}
			else
			{
				return false;
			}	
			
		}
		else{
			System.out.println("calcalute distance err:status=" + String.valueOf(status));
			return false;
		}
		
	}

}
