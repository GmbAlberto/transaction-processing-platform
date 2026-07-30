package com.bethocr.transactionservice.dto.response;

public enum ErrorCode {
    VALIDATION_ERROR,
    MALFORMED_JSON,
    TRANSACTION_NOT_FOUND,
    REFERENCE_GENERATION_ERROR,
    DATA_INTEGRITY_VIOLATION,
    INTERNAL_SERVER_ERROR
}