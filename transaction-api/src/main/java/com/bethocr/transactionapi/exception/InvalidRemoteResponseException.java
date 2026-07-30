package com.bethocr.transactionapi.exception;

public class InvalidRemoteResponseException extends RuntimeException {
    public InvalidRemoteResponseException(String message) {
        super(message);
    }
}