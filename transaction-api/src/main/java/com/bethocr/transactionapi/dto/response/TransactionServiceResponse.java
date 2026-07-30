package com.bethocr.transactionapi.dto.response;

public record TransactionServiceResponse(
        Long id,
        String status,
        String reference,
        String operation
) {
}