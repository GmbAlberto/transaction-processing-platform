package com.bethocr.transactionapi.dto.response;

public record LoginResponse(
        String token,
        String type,
        long expiresIn
) {
}