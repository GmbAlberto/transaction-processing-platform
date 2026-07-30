package com.bethocr.transactionapi.dto.request;

import java.math.BigDecimal;

public record TransactionServiceRequest(
        String operation,
        BigDecimal amount,
        String customer,
        String secret
) {
}
