package com.changyu.foryou.mapper;

import java.util.Map;


public interface OrderMapper {
	int insertSelective(Map<String, Object> paramMap);
}
