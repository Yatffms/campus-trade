package com.campus.trade.service;

import com.campus.trade.dto.LoginDto;
import com.campus.trade.dto.RegisterDto;
import com.campus.trade.entity.User;

public interface UserService {
    User getById(Long id);
    void register (RegisterDto dto);
    String login(LoginDto dto);
}
