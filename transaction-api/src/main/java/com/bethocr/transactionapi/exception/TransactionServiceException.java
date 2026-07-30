package com.bethocr.transactionapi.exception;

import com.bethocr.transactionapi.dto.response.ApiResponse;
import org.springframework.http.HttpStatusCode;

public class TransactionServiceException extends RuntimeException {
    private final HttpStatusCode status;
    private final ApiResponse<?> response;

    public TransactionServiceException(HttpStatusCode status, ApiResponse<?> response) {
        super(response.message());
        this.status = status;
        this.response = response;
    }

    public HttpStatusCode getStatus() {
        return status;
    }

    public ApiResponse<?> getResponse() {
        return response;
    }
}