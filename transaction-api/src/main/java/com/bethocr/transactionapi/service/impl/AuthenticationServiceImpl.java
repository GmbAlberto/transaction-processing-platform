package com.bethocr.transactionapi.service.impl;

import com.bethocr.transactionapi.config.JwtProperties;
import com.bethocr.transactionapi.dto.request.LoginRequest;
import com.bethocr.transactionapi.dto.response.LoginResponse;
import com.bethocr.transactionapi.entity.User;
import com.bethocr.transactionapi.exception.InvalidCredentialsException;
import com.bethocr.transactionapi.repository.UserRepository;
import com.bethocr.transactionapi.service.AuthenticationService;
import com.bethocr.transactionapi.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    private final JwtProperties jwtProperties;

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches = passwordEncoder.matches(
                request.password(),
                user.getPassword()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user.getUsername());

        return new LoginResponse(
                token,
                "Bearer",
                jwtProperties.expirationMinutes() * 60
        );
    }
}