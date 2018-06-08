package com.changyu.foryou.serviceImpl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.changyu.foryou.mapper.UsersMapper;
import com.changyu.foryou.model.Users;
import com.changyu.foryou.service.UserService;


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
	
	@Override
	public Users checkLogin(String user_id) {
		return usersMapper.checkLogin(user_id);
	}


}
