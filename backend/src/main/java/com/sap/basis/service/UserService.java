package com.sap.basis.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sap.basis.entity.User;
import com.sap.basis.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class UserService extends ServiceImpl<UserMapper, User> {
}
