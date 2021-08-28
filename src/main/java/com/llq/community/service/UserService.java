package com.llq.community.service;

import com.llq.community.dao.UserMapper;
import com.llq.community.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author llq
 * @create 2021-08-27  21:13
 */
@Service
public class UserService {
    @Autowired
    private UserMapper userMapper;

    public User findUserById(int id){
      return  userMapper.selectById(id);
    }
}
