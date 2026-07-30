package com.bethocr.transactionapi.dto.request;

import com.bethocr.transactionapi.entity.TransactionStatus;

public record TransactionStatusUpdateRequest(
        Long id,
        String reference,
        TransactionStatus status
) {
}
