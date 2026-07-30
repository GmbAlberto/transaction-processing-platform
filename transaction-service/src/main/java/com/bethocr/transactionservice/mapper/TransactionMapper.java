package com.bethocr.transactionservice.mapper;

import com.bethocr.transactionservice.dto.request.TransactionRequest;
import com.bethocr.transactionservice.dto.response.TransactionResponse;
import com.bethocr.transactionservice.entity.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public Transaction toEntity(TransactionRequest request) {
        return Transaction.builder()
                .operation(request.operation())
                .amount(request.amount())
                .customer(request.customer())
                .secret(request.secret())
                .build();
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return new TransactionResponse(
                transaction.getId(),
                transaction.getStatus().getDescription(),
                transaction.getReference(),
                transaction.getOperation()
        );
    }
}