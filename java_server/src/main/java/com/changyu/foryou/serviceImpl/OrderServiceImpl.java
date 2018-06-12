package com.changyu.foryou.serviceImpl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.changyu.foryou.mapper.OrderMapper;
import com.changyu.foryou.model.Order;
import com.changyu.foryou.service.OrderService;

@Service("orderService")
public class OrderServiceImpl implements OrderService {
	private OrderMapper orderMapper;
	
	public OrderMapper getOrderMapper() {
		return orderMapper;
	}

	@Autowired
	public void setOrderMapper(OrderMapper orderMapper) {
		this.orderMapper = orderMapper;
	}
	
	public int addOrder(Map<String, Object> paramMap) {
		return orderMapper.insertSelective(paramMap);
	}
	
	public Order getOrderByIdWx(Map<String, Object> paramMap) {
		return orderMapper.selectByPrimaryKey(paramMap);
	}
	
	public int updateOrderStatus(Map<String, Object> paramMap) {
		return orderMapper.updateOrderStatus(paramMap);
	}
	
	public int updateOrderReceiver(Map<String, Object> paramMap) {
		return orderMapper.updateOrderReceiver(paramMap);
	}
	
	public List<Order> getNearByOrders(Map<String, Object> paramMap){
		return orderMapper.getNearByOrders(paramMap);
	}
}
