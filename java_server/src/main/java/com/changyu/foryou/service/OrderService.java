package com.changyu.foryou.service;

import java.util.List;
import java.util.Map;

import com.changyu.foryou.model.Order;

public interface OrderService {
	
	public int addOrder(Map<String, Object> paramMap);
	
	public Order getOrderByIdWx(Map<String, Object> paramMap);
	
	public Order getMineProcessingOrder(Map<String, Object> paramMap);
	
	public Order getMineProcessingOrderDriver(Map<String, Object> paramMap);
	
	public int updateOrderStatus(Map<String, Object> paramMap);
	
	public int updateOrderStarByPassenger(Map<String, Object> paramMap);
	
	public int updateOrderStarByDriver(Map<String, Object> paramMap);
	
	public int updateOrderReceiver(Map<String, Object> paramMap);
	
	public List<Order> getNearByOrders(Map<String, Object> paramMap);
	
	public List<Order> getDriverOrders(Map<String, Object> paramMap);
	
	public List<Order> getPassengerOrders(Map<String, Object> paramMap);

}
