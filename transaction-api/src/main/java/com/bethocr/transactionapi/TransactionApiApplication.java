package com.bethocr.transactionapi;

import com.bethocr.transactionapi.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class TransactionApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(TransactionApiApplication.class, args);
    }

}
