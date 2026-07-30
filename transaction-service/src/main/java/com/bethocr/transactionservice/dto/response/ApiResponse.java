package com.bethocr.transactionservice.dto.response;

import com.bethocr.transactionservice.config.ApiConstants;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        ErrorCode code,
        T data,
        String version,
        LocalDateTime timestamp
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, null, data, ApiConstants.API_VERSION, LocalDateTime.now());
    }

    public static <T> ApiResponse<T> error(String message, ErrorCode code, T data) {
        return new ApiResponse<>(false, message, code, data, ApiConstants.API_VERSION, LocalDateTime.now());
    }
}