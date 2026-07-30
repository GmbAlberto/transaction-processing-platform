package com.bethocr.transactionservice.exception;

public class ReferenceGenerationException extends RuntimeException {
    public ReferenceGenerationException() {
        super("No fue posible generar una referencia única");
    }
}