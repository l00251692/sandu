package com.changyu.foryou.serviceImpl;

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
}
