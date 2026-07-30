package com.bethocr.transactionservice.dto.response;

public record TransactionResponse(
        Long id,
        String status,
        String reference,
        String operation
) {
}