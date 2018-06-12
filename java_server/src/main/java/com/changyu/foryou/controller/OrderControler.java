package com.changyu.foryou.controller;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.changyu.foryou.model.DSHOrder;
import com.changyu.foryou.model.Order;
import com.changyu.foryou.model.Users;
import com.changyu.foryou.service.DelayService;
import com.changyu.foryou.service.OrderService;
import com.changyu.foryou.service.RedisService;
import com.changyu.foryou.service.UserService;
import com.changyu.foryou.tools.Constants;
import com.changyu.foryou.tools.ThreadPoolUtil;
import com.changyu.foryou.tools.ToolUtil;

import cn.jpush.api.report.UsersResult.User;

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
	
	@Autowired  
    private DelayService delayService;  
    @Autowired  
    private RedisService redisServie; 
    
    private static final Logger logger = LoggerFactory.getLogger(OrderControler.class);
	
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
	public @ResponseBody Map<String, Object> addOrderWx(@RequestParam String city_name,  @RequestParam String district_name,
			@RequestParam String from_add,  @RequestParam String from_add_detail, @RequestParam String from_add_longitude,@RequestParam String from_add_latitude,
			@RequestParam String to_add,  @RequestParam String to_add_detail, @RequestParam String to_add_longitude,@RequestParam String to_add_latitude,
			@RequestParam String time, @RequestParam String user_id){
		Map<String,Object> map = new HashMap<String, Object>();
		
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			
			System.out.println("addOrderWx: toaddr=" + to_add);
			Date createTime =  new Date();
			paramMap.put("createUser",user_id);
			paramMap.put("cityName",city_name);
			paramMap.put("districtName",district_name);
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
	
	@RequestMapping("/cancelOrderWx")
	public @ResponseBody Map<String, String> cancelOrderWx(
			@RequestParam String user_id,  @RequestParam String order_id){
		Map<String,String> result = new HashMap<String, String>();
		JSONObject node = new JSONObject();
		
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("orderId",order_id);
			Order order = orderService.getOrderByIdWx(paramMap);
			if(order == null)
			{
				result.put("State", "Fail");
				result.put("info", "生成订单失败");	

				return result;
			}

			paramMap.put("orderStatus",Constants.orderStatusCancel);
			
			JSONArray recordes = JSON.parseArray(order.getRecords());
			JSONObject record = new JSONObject();
			record.put("status",Constants.orderStatusCancel);
			record.put("time", new Date());
			recordes.add(record);

			paramMap.put("records",recordes.toString());
			
			
			int flag = orderService.updateOrderStatus(paramMap);
			if (flag != -1 && flag != 0)
			{
				result.put("State", "Success");
				result.put("data", null);	
				return result;
			} 
			else 
			{
				result.put("State", "Fail");
				result.put("info", null);	
				return result;
			}
				
		} catch (Exception e) {
			System.out.println(e);
		}
		
		result.put("State", "Fail");
		result.put("info", null);	

		return result;
	}
	
	/**
	 * 获取下达的所有订单
	 * 
	 * @param phoneId
	 *            ,status
	 * @return
	 */
	@RequestMapping("/getDistanceOrdersWx")
	public @ResponseBody Map<String, Object> getDistanceOrdersWx(@RequestParam String city_name,@RequestParam String district_name,
			@RequestParam Integer page,@RequestParam String my_longitude,@RequestParam String my_latitude) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			
			paramMap.put("limit", 10);
			paramMap.put("offset", page * 10);//默认一次5条
			paramMap.put("cityName", city_name);
			paramMap.put("districtName", district_name);
	
			List<Order> ordersList =orderService.getNearByOrders(paramMap);
			
			System.out.println("getDistanceOrders size:" + ordersList.size());
			
			
			
			JSONArray orderArray = new JSONArray();
			
			for(Order order:ordersList)
			{
				JSONObject obj = new JSONObject();
				System.out.println("getDistanceOrders:1");
				if(ToolUtil.isNearByOrder(my_longitude, my_latitude, order.getFromAddrLongitude(), order.getFromAddrLatitude()))
				{
					System.out.println("getDistanceOrders:2");
					Users user = userService.selectByUserId(order.getCreateUser());
					obj.put("create_time", order.getCreateTime());
					obj.put("depart_time", order.getDepartTime());
					obj.put("passenger_name", user.getNickname());
					obj.put("passenger_url", user.getImgUrl());
					obj.put("order_id", order.getOrderId());
					obj.put("from_addr", order.getFromAddr());
					obj.put("from_addr_detail", order.getFromAddrDetai());
					obj.put("to_addr", order.getToAddr());
					obj.put("to_addr_detail", order.getToAddrDetai());
					
					orderArray.add(obj);
				}	
			}
			
			JSONObject data = new JSONObject();
			data.put("list",orderArray);
			data.put("page",String.valueOf(page));

			map.put("State", "Success");
			map.put("data", data);	
			return map;

		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		map.put("State", "False");
		map.put("data", null);	
		return map;
	}
	
	/**
	 * 获取订单具体信息
	 * 
	 * @param phoneId
	 *            ,status
	 * @return
	 */
	@RequestMapping("/getOrderInfoWx")
	public @ResponseBody Map<String, Object> getOrderInfoWx(
			@RequestParam String  user_id, @RequestParam String order_id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("orderId",order_id);
			Order order = orderService.getOrderByIdWx(paramMap);
			
			if (order == null)
			{
				map.put("State", "Fail");
				map.put("data", "查询订单详细信息失败");	
				return map;
			}
		
			JSONObject data = new JSONObject();
			JSONObject obj_driver = new JSONObject();
			JSONObject obj_passenger = new JSONObject();
			JSONObject obj_order = new JSONObject();
			
			Users driver = userService.selectByUserId(order.getReceiveUser());
			if(driver != null)
			{
				obj_driver.put("name", driver.getNickname());
				obj_driver.put("head", driver.getImgUrl());
				obj_driver.put("phone", driver.getPhone());
				obj_driver.put("stars", 5);
				obj_driver.put("tip", "这个人还没有介绍");
				obj_driver.put("cart", "蓝色*透明");
			}
			
			Users passenger = userService.selectByUserId(order.getCreateUser());
			obj_passenger.put("name", passenger.getNickname());
			obj_passenger.put("head", passenger.getImgUrl());
			obj_passenger.put("phone", passenger.getPhone());
			obj_passenger.put("stars", 5);
			obj_passenger.put("tip", "这个人还没有介绍");
			
			obj_order.put("strLatitude", order.getFromAddrLatitude());
			obj_order.put("strLongitude", order.getFromAddrLongitude());
			obj_order.put("endLatitude", order.getToAddrLatitude());
			obj_order.put("endLongitude", order.getToAddrLongitude());
			obj_order.put("fromAddr", order.getFromAddr());
			obj_order.put("fromAddrDetail", order.getFromAddrDetai());
			obj_order.put("toAddr", order.getToAddr());
			obj_order.put("toAddrDetail", order.getToAddrDetai());
			obj_order.put("createTime", order.getCreateTime());
			
			
			data.put("driver", obj_driver);
			data.put("passenger", obj_passenger);
			data.put("order", obj_order);

			map.put("State", "Success");
			map.put("data", data);	
			return map;

		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		map.put("State", "False");
		map.put("data", null);	
		return map;
	}
	
	/**
	 * 商家接单
	 * 
	 * @return
	 */
	@RequestMapping("/setRecvOrderWx")
	public @ResponseBody Map<String, Object> setRecvOrderWx(
			@RequestParam String  user_id, @RequestParam String order_id) {
		Map<String, Object> map = new HashMap<String, Object>();

		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("orderId",order_id);
		Order order = orderService.getOrderByIdWx(paramMap);
		if (order == null)
		{
			map.put("State", "False");
			map.put("data", "查询订单详细信息失败");	
			return map;
		}
		
		JSONArray records = JSON.parseArray(order.getRecords());
		JSONObject record = new JSONObject();
		record.put("status",Constants.orderStatusRecv);
		record.put("time", new Date());
		records.add(record);
		
		paramMap.put("records",records.toJSONString());
		paramMap.put("orderStatus",Constants.orderStatusRecv);
		paramMap.put("receiveUser",user_id);
		
		int flage = orderService.updateOrderReceiver(paramMap);
		if(flage != -1 && flage !=0)
		{
			//订单已经被接手，下一个状态就是结束行程，如果用户未确认则时间到后自动结束行程
	        ThreadPoolUtil.execute(new Runnable(){  
	            public void run(){  
	                //从delay队列删除  
	                delayService.remove(order_id);  
	                //从redis删除  
	                redisServie.delete(Constants.REDISPREFIX+order_id); 
	                
	                //重新添加等待结束行程的延迟队列
	                DSHOrder dshOrder = new DSHOrder(order_id,Constants.orderStatusRecv, Constants.WAIT_FINISH);  
	                delayService.add(dshOrder);  
	        
	                //2插入到redis  
	                redisServie.add(Constants.REDISPREFIX+order_id, dshOrder, Constants.REDISSAVETIME);  
	            }  
	        });
			map.put("State", "Success");
			map.put("data", null);	
		}
		else
		{
			map.put("State", "False");
			map.put("data", null);	
		}
		
		return map;
	}
	
	@RequestMapping("/finishOrderByDriverWx")
	public @ResponseBody Map<String, Object> finishOrderByDriverWx(
			@RequestParam String user_id,  @RequestParam String order_id){
		Map<String,Object> result = new HashMap<String, Object>();

		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("orderId",order_id);
			Order order = orderService.getOrderByIdWx(paramMap);
			if(order == null || !order.getReceiveUser().equals(user_id))
			{
				result.put("State", "Fail");
				result.put("info", "结束订单失败");	
				return result;
			}

			paramMap.put("orderStatus",Constants.orderStatusFinish);
			
			JSONArray recordes = JSON.parseArray(order.getRecords());
			JSONObject record = new JSONObject();
			record.put("status",Constants.orderStatusFinish);
			record.put("time", new Date());
			recordes.add(record);

			paramMap.put("records",recordes.toString());
			
			
			int flag = orderService.updateOrderStatus(paramMap);
			if (flag != -1 && flag != 0)
			{
				result.put("State", "Success");
				result.put("data", "结束订单成功");	
				return result;
			} 
			else 
			{
				result.put("State", "Fail");
				result.put("info", "结束订单失败");	
				return result;
			}
				
		} catch (Exception e) {
			System.out.println(e);
		}

		result.put("State", "Fail");
		result.put("info", "结束订单失败");	

		return result;
	}
	
	/**
	 * 获取订单具体信息
	 * 
	 * @param phoneId
	 *            ,status
	 * @return
	 */
	@RequestMapping("/getMineProcessingOrderWx")
	public @ResponseBody Map<String, Object> getMineProcessingOrderWx( @RequestParam String  user_id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("userId",user_id);
			Order order = orderService.getMineProcessingOrder(paramMap);
			
			//没有正在处理的订单
			if (order == null)
			{
				map.put("State", "Success");
				map.put("data", "没有正在处理的订单");	
				return map;
			}
		
			JSONObject data = new JSONObject();
			data.put("orderId", order.getOrderId());
			
			if(order.getCreateUser().equals(user_id))
			{
				data.put("userType", 1);
			}
			else if(order.getReceiveUser().equals(user_id))
			{
				data.put("userType", 2);
			}
			else {
				data.put("userType", 0);
				logger.error("getMineProcessingOrder err:user_id" + user_id);
			}
			
			map.put("State", "Success");
			map.put("data", data);	
			return map;

		} catch (Exception e) 
		{
			e.printStackTrace();
			map.put("State", "Fail");
			map.put("info", null);	
			return map;
		}	
	}
}
