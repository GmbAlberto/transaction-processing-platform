package com.bethocr.transactionapi.controller;

import com.bethocr.transactionapi.dto.request.LoginRequest;
import com.bethocr.transactionapi.dto.response.ApiResponse;
import com.bethocr.transactionapi.dto.response.LoginResponse;
import com.bethocr.transactionapi.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController extends BaseController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.login(request);

        return ok(response);
    }
}