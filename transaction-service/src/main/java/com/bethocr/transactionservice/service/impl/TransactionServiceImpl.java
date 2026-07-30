package com.bethocr.transactionservice.service.impl;

import com.bethocr.transactionservice.dto.request.TransactionRequest;
import com.bethocr.transactionservice.dto.request.TransactionStatusUpdateRequest;
import com.bethocr.transactionservice.dto.response.PageResponse;
import com.bethocr.transactionservice.dto.response.TransactionResponse;
import com.bethocr.transactionservice.entity.Transaction;
import com.bethocr.transactionservice.entity.TransactionStatus;
import com.bethocr.transactionservice.exception.InvalidPaginationException;
import com.bethocr.transactionservice.exception.TransactionNotFoundException;
import com.bethocr.transactionservice.mapper.TransactionMapper;
import com.bethocr.transactionservice.repository.TransactionRepository;
import com.bethocr.transactionservice.service.ReferenceService;
import com.bethocr.transactionservice.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {
    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final ReferenceService referenceService;

    private static final int MAX_PAGE_SIZE = 100;

    private static final Map<String, String> ALLOWED_SORT_FIELDS = Map.of(
            "id", "id",
            "operation", "operation",
            "amount", "amount",
            "customer", "customer",
            "reference", "reference",
            "status", "status",
            "createdAt", "createdAt"
    );

    private static final Set<String> ALLOWED_DIRECTIONS =
            Set.of("asc", "desc");

    @Override
    @Transactional
    public TransactionResponse create(TransactionRequest request) {
        Transaction transaction = transactionMapper.toEntity(request);

        transaction.setReference(referenceService.generate());
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionResponse> findAll(int page, int size, String sortBy, String direction) {
        validatePagination(page, size, sortBy, direction);

        String entityField = ALLOWED_SORT_FIELDS.get(sortBy);

        Sort.Direction sortDirection = Sort.Direction.fromString(direction);

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, entityField));

        Page<Transaction> transactionPage = transactionRepository.findAll(pageable);

        Page<TransactionResponse> responsePage = transactionPage.map(transactionMapper::toResponse);

        return new PageResponse<>(
                responsePage.getContent(),
                responsePage.getNumber(),
                responsePage.getSize(),
                responsePage.getTotalElements(),
                responsePage.getTotalPages(),
                responsePage.isFirst(),
                responsePage.isLast(),
                responsePage.isEmpty()
        );
    }

    private void validatePagination(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new InvalidPaginationException("El número de página no puede ser negativo");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidPaginationException("El número de registros por página debe estar entre 1 y " + MAX_PAGE_SIZE);
        }

        if (!ALLOWED_SORT_FIELDS.containsKey(sortBy)) {
            throw new InvalidPaginationException("El campo de ordenamiento no es válido. Campos permitidos: " + ALLOWED_SORT_FIELDS.keySet());
        }

        if (!ALLOWED_DIRECTIONS.contains(direction.toLowerCase())) {
            throw new InvalidPaginationException("La dirección debe ser asc o desc");
        }
    }
}
