package com.bethocr.transactionservice.controller;

import com.bethocr.transactionservice.dto.request.TransactionRequest;
import com.bethocr.transactionservice.dto.request.TransactionStatusUpdateRequest;
import com.bethocr.transactionservice.dto.response.ApiResponse;
import com.bethocr.transactionservice.dto.response.TransactionResponse;
import com.bethocr.transactionservice.service.TransactionService;
import jakarta.validation.Valid;
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
public class TransactionController extends BaseController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TransactionResponse>> create(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.create(request);

        return created("Transacción registrada correctamente", response);
    }

    @PatchMapping("/status")
    public ResponseEntity<ApiResponse<TransactionResponse>> updateStatus(@Valid @RequestBody TransactionStatusUpdateRequest request) {
        TransactionResponse response = transactionService.updateStatus(request);

        return ok("Estatus de la transacción actualizado correctamente", response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionResponse>> findById(@PathVariable Long id) {
        return ok(transactionService.findById(id));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> findAll() {
        return ok(transactionService.findAll());
    }
}