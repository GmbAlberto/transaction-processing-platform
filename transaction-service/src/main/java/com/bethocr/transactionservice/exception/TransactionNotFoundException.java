package com.bethocr.transactionservice.exception;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(Long id) {
        super("No se encontró la transacción con id " + id);
    }

    public TransactionNotFoundException(Long id, String reference) {
        super("No se encontró la transacción con id " + id + " y referencia " + reference);
    }
}
