package com.bethocr.transactionservice.exception;

public record ErrorDetail(
        String field,
        String message
) {
}
