package com.bethocr.transactionapi.service;

import com.bethocr.transactionapi.dto.request.TransactionCancellationRequest;
import com.bethocr.transactionapi.dto.request.TransactionRequest;
import com.bethocr.transactionapi.dto.response.PageResponse;
import com.bethocr.transactionapi.dto.response.TransactionResponse;

import java.util.List;

public interface TransactionApplicationService {
    TransactionResponse processTransaction(TransactionRequest request);

    TransactionResponse cancelTransaction(TransactionCancellationRequest id);

    TransactionResponse findById(Long id);

    PageResponse<TransactionResponse> findAll(int page, int size, String sortBy, String direction);
}
