package com.bethocr.transactionapi.exception;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Usuario o contraseña incorrectos");
    }
}