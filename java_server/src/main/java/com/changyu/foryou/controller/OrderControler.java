package com.changyu.foryou.controller;

import java.io.UnsupportedEncodingException;
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
import org.springframework.web.socket.TextMessage;

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
import com.changyu.foryou.tools.HttpRequest;
import com.changyu.foryou.tools.PayUtil;
import com.changyu.foryou.tools.ThreadPoolUtil;
import com.changyu.foryou.tools.TimeUtil;
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
				
				//发送消息通知用户新订单
				JSONObject to = new JSONObject();
				to.put("type", "orderMsg");
				to.put("orderId", orderId);
				to.put("msg", "新订单");
				to.put("passengerId", user_id);
		

				TextMessage toMsg = new TextMessage(to.toString());
				
				//找到同一区的用户，发送给附近的用户
				
				webSocketHandler.sendMessagesToUsers(toMsg);
				
		        ThreadPoolUtil.execute(new Runnable(){  
		            public void run(){ 
		            	
		                //重新添加等待服务行程的延迟队列
		                DSHOrder dshOrder = new DSHOrder(orderId,Constants.orderStatusCreate, Constants.WAIT_CREATE);  
		                delayService.add(dshOrder);  
		        
		                //2插入到redis  
		                redisServie.add(Constants.REDISPREFIX+orderId, dshOrder, Constants.REDISSAVETIME);  
		            }  
		        });
				
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
		map.put("info", "提交订单失败");	

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
				result.put("info", "读取订单信息失败");	

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
				//发送消息通知用户订单已取消
				JSONObject to = new JSONObject();
				to.put("type", "orderMsg");
				to.put("orderId", order_id);
				to.put("msg", "取消订单");
				to.put("passengerId", user_id);

				TextMessage toMsg = new TextMessage(to.toString());
				webSocketHandler.sendMessagesToUsers(toMsg);
				
				
				//订单已经取消，从redis和延迟队列删除
		        ThreadPoolUtil.execute(new Runnable(){  
		            public void run(){  
		                //从delay队列删除  
		                delayService.remove(order_id);  
		                //从redis删除  
		                redisServie.delete(Constants.REDISPREFIX+order_id); 
		            }  
		        });
				
				
				
				result.put("State", "Success");
				result.put("data", null);	
				return result;
			} 
			else 
			{
				result.put("State", "Fail");
				result.put("info", "更新订单信息失败");	
				return result;
			}
				
		} catch (Exception e) {
			System.out.println(e);
		}
		
		result.put("State", "Fail");
		result.put("info", "取消订单失败");	

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
			
			paramMap.put("limit", 5);
			paramMap.put("offset", page * 5);//默认一次5条
			paramMap.put("cityName", city_name);
			paramMap.put("districtName", district_name);
	
			List<Order> ordersList =orderService.getNearByOrders(paramMap);
			
			System.out.println("getDistanceOrders size:" + ordersList.size());
			
			
			JSONArray orderArray = new JSONArray();
			
			for(Order order:ordersList)
			{
				JSONObject obj = new JSONObject();
				if(ToolUtil.isNearByOrder(my_longitude, my_latitude, order.getFromAddrLongitude(), order.getFromAddrLatitude()))
				{
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
	 * 获取下达的所有订单
	 * 
	 * @param phoneId
	 *            ,status
	 * @return
	 */
	@RequestMapping("/getDriverOrdersWx")
	public @ResponseBody Map<String, Object> getDriverOrdersWx(@RequestParam String user_id, @RequestParam Integer page) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			
			paramMap.put("userId", user_id);//默认一次5条
			paramMap.put("limit", 5);
			paramMap.put("offset", page * 5);//默认一次5条
	
			List<Order> ordersList =orderService.getDriverOrders(paramMap);
				
			JSONArray orderArray = new JSONArray();
			
			for(Order order:ordersList)
			{
				JSONObject obj = new JSONObject();

				Users user = userService.selectByUserId(order.getCreateUser());
				obj.put("create_time", order.getCreateTime());
				obj.put("depart_time", order.getDepartTime());
				obj.put("order_price", (order.getOrderPrice() - 0.1f));//减去0.1的手续费
				obj.put("order_status", order.getOrderStatus());
				obj.put("order_id", order.getOrderId());
				obj.put("from_addr", order.getFromAddr());
				obj.put("to_addr", order.getToAddr());
				
				orderArray.add(obj);
			
			}
			
			JSONObject data = new JSONObject();
			data.put("list2",orderArray);
			data.put("count",ordersList.size());
			data.put("page",page);

			map.put("State", "Success");
			map.put("data", data);	
			return map;

		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		map.put("State", "Fail");
		map.put("data", "读取订单记录失败");	
		return map;
	}
	
	/**
	 * 获取下达的所有订单
	 * 
	 * @param phoneId
	 *            ,status
	 * @return
	 */
	@RequestMapping("/getPassengerOrdersWx")
	public @ResponseBody Map<String, Object> getPassengerOrdersWx(@RequestParam String user_id, @RequestParam Integer page) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			
			paramMap.put("userId", user_id);//默认一次5条
			paramMap.put("limit", 10);
			paramMap.put("offset", page * 10);//默认一次5条
	
			List<Order> ordersList =orderService.getPassengerOrders(paramMap);
				
			JSONArray orderArray = new JSONArray();
			
			for(Order order:ordersList)
			{
				JSONObject obj = new JSONObject();

				Users user = userService.selectByUserId(order.getCreateUser());
				obj.put("create_time", order.getCreateTime());
				obj.put("depart_time", order.getDepartTime());
				obj.put("order_price", order.getOrderPrice());
				obj.put("order_status", order.getOrderStatus());
				obj.put("order_id", order.getOrderId());
				obj.put("from_addr", order.getFromAddr());
				obj.put("to_addr", order.getToAddr());
				
				orderArray.add(obj);
				
			}
			
			JSONObject data = new JSONObject();
			data.put("list2",orderArray);
			data.put("count",ordersList.size());
			data.put("page",page);

			map.put("State", "Success");
			map.put("data", data);	
			return map;

		} catch (Exception e) 
		{
			e.printStackTrace();
		}
		map.put("State", "Fail");
		map.put("data", "读取订单记录失败");	
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
				obj_driver.put("driverId", order.getReceiveUser());
				obj_driver.put("name", driver.getNickname());
				obj_driver.put("head", driver.getImgUrl());
				obj_driver.put("phone", driver.getPhone());
				obj_driver.put("stars", 5);
				obj_driver.put("feature", driver.getSanFeature());
				obj_driver.put("color", driver.getSanColor());
				obj_driver.put("style", driver.getSanStyle());
			}
			
			Users passenger = userService.selectByUserId(order.getCreateUser());
			obj_passenger.put("passengerId", order.getCreateUser());
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
			obj_order.put("rcvTime", order.getRcvTime());
			obj_order.put("departTime", order.getDepartTime());
			obj_order.put("orderStatus", order.getOrderStatus());
			obj_order.put("orderPrice", order.getOrderPrice());
			
			
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
			map.put("State", "Fail");
			map.put("info", "查询订单详细信息失败");	
			return map;
		}
		
		//TODO:合理性处理先注释掉
		/*if(user_id.equals(order.getCreateUser())){
			map.put("State", "Fail");
			map.put("info", "您不能接单自己创建的订单");
			return map;		
		}*/
		
		if(order.getReceiveUser() != null && order.getReceiveUser().length() > 0)
		{
			map.put("State", "Fail");
			map.put("info", "该订单已被接，请选择其他订单");
			return map;
		}
		
		Date date = new Date();
		JSONArray records = JSON.parseArray(order.getRecords());
		JSONObject record = new JSONObject();
		record.put("status",Constants.orderStatusRecv);
		record.put("time", date);
		records.add(record);
		
		paramMap.put("records",records.toJSONString());
		paramMap.put("orderStatus",Constants.orderStatusRecv);
		paramMap.put("receiveUser",user_id);
		paramMap.put("rcvTime",date);
		
		int flage = orderService.updateOrderReceiver(paramMap);
		if(flage != -1 && flage !=0)
		{
			//发送消息通知用户订单已被接
			JSONObject to = new JSONObject();
			to.put("type", "orderMsg");
			to.put("orderId", order_id);
			to.put("msg", "订单被接");
			to.put("driverId", user_id);
			to.put("toId", order.getCreateUser());
			
			to.put("time", TimeUtil.DateformatTime(date));
			TextMessage toMsg = new TextMessage(to.toString());
	
			webSocketHandler.sendMessagesToUsers(toMsg);
			
			System.out.println("删除之前的delay队列");
			//订单已经被接手，下一个状态就是结束行程，如果用户未确认则时间到后自动结束行程
	        ThreadPoolUtil.execute(new Runnable(){  
	            public void run(){  
	                //从delay队列删除  
	                delayService.remove(order_id);  
	                //从redis删除  
	                redisServie.delete(Constants.REDISPREFIX+order_id); 
	            }  
	        });
	        
	        
			map.put("State", "Success");
			map.put("data", null);	
		}
		else
		{
			map.put("State", "Fail");
			map.put("info", "接单失败");	
		}
		
		return map;
	}
	
	@RequestMapping("/finishOrderByDriverWx")
	public @ResponseBody Map<String, Object> finishOrderByDriverWx(
			@RequestParam String user_id,  @RequestParam String order_id, @RequestParam int star){
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

				paramMap.put("starByDriver",star);
				orderService.updateOrderStarByDriver(paramMap);
				
				//发送消息通知用户行程完成
				JSONObject to = new JSONObject();
				to.put("type", "orderMsg");
				to.put("orderId", order_id);
				to.put("msg", "行程完成");

				TextMessage toMsg = new TextMessage(to.toString());
				webSocketHandler.sendMessageToUser(order.getCreateUser(), toMsg);
				
				
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
	
	@RequestMapping("/finishOrderByPassengerWx")
	public @ResponseBody Map<String, Object> finishOrderByPassengerWx(
			@RequestParam String user_id,  @RequestParam int star, @RequestParam String order_id ,@RequestParam float pay_money, @RequestParam String prepay_id){
		Map<String,Object> result = new HashMap<String, Object>();

		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("orderId",order_id);
			Order order = orderService.getOrderByIdWx(paramMap);
			if(order == null || !order.getCreateUser().equals(user_id))
			{
				result.put("State", "Fail");
				result.put("info", "结束订单失败[订单信息错误]");	
				return result;
			}

			paramMap.put("orderStatus",Constants.orderStatusDone);
			
			JSONArray recordes = JSON.parseArray(order.getRecords());
			JSONObject record = new JSONObject();
			record.put("status",Constants.orderStatusDone);
			record.put("time", new Date());
			recordes.add(record);

			paramMap.put("records",recordes.toString());
			
			int flag = orderService.updateOrderStatus(paramMap);
			if (flag != -1 && flag != 0)
			{
				
				//更新用户对司机的评价
				paramMap.put("startByPassenger", star);
				orderService.updateOrderStarByPassenger(paramMap);
				
				//更新司机账户余额信息
				Users user = userService.selectByUserId(order.getCreateUser());
				
				if(user != null )
				{
					Map<String, Object> paramMap2 = new HashMap<String, Object>();
					paramMap2.put("userId",user_id);
					
					float balance = user.getBallance() + (pay_money - 0.1f);//每笔订单收取0.1元手续费
					paramMap2.put("ballance",balance);
					
					int flag2 = userService.updateUserBallance(paramMap2);
					
					if (flag != -1 && flag != 0)
					{
						result.put("State", "Success");
						result.put("data", "结束订单成功");	
					}
					else
					{
						result.put("State", "Fail");
						result.put("info", "结束订单失败[结算失败]");	
					}
				}
				else
				{
					result.put("State", "Fail");
					result.put("info", "结束订单失败[司机信息错误]");	
				}
				System.out.println("向用户发送消息开始");
				
				//向消费用户发送模板消息                                    1
				String url = "https://api.weixin.qq.com/cgi-bin/message/wxopen/template/send";

				String access_token = (String) PayUtil.getAccessToken().get("access_token");
				//取access_token
		    
				url = url + "?access_token=" + access_token;
				String touser = user_id;
				//选取的模板消息id，进入小程序公众后台可得
				String template_id = Constants.TemplateIdPaySuccess;
				//支付时下单而得的prepay_id
				
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式  
				String curDateStr = df.format(new Date());
				
				StringBuffer buffer = new StringBuffer();
				//按照官方api的要求提供params
				buffer.append("{");
				buffer.append(String.format("\"touser\":\"%s\"", touser)).append(",");
				buffer.append(String.format("\"template_id\":\"%s\"", template_id)).append(",");
				buffer.append(String.format("\"page\":\"%s\"", "pages/index/index")).append(",");
				buffer.append(String.format("\"form_id\":\"%s\"", prepay_id)).append(",");
				buffer.append("\"data\":{");
				/*buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"},","first", "订单支付成功通知", "#173177"));
				buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"},","keyword1", curDateStr, "#173177"));
				buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"},","keyword2", "打伞服务", "#173177"));
				buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"},","keyword3", order_id, "#173177"));
				buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"},","keyword4", order.getOrderPrice(), "#173177"));
				buffer.append(String.format("\"%s\": {\"value\":\"%s\",\"color\":\"%s\"}","remark", "欢迎再次使用", "#173177"));*/
				
				buffer.append(String.format("\"%s\": {\"value\":\"%s\"},","keyword1", curDateStr));
				buffer.append(String.format("\"%s\": {\"value\":\"%s\"},","keyword2", "打伞服务"));
				buffer.append(String.format("\"%s\": {\"value\":\"%s\"},","keyword3", order_id));
				buffer.append(String.format("\"%s\": {\"value\":\"%s\"}","keyword4", order.getOrderPrice()));
				
				buffer.append("}");
				buffer.append("}");
				String params = "";
				try {
					params = new String(buffer.toString().getBytes("UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				};
				System.out.println(params);
				String sr = HttpRequest.sendPost(url,params);
				
				System.out.println("向用户发送消息" + sr);
						
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
			data.put("orderStatus", order.getOrderStatus());
			
			
			map.put("State", "Success");
			map.put("data", data);	
			return map;

		} catch (Exception e) 
		{
			e.printStackTrace();
			map.put("State", "Fail");
			map.put("info", "获取进行中的订单失败");	
			return map;
		}	
	}
	
	/**
	 * 获取订单具体信息
	 * 
	 * @param phoneId
	 *            ,status
	 * @return
	 */
	@RequestMapping("/getMineProcessingOrderDriverWx")
	public @ResponseBody Map<String, Object> getMineProcessingOrderDriverWx( @RequestParam String  user_id) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("userId",user_id);
			Order order = orderService.getMineProcessingOrderDriver(paramMap);
			
			//没有正在处理的订单
			if (order == null)
			{
				map.put("State", "Success");
				map.put("data", "没有正在处理的订单");	
				return map;
			}
		
			JSONObject data = new JSONObject();
			data.put("orderId", order.getOrderId());
			data.put("orderStatus", order.getOrderStatus());
			
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
			map.put("info", "获取进行中的订单失败");	
			return map;
		}	
	}
	
	/**
     * 申请退款
     * @return
     */
	@RequestMapping("/refundWx")
    public @ResponseBody Map<String, Object> refundWx(@RequestParam String order_id,String user_id,Float fee) {
          Map<String,Object> result = new HashMap<String,Object>();
          
          /*String resultStr = payService.refund(order_id, String.valueOf(fee*100));
          
          System.out.println("调试模式_统一下单接口 返回XML数据：" + result);  
            
          //解析结果
          try {
              Map map =  PayUtil.doXMLParse(resultStr);
              String returnCode = map.get("return_code").toString();
              if(returnCode.equals("SUCCESS")){
                  String resultCode = map.get("result_code").toString();
                  if(resultCode.equals("SUCCESS")){
                      JSONObject node = new JSONObject();
                      node.put("totalFee", fee);
                      result.put("State", "Success");
                      result.put("data", node);	
          			return result;
                  }
                  else
                  {
                      result.put("State", "Fail");
                  }
              }
              else
              {
                  result.put("State", "Fail");
              }
          } 
          catch (Exception e) 
          {
              e.printStackTrace();
              result.put("State", "Fail");
          }*/
          result.put("State", "Success");
          result.put("data", null);	
          return result;
    }
}
