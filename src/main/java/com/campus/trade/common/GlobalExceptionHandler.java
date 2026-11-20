package com.campus.trade.common;


import lombok.extern.slf4j.Slf4j;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 处理业务异常 用户名存在 密码错误
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e){
        return Result.fail(e.getCode(), e.getMessage());
    }
    //参数校验失败 @Valid触发
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e){
       String msg=e.getBindingResult().getFieldError()!=null?e.getBindingResult().getFieldError().getDefaultMessage():"参数不合法";
       return Result.fail(400, msg); // Changed from Result.fail(400, msg) to Result.fail(400, msg);
    }
    //兜底 未处理的异常
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e){
        log.error("系统异常", e);
        return Result.fail(500, "系统繁忙，请稍后再试");
    }
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Result<Void> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        log.error("不支持当前请求方法", e);
        return Result.fail(405, "不支持当前请求方法");
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNoResourceFound() {
        log.error("接口不存在");
        return Result.fail(404, "接口不存在");
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.error("请求参数格式错误", e);
        return Result.fail(400, "请求参数格式错误");
    }
}
