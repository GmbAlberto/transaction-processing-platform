package com.bethocr.transactionservice.service;

import com.bethocr.transactionservice.dto.request.TransactionRequest;
import com.bethocr.transactionservice.dto.request.TransactionStatusUpdateRequest;
import com.bethocr.transactionservice.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionService {
    TransactionResponse create(TransactionRequest request);

    TransactionResponse findById(Long id);

    List<TransactionResponse> findAll();

    TransactionResponse updateStatus(TransactionStatusUpdateRequest request);
}
