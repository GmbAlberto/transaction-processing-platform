package com.bethocr.transactionapi.config;

import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class TransactionServiceClientConfiguration {

    @Bean
    public ErrorDecoder transactionServiceErrorDecoder(ObjectMapper objectMapper) {
        return new TransactionServiceErrorDecoder(objectMapper);
    }
}