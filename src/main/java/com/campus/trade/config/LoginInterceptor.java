package com.campus.trade.config;


import com.campus.trade.common.JwtUtil;
import com.campus.trade.common.Result;
import com.campus.trade.common.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;


@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Resource
    private JwtUtil jwtUtil;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //放行浏览器的跨域预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String auth=request.getHeader("Authorization");
        if (auth == null ||!auth.startsWith("Bearer ")) {
            response.setStatus(401);
            return false;
        }
        String token=auth.substring(7);//去掉baerer前缀
        try {
            Claims claims = jwtUtil.parse(token);
            UserContext.setUserId(Long.valueOf(claims.getSubject()));
            return true;

        }catch(Exception e){
            response.setStatus(401);
            return false ;
        }




    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex){
        UserContext.clear();
    }



}