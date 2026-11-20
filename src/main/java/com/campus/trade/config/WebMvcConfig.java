package com.campus.trade.config;

import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;



@Configuration
public class WebMvcConfig implements WebMvcConfigurer {


    @Resource
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {//注意重写的方法
       registry .addInterceptor(loginInterceptor)
               .addPathPatterns("/api/**")
               .excludePathPatterns(
                       "/api/user/login",
                       "/api/user/register",
                       "/error",
                       "/api/item/page",
                       "/api/item/*",
                       "/uploads/**",
                       "/api/message/item/*",
                       "/api/pay/return",
                       "/api/pay/ping",
                       "/api/mq/**",
                       "/api/pay/notify"
               );


    }

    @org.springframework.beans.factory.annotation.Value("${file.upload-dir}")
    private  String uploadDir;
    @Override
    public void addResourceHandlers(org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry registry){//重写
        registry.addResourceHandler( "/uploads/**")
                .addResourceLocations("file:"+uploadDir+"/");
    }



}

