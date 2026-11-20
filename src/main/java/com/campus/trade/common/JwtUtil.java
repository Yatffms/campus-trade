package com.campus.trade.common;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @org.springframework.beans.factory.annotation.Value("${jwt.secret}")
    private  String secret;
    @org.springframework.beans.factory.annotation.Value("${jwt.expire-hours}")
    private  long expireHours;

    private SecretKey key(){
        return  Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

    }
    //生成token
    public String generate(long userId, String username) {
        Date now=new Date();
        Date expire=new Date(now.getTime()+expireHours*3600_000L);
        return Jwts.builder()
                .subject(String.valueOf(userId))//用户ID
                .claim("username", username)//用户名
                .issuedAt(now)//签发时间
                .expiration(expire)//过期时间
                .signWith(key())//签名 冲压一下
                .compact();//转成字符串

    }

    //解析token  失败会抛出异常
    public Claims parse(String token){
        return Jwts.parser()
                .verifyWith(key())//用用一个密钥印签 不对会抛出signException
                .build()//组装完毕
                .parseSignedClaims(token)//核对token过期会抛出expiredException
                .getPayload(); // // 掏出来的东西叫 Claims，里面装着刚才塞进去的 userId 和 username

    }


}
