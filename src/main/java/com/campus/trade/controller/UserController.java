package com.campus.trade.controller;

import com.campus.trade.common.Result;
import com.campus.trade.dto.LoginDto;
import com.campus.trade.dto.RegisterDto;
import com.campus.trade.entity.User;
import com.campus.trade.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable("id") Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.fail("用户不存在");
        }
        return Result.ok(user);
    }

    @PostMapping("/register")
    public Result<Void> register(@RequestBody @Valid RegisterDto dto) {
        userService.register(dto);
        return Result.ok();

    }
    @PostMapping("/login")
    public Result<String> login(@RequestBody @Valid LoginDto dto) {
        return Result.ok(userService.login(dto));
    }

}
