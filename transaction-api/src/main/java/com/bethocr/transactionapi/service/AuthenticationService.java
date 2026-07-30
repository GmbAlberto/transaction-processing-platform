package com.bethocr.transactionapi.service;

import com.bethocr.transactionapi.dto.request.LoginRequest;
import com.bethocr.transactionapi.dto.response.LoginResponse;

public interface AuthenticationService {
    LoginResponse login(LoginRequest request);
}
