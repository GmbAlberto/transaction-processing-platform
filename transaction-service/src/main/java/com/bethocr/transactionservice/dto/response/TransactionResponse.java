package com.bethocr.transactionservice.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TransactionResponse(
        Long id,
        String status,
        String reference,
        String operation,
        String customer,
        BigDecimal amount,
        LocalDateTime createdAt
) {
}