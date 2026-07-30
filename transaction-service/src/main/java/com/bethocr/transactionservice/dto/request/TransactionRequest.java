package com.bethocr.transactionservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "La operación es obligatoria")
        @Size(max = 20, message = "La operación no puede superar los 15 caracteres")
        @Pattern(
                regexp = "^[\\p{L} ]+$",
                message = "La operación solo puede contener letras y espacios"
        )
        String operation,

        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01", message = "El importe debe ser mayor que cero")
        @Digits(integer = 17, fraction = 2, message = "El importe admite hasta 17 enteros y 2 decimales")
        BigDecimal amount,

        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 60, message = "El cliente no puede superar los 60 caracteres")
        @Pattern(
                regexp = "^[\\p{L} ]+$",
                message = "El cliente solo puede contener letras y espacios"
        )
        String customer,

        @NotBlank(message = "El secreto es obligatorio")
        @Size(max = 255, message = "El secreto cifrado no puede superar los 255 caracteres")
        String secret
) {
}