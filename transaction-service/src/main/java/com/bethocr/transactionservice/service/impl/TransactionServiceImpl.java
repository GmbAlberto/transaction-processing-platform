package com.bethocr.transactionservice.service.impl;

import com.bethocr.transactionservice.dto.request.TransactionRequest;
import com.bethocr.transactionservice.dto.request.TransactionStatusUpdateRequest;
import com.bethocr.transactionservice.dto.response.TransactionResponse;
import com.bethocr.transactionservice.entity.Transaction;
import com.bethocr.transactionservice.entity.TransactionStatus;
import com.bethocr.transactionservice.exception.TransactionNotFoundException;
import com.bethocr.transactionservice.mapper.TransactionMapper;
import com.bethocr.transactionservice.repository.TransactionRepository;
import com.bethocr.transactionservice.service.ReferenceGenerator;
import com.bethocr.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ReferenceGenerator referenceGenerator;

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Transaction transaction = transactionMapper.toEntity(request);

        transaction.setReference(referenceGenerator.generate());
        transaction.setStatus(TransactionStatus.APPROVED);

        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toResponse(savedTransaction);
    }

    @Override
    @Transactional(readOnly = true)
    public TransactionResponse findById(Long id) {
        return transactionRepository.findById(id)
                .map(transactionMapper::toResponse)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> findAll() {
        return transactionRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(transactionMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public TransactionResponse updateStatus(TransactionStatusUpdateRequest request) {
        int updatedRows = transactionRepository.updateStatus(
                request.id(),
                request.reference(),
                request.status()
        );

        if (updatedRows == 0) {
            throw new TransactionNotFoundException(request.id(), request.reference());
        }

        return transactionRepository.findById(request.id())
                .map(transactionMapper::toResponse)
                .orElseThrow(() ->
                        new TransactionNotFoundException(request.id())
                );
    }
}
