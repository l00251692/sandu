package com.changyu.foryou.controller;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.swing.plaf.synth.SynthSeparatorUI;

import org.apache.log4j.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.changyu.foryou.model.Users;
import com.changyu.foryou.model.WeChatContext;
import com.changyu.foryou.service.UserService;
import com.changyu.foryou.tools.AesCbcUtil;
import com.changyu.foryou.tools.Constants;
import com.changyu.foryou.tools.HttpRequest;
import com.changyu.foryou.tools.Md5;
import com.google.gson.JsonObject;


@Controller
@RequestMapping("/user")
public class UserController {
	@Autowired
	private UserService userService;
	
	private static final Logger LOGGER = Logger.getLogger(UserController.class);
	
	/**
	 * 用户登陆
	 * @param phone
	 * @param password
	 * @return
	 */
	@RequestMapping(value="/toLoginWx")
	public @ResponseBody
	Map<String, Object> toLoginWx(@RequestParam String wx_code,@RequestParam String encryptedData,@RequestParam String iv) {
		Map<String, Object> map = new HashMap<String, Object>();
			
		//登录凭证不能为空 
		if (wx_code == null || wx_code.length() == 0) 
		{ 
			map.put("State", "Fail"); 
			map.put("info", "登陆失败"); 
			return map; 	
		} 

		//小程序唯一标识  (在微信小程序管理后台获取) 
		String wxspAppid = Constants.appId;
		//小程序的 app secret (在微信小程序管理后台获取) 
		String wxspSecret = Constants.apiKey;
	    //授权（必填） 
		String grant_type = "authorization_code"; 
	
	
		//////////////// 1、向微信服务器 使用登录凭证 code 获取 session_key 和 openid //////////////// 
		//请求参数 
		String params = "appid=" + wxspAppid + "&secret=" + wxspSecret + "&js_code=" + wx_code + "&grant_type=" + grant_type; 
		//发送请求 
		String sr = HttpRequest.sendGet("https://api.weixin.qq.com/sns/jscode2session", params); 
		//解析相应内容（转换成json对象） 
		//JSONObject json = JSONObject.fromObject(sr); 
		JSONObject json = JSONObject.parseObject(sr);
		//获取会话密钥（session_key） 
		String session_key = json.get("session_key").toString(); 
		//用户的唯一标识（openid） 
		String openid = (String) json.get("openid"); 
	
		//////////////// 2、对encryptedData加密数据进行AES解密 //////////////// 
		try { 
		  String result = AesCbcUtil.decrypt(encryptedData, session_key, iv, "UTF-8");
		  if (null != result && result.length() > 0) 
		  { 
		
			JSONObject userInfoJSON = JSONObject.parseObject(result); 
			
			JSONObject userInfo = new JSONObject(); 
			userInfo.put("openId", userInfoJSON.get("openId")); 
			userInfo.put("nickName", userInfoJSON.get("nickName")); 
			userInfo.put("gender", userInfoJSON.get("gender")); 
			userInfo.put("city", userInfoJSON.get("city")); 
			userInfo.put("province", userInfoJSON.get("province")); 
			userInfo.put("country", userInfoJSON.get("country")); 
			userInfo.put("avatarUrl", userInfoJSON.get("avatarUrl")); 
			userInfo.put("user_id", userInfoJSON.get("openId")); 
			userInfo.put("user_token", userInfoJSON.get("unionId"));

			JSONObject loginInfo = new JSONObject();
			loginInfo.put("is_login", "1");
			loginInfo.put("session_key", session_key);
			loginInfo.put("userInfo", userInfo); 
			
			try {
				//SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
				// = df.format(new Date());// new Date()为获取当前系统时间
				
				Users users = userService.checkLogin(userInfoJSON.get("openId").toString());
				if (users != null)
				{
					users.setImgUrl(userInfoJSON.get("avatarUrl").toString());
					users.setNickname(userInfoJSON.get("nickName").toString());
					users.setLastLoginDate(new Date());
					users.setSex(new Short(userInfoJSON.get("gender").toString()));
					userService.updateUserInfo(users);
				}
				else
				{
					Users users2 = new Users(userInfoJSON.get("openId").toString(), userInfoJSON.get("nickName").toString(),
							userInfoJSON.get("avatarUrl").toString(),userInfoJSON.get("gender").toString());
					userService.addUsers(users2);
				}				
			} catch (Exception e) {
				System.out.println(e);
			}
			
			map.put("State", "Success"); 
			map.put("data", loginInfo); 
			return map; 
		  }
		} 
		catch (Exception e) 
		{ 
			e.printStackTrace(); 
		} 
		
		map.put("State", "Fail"); 
		map.put("info", "登陆失败"); 
		return map; 	
	
	}


	/**
	 * 获取我的用户总信息
	 * @param phone 用户id
	 * @return
	 */
	@RequestMapping(value="getMineInfoWx")
	public @ResponseBody Map<String, Object> getMineInfoWx(@RequestParam String user_id){
		Map<String, Object> map = new HashMap<String, Object>();
	
		Users users=userService.selectByUserId(user_id);
		if(users == null)
		{
			map.put("State", "Fail"); 
			map.put("info", "获得我的信息失败"); 
			return map; 
		}
		
		JSONObject obj = new JSONObject();
		obj.put("phone", users.getPhone());
		obj.put("registType", users.getType());
		obj.put("ballance", users.getBallance());
	
			
		map.put("State", "Success"); 
		map.put("data", obj); 
		return map;
	}
	
	/**
	 * 获取我的用户总信息
	 * @param phone 用户id
	 * @return
	 * @throws ClientException 
	 */
	@RequestMapping(value="getCheckCode")
	public @ResponseBody Map<String, Object> getCheckCodeWx(@RequestParam String phone) throws ClientException{
		Map<String, Object> map = new HashMap<String, Object>();
	
		//可自助调整超时时间
        System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
        System.setProperty("sun.net.client.defaultReadTimeout", "10000");
        
        String accessKeyId = "LTAI4CGPrpZbmFZx";
        String accessKeySecret = "6BMA3kW58dUpwPOSpHW6Awy95K07Jf";
        //短信API产品名称
        String product="Dysmsapi";
        //短信API产品域名
        String domain="dysmsapi.aliyuncs.com";

        //初始化acsClient,暂不支持region化
        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, accessKeySecret);
        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
        IAcsClient acsClient = new DefaultAcsClient(profile);
        
        int codeNum = (int)(Math.random()*9000)+1000;  //每次调用生成一次四位数的随机数

        //组装请求对象-具体描述见控制台-文档部分内容
        SendSmsRequest request = new SendSmsRequest();
        //必填:待发送手机号
        request.setPhoneNumbers(phone);
        //必填:短信签名-可在短信控制台中找到
        request.setSignName("2333一起打伞");
        //必填:短信模板-可在短信控制台中找到
        request.setTemplateCode("SMS_137180089");
        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
        request.setTemplateParam("{\"code\":\""+ codeNum + "\"}");

        //选填-上行短信扩展码(无特殊需求用户请忽略此字段)
        //request.setSmsUpExtendCode("90997");

        //可选:outId为提供给业务方扩展字段,最终在短信回执消息中将此值带回给调用者
        //request.setOutId("yourOutId");

        //hint 此处可能会抛出异常，注意catch
        SendSmsResponse sendSmsResponse = acsClient.getAcsResponse(request);
        
        //SendSmsResponse sendSmsResponse = new SendSmsResponse();
        sendSmsResponse.setCode("OK");
        
        if(sendSmsResponse.getCode() != null && sendSmsResponse.getCode().equals("OK")) {

        	JSONObject obj = new JSONObject();
    		obj.put("codeNumStr", codeNum + "");
    			
    		map.put("State", "Success"); 
    		map.put("data", obj);
		}else {
			LOGGER.error("发送验证码失败！code=" + sendSmsResponse.getCode());
		    map.put("State", "Fail"); 
    		map.put("data", "");
		}
        
		return map;
	}
	
	/**
	 * 获取我的用户总信息
	 * @param phone 用户id
	 * @return
	 */
	@RequestMapping(value="bindPhone")
	public @ResponseBody Map<String, Object> bindPhoneWx(@RequestParam String user_id,@RequestParam String phone){
		Map<String, Object> map = new HashMap<String, Object>();
	
		if(user_id != null && phone != null)
		{
			Users users=userService.selectByUserId(user_id);
			if(users == null)
			{
				map.put("State", "Fail"); 
				map.put("info", "获得我的信息失败"); 
				return map; 
			}
			
			users.setPhone(phone);
			int flag = userService.updateUserInfo(users);
			if(flag !=0 && flag != -1)
			{
				map.put("State", "Success"); 
				map.put("data", "OK"); 
			}
			else
			{
				map.put("State", "Fail"); 
				map.put("info", "绑定手机号失败"); 
			}	
		}
		else
		{
			map.put("State", "Fail"); 
			map.put("data", "用户或手机号为空"); 
		}
		
		
		return map;
	}
	
	/**
	 * 获取我的用户总信息
	 * @param phone 用户id
	 * @return
	 */
	@RequestMapping(value="registerSanWx")
	public @ResponseBody Map<String, Object> registerSanWx(@RequestParam String user_id,@RequestParam String color_reg,
			@RequestParam String style_reg, @RequestParam String feature_reg){
		Map<String, Object> map = new HashMap<String, Object>();
	

		Users users=userService.selectByUserId(user_id);

		if(users == null)
		{
			map.put("State", "Fail"); 
			map.put("info", "获得我的信息失败"); 
			return map; 
		}
		
		users.setSanColor(color_reg);
		users.setSanStyle(style_reg);
		users.setType((short)2);
		
		if(!feature_reg.equals("no")){
			users.setSanFeature(feature_reg);
		}
		
		int flag = userService.updateUserSanInfo(users);
		if(flag !=0 && flag != -1)
		{
			map.put("State", "Success"); 
			map.put("data", "OK"); 
		}
		else
		{
			map.put("State", "Fail"); 
			map.put("info", "绑定信息失败"); 
		}		
		
		return map;
	}
	
	/**
	 * 获取我的用户总信息
	 * @param phone 用户id
	 * @return
	 */
	@RequestMapping(value="getRegSanInfoWx")
	public @ResponseBody Map<String, Object> getRegSanInfoWx(@RequestParam String user_id){
		Map<String, Object> map = new HashMap<String, Object>();
	

		Users users=userService.selectByUserId(user_id);
		
		if(users == null)
		{
			map.put("State", "Fail"); 
			map.put("info", "获得我的信息失败"); 
			return map; 
		}
		
		JSONObject obj = new JSONObject();
		obj.put("color", users.getSanColor());
		obj.put("style", users.getSanStyle());
		obj.put("feature", users.getSanFeature());
		
		map.put("State", "Success"); 
		map.put("data", obj); 
		
		return map;
	}
	
	/**
	 * 获取我的用户总信息
	 * @param phone 用户id
	 * @return
	 */
	@RequestMapping(value="updateUserBallanceWx")
	public @ResponseBody Map<String, Object> updateUserBallanceWx(@RequestParam String user_id){
		Map<String, Object> map = new HashMap<String, Object>();
	
		map.put("State", "Success"); 
		map.put("data", ""); 	
		return map;
	}
}
