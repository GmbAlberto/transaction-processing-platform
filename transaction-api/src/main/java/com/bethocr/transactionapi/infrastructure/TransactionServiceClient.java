package com.bethocr.transactionapi.infrastructure;


import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(
        name = "transaction-service",
        url = "transaction-service.ur"
)
public interface TransactionServiceClient {

}
