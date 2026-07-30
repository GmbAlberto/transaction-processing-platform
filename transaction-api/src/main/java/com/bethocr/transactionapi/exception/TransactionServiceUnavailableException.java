package com.bethocr.transactionapi.exception;

public class TransactionServiceUnavailableException extends RuntimeException {
    public TransactionServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}