package com.campus.trade.config;


import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;


@Getter
@Configuration
public class AlipayConfig  {
    @Value("${alipay.app-id}")
    private String appId;

    @Value("${alipay.gateway}")
    private String gatewayUrl;

    @Value("${alipay.private-key-path}")
    private String privateKeyPath;

    @Value("${alipay.alipay-public-key-path}")
    private String alipayPublicKeyPath;

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Value("${alipay.return-url}")
    private String returnUrl;


    @Bean
    public AlipayClient alipayClient() throws IOException {
        String privateKey = readKey(privateKeyPath);
        String alipayPublicKey = readKey(alipayPublicKeyPath);
        return new DefaultAlipayClient(gatewayUrl,
                appId,
                privateKey,
                "json",
                "utf-8",
                alipayPublicKey,
                "RSA2");
    }
    private String readKey(String path) throws IOException {
        return Files.readString(Path.of(path), StandardCharsets.UTF_8).replaceAll("\\s+", "");
    }

    public String publicKey() throws IOException {
        return readKey(alipayPublicKeyPath);
    }




}
