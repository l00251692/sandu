package com.changyu.foryou.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.changyu.foryou.model.Order;
import com.changyu.foryou.service.OrderService;
import com.changyu.foryou.service.UserService;
import com.changyu.foryou.tools.Constants;

@Controller
@RequestMapping("/order")
public class OrderControler {
	private OrderService orderService;
	private UserService userService;
	
	@Resource  
    private WebSocketPushHandler webSocketHandler;  
	
	@Autowired
	public void setOrderService(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@Autowired
	public void setUserServce(UserService userService) {
		this.userService = userService;
	}
	
	/**
	 * 生成订单
	 * 
	 * @param phoneId
	 * @param foodId
	 * @param foodCount
	 * @param foodSpecial
	 * @return
	 */
	@RequestMapping("/addOrderWx")
	public @ResponseBody Map<String, Object> addOrderWx(
			@RequestParam String from_add,  @RequestParam String from_add_detail, @RequestParam String from_add_longitude,@RequestParam String from_add_latitude,
			@RequestParam String to_add,  @RequestParam String to_add_detail, @RequestParam String to_add_longitude,@RequestParam String to_add_latitude,
			@RequestParam String time, @RequestParam String user_id){
		Map<String,Object> map = new HashMap<String, Object>();
		
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			
			Date createTime =  new Date();
			paramMap.put("createUser",user_id);
			paramMap.put("fromAddr",from_add);
			paramMap.put("fromAddrDetai",from_add_detail);
			paramMap.put("fromAddrLongitude",from_add_longitude);
			paramMap.put("fromAddrLatitude",from_add_latitude);
			paramMap.put("toAddr",to_add);
			paramMap.put("toAddrDetai",to_add_detail);
			paramMap.put("toAddrLongitude",to_add_longitude);
			paramMap.put("toAddrLatitude",to_add_latitude);
			paramMap.put("createTime",createTime);
			
			if(time.equals("now"))
			{
				paramMap.put("departTime",createTime);
				paramMap.put("orderType",Constants.orderNow);
			}
			else
			{
				paramMap.put("departTime",time);
				paramMap.put("orderType",Constants.orderLater);
			}
			
			
			paramMap.put("orderStatus",Constants.orderStatusCreate);
			paramMap.put("orderPrice",1.0);
			
			JSONArray records = new JSONArray();
			JSONObject record = new JSONObject();
			record.put("status", Constants.orderStatusCreate);
			record.put("time", createTime);
			records.add(record);
			paramMap.put("records",records.toString());
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Random random = new Random();  
			String result="";  
			for (int i=0;i<4;i++)  
			{  
			    result+=random.nextInt(10);  
			}  
			String orderId = sdf.format(createTime) + result;
			
			paramMap.put("orderId",orderId);
			System.out.println("[ADD ORDER]: orderId=" + orderId);
			
			int flag = orderService.addOrder(paramMap);
			if(flag != 0 && flag != -1)
			{
				JSONObject obj = new JSONObject();
				obj.put("orderId", orderId);
				
				map.put("State", "Success");
				map.put("data", obj);	

				return map;
			}
			
			
			
		} catch (Exception e) {
			System.out.println(e);
		}
		
		map.put("State", "Fail");
		map.put("data", null);	

		return map;
	}
}
