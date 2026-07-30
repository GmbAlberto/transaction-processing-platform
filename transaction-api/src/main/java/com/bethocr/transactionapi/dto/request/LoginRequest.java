package com.bethocr.transactionapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Size(max = 60, message = "El usuario no puede superar los 60 caracteres")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(max = 100, message = "La contraseña no puede superar los 100 caracteres")
        String password
) {
}