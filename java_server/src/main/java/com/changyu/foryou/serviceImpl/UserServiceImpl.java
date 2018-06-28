package com.changyu.foryou.serviceImpl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.changyu.foryou.mapper.UsersMapper;
import com.changyu.foryou.model.Users;
import com.changyu.foryou.service.UserService;
import com.changyu.foryou.tools.ToolUtil;


@Service("userService")
public class UserServiceImpl implements UserService {
	private UsersMapper usersMapper;         //操作用户信息


	@Autowired
	public void setUsersMapper(UsersMapper usersMapper) {
		this.usersMapper = usersMapper;
	}

	
	public Users selectByUserId(String userId) {
		return usersMapper.selectByPrimaryKey(userId);
	}


	public void addUsers(Users users) {
		usersMapper.insertSelective(users);
	}


	public int updateUserInfo(Users users) {
		return usersMapper.updateByPrimaryKeySelective(users);
	}
	
	public int updateUserBallance(Map<String, Object> paramMap) {
		return usersMapper.updateUserBallance(paramMap);
	}
	
	public int updateUserLocation(Map<String, Object> paramMap) {
		return usersMapper.updateUserLocation(paramMap);
	}
	
	public int updateUserSanInfo(Users users) {
		return usersMapper.updateUserSanReg(users);
	}
	
	@Override
	public Users checkLogin(String user_id) {
		return usersMapper.checkLogin(user_id);
	}

	
	public List<Users> getNearByUsers(String cityName,String districtName,String longitude,String latitude){
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		paramMap.put("lastCity",cityName);
		paramMap.put("lastDistrict",districtName);
	
		List<Users> list = usersMapper.getDistrictUsers(paramMap);
		
		System.out.println("getDistrictUsers:size=" + list.size());
		
		for(Users user: list)
		{
			System.out.println("before judge distance,userID:" + user.getUserId());
			if(!ToolUtil.isNearBy(longitude, latitude, user.getLastLongitude(), user.getLastLatitude()))
			{
				list.remove(user);
			}		
		}
		
		return list;
 	}

}
