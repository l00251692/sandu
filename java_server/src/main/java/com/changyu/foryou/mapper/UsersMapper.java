package com.changyu.foryou.mapper;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.changyu.foryou.model.Users;

public interface UsersMapper {


    int insertSelective(Users record);

    Users selectByPrimaryKey(String userId);

    int updateByPrimaryKeySelective(Users record);

	Users checkLogin(String user_id);

}