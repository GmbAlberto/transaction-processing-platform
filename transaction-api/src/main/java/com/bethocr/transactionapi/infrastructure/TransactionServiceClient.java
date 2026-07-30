package com.bethocr.transactionapi.infrastructure;


import com.bethocr.transactionapi.config.TransactionServiceClientConfiguration;
import com.bethocr.transactionapi.dto.request.TransactionServiceRequest;
import com.bethocr.transactionapi.dto.request.TransactionCancellationRequest;
import com.bethocr.transactionapi.dto.request.TransactionStatusUpdateRequest;
import com.bethocr.transactionapi.dto.response.ApiResponse;
import com.bethocr.transactionapi.dto.response.PageResponse;
import com.bethocr.transactionapi.dto.response.TransactionResponse;
import com.bethocr.transactionapi.dto.response.TransactionServiceResponse;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(
        name = "transaction-service",
        url = "${transaction-service.url}",
        configuration = TransactionServiceClientConfiguration.class
)
public interface TransactionServiceClient {
    @PostMapping("/api/transactions")
    ApiResponse<TransactionServiceResponse> createTransaction(
            @RequestBody TransactionServiceRequest request
    );

    @PatchMapping("/api/transactions/status")
    ApiResponse<TransactionServiceResponse> updateStatus(
            @Valid @RequestBody TransactionStatusUpdateRequest request
    );

    @GetMapping("/api/transactions/{id}")
    ApiResponse<TransactionServiceResponse> findById(
            @PathVariable("id") Long id
    );

    @GetMapping("/api/transactions")
    ApiResponse<PageResponse<TransactionServiceResponse>> findAll(
            @RequestParam("page") int page,
            @RequestParam("size") int size,
            @RequestParam("sortBy") String sortBy,
            @RequestParam("direction") String direction
    );
}
