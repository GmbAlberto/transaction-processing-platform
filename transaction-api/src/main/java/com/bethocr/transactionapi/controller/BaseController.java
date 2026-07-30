package com.bethocr.transactionapi.controller;

import com.bethocr.transactionapi.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    protected static final String DEFAULT_SUCCESS_MESSAGE = "Operación exitosa";


    protected <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ok(DEFAULT_SUCCESS_MESSAGE, data);
    }

    protected <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    protected <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(message, data)
                );
    }
}