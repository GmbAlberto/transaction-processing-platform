package com.bethocr.transactionapi.entity;

public enum TransactionStatus {
    APPROVED("Aprobada"),
    REJECTED("Rechazada"),
    CANCELLED("Cancelada");

    private final String description;

    TransactionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}