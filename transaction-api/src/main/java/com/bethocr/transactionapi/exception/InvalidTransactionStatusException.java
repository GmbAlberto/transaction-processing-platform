package com.bethocr.transactionapi.exception;

public class InvalidTransactionStatusException extends RuntimeException {
    public InvalidTransactionStatusException(String message) {
        super(message);
    }
}
