package com.changyu.foryou.mapper;

import java.util.List;
import java.util.Map;

import com.changyu.foryou.model.Order;


public interface OrderMapper {
	int insertSelective(Map<String, Object> paramMap);
	
	Order selectByPrimaryKey(Map<String,Object> paramMap);
	
	Order getMineProcessingOrder(Map<String,Object> paramMap);
	
	Order getMineProcessingOrderDriver(Map<String,Object> paramMap);
	
	public int updateOrderStatus(Map<String, Object> paramMap);
	
	public int updatePassengerStar(Map<String, Object> paramMap);
	
	public int updateDriverStar(Map<String, Object> paramMap);
	
	public int updateOrderReceiver(Map<String, Object> paramMap);
	
	public List<Order> getNearByOrders(Map<String, Object> paramMap);
	
	public List<Order> getDriverOrders(Map<String, Object> paramMap);
	
	public List<Order> getPassengerOrders(Map<String, Object> paramMap);
}
