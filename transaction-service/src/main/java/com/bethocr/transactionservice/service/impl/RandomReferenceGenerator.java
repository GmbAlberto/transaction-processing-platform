package com.bethocr.transactionservice.service.impl;

import com.bethocr.transactionservice.exception.ReferenceGenerationException;
import com.bethocr.transactionservice.repository.TransactionRepository;
import com.bethocr.transactionservice.service.ReferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class RandomReferenceGenerator implements ReferenceService {
    private final SecureRandom secureRandom = new SecureRandom();
    private final TransactionRepository transactionRepository;

    private static final int MIN_REFERENCE = 100000;
    private static final int MAX_REFERENCE = 999999;
    private static final int MAX_ATTEMPTS = 10;

    public String generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String reference = generateCandidate();

            if (!transactionRepository.existsByReference(reference)) {
                return reference;
            }
        }

        throw new ReferenceGenerationException();
    }

    private String generateCandidate() {
        int value = secureRandom.nextInt(MAX_REFERENCE - MIN_REFERENCE + 1) + MIN_REFERENCE;

        return String.valueOf(value);
    }
}
