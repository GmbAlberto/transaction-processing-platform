package com.bethocr.transactionapi.service;

public interface JwtService {
    String generateToken(String username);

    String extractUsername(String token);

    boolean isTokenValid(String token, String username);
}
