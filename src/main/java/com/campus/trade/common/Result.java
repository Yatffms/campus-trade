package com.campus.trade.common;

import lombok.Data;

/**
 统一响应体：所有接口都返回这个结构
 { "code": 200, "message": "ok", "data": ... }
 */
@Data
public class Result<T> {

    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("ok");
        r.setData(data);
        return r;
    }

    public static <T> Result<T>ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }
}
