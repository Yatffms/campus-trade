package com.campus.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.campus.trade.common.BusinessException;
import com.campus.trade.common.JwtUtil;
import com.campus.trade.dto.LoginDto;
import com.campus.trade.dto.RegisterDto;
import com.campus.trade.entity.User;
import com.campus.trade.mapper.UserMapper;
import com.campus.trade.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

    @Service
    public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper userMapper;

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Resource
    private PasswordEncoder passwordEncoder;//加密器 不需要new
    @Override
    public  void register  (RegisterDto dto){
        //查重
        Long count = userMapper.selectCount(new QueryWrapper<User>().eq("username", dto.getUsername()));
        if (count > 0) {
            throw new BusinessException(400, "用户名已存在");
        }
        //组装实体
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 密码加密
        user.setNickname(dto.getNickname());
        user.setSchool(dto.getSchool());
        //插入
        userMapper.insert(user);

    }

    @Resource
    private JwtUtil jwtUtil;
    @Override
    public String login(LoginDto dto) {
        //根据用户名查询用户
        User user = userMapper.selectOne(
                new QueryWrapper<User>().eq("username", dto.getUsername()));
        //用户名或密码错误
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException(400,"用户名或密码错误");
        }

       //生成token
        return jwtUtil.generate(user.getId(), user.getUsername());

    }

 }
