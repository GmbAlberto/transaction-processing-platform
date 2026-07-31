package com.bethocr.transactionapi.service.impl;

import com.bethocr.transactionapi.config.JwtProperties;
import com.bethocr.transactionapi.dto.request.LoginRequest;
import com.bethocr.transactionapi.dto.response.LoginResponse;
import com.bethocr.transactionapi.entity.User;
import com.bethocr.transactionapi.exception.InvalidCredentialsException;
import com.bethocr.transactionapi.repository.UserRepository;
import com.bethocr.transactionapi.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private JwtProperties jwtProperties;

    private AuthenticationServiceImpl authenticationService;

    @BeforeEach
    void setUp() {
        jwtProperties = new JwtProperties("test-secret-key", 10);

        authenticationService = new AuthenticationServiceImpl(
                userRepository,
                passwordEncoder,
                jwtService,
                jwtProperties
        );
    }

    @Test
    @DisplayName("Debe autenticar al usuario y devolver el token cuando las credenciales son correctas")
    void shouldLoginSuccessfullyWhenCredentialsAreValid() {
        LoginRequest request = new LoginRequest(
                "usuario",
                "abc123#"
        );

        User user = new User();
        user.setUsername("usuario");
        user.setPassword("$2a$10$encodedPassword");

        String generatedToken = "generated.jwt.token";

        when(userRepository.findByUsername("usuario"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "abc123#",
                "$2a$10$encodedPassword"
        )).thenReturn(true);

        when(jwtService.generateToken("usuario"))
                .thenReturn(generatedToken);

        LoginResponse response = authenticationService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo(generatedToken);
        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(600);

        verify(userRepository).findByUsername("usuario");

        verify(passwordEncoder).matches("abc123#", "$2a$10$encodedPassword");

        verify(jwtService).generateToken("usuario");
    }

    @Test
    @DisplayName("Debe lanzar InvalidCredentialsException cuando el usuario no existe")
    void shouldThrowInvalidCredentialsExceptionWhenUserDoesNotExist() {
        LoginRequest request = new LoginRequest("usuario-inexistente", "abc123#");

        when(userRepository.findByUsername("usuario-inexistente"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository)
                .findByUsername("usuario-inexistente");

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    @DisplayName("Debe lanzar InvalidCredentialsException cuando la contraseña es incorrecta")
    void shouldThrowInvalidCredentialsExceptionWhenPasswordIsInvalid() {
        LoginRequest request = new LoginRequest("usuario", "password-incorrecto");

        User user = new User();
        user.setUsername("usuario");
        user.setPassword("$2a$10$encodedPassword");

        when(userRepository.findByUsername("usuario"))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(
                "password-incorrecto",
                "$2a$10$encodedPassword"
        )).thenReturn(false);

        assertThatThrownBy(() -> authenticationService.login(request))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(userRepository).findByUsername("usuario");

        verify(passwordEncoder).matches(
                "password-incorrecto",
                "$2a$10$encodedPassword"
        );

        verify(jwtService, never()).generateToken("usuario");
    }
}