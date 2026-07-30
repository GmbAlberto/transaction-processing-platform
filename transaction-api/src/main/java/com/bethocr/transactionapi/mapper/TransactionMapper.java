package com.bethocr.transactionapi.mapper;

import com.bethocr.transactionapi.dto.response.TransactionResponse;
import com.bethocr.transactionapi.dto.response.TransactionServiceResponse;

public class TransactionMapper {
    public static TransactionResponse toAPIResponse(TransactionServiceResponse response) {
        return new TransactionResponse(
                response.id(),
                response.status(),
                response.reference(),
                response.operation()
        );
    }
}
