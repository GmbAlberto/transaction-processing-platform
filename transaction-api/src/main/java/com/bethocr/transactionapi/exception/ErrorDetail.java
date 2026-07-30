package com.bethocr.transactionapi.exception;

public record ErrorDetail(
        String field,
        String message
) {
}