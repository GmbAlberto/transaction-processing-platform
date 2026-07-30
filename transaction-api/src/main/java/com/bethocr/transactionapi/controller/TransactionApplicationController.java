package com.bethocr.transactionapi.controller;

import com.bethocr.transactionapi.dto.request.TransactionCancellationRequest;
import com.bethocr.transactionapi.dto.request.TransactionRequest;
import com.bethocr.transactionapi.dto.response.ApiResponse;
import com.bethocr.transactionapi.dto.response.TransactionResponse;
import com.bethocr.transactionapi.service.TransactionApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionApplicationController extends BaseController {
    private final TransactionApplicationService transactionApplicationService;

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> createTransaction(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionApplicationService.processTransaction(request);

        return created("Transacción registrada correctamente", response);
    }

    @PatchMapping("/cancel")
    public ResponseEntity<ApiResponse<TransactionResponse>> cancelTransaction(@Valid @RequestBody TransactionCancellationRequest request) {
        TransactionResponse response = transactionApplicationService.cancelTransaction(request);

        return ok("Transacción cancelada correctamente", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> findById(
            @PathVariable Long id
    ) {
        return ok(transactionApplicationService.findById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> findAll() {
        return ok(transactionApplicationService.findAll());
    }
}