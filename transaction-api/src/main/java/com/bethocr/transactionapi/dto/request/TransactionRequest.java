package com.bethocr.transactionapi.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionRequest(
        @JsonProperty("operacion")
        @NotBlank(message = "La operación es obligatoria")
        @Size(max = 20, message = "La op                        eración no puede superar los 20 caracteres")
        @Pattern(
                regexp = "^[\\p{L} ]+$",
                message = "La operación solo puede contener letras y espacios"
        )
        String operation,

        @JsonProperty("importe")
        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01", message = "El importe debe ser mayor que cero")
        @Digits(
                integer = 17,
                fraction = 2,
                message = "El importe admite hasta 17 enteros y 2 decimales"
        )
        BigDecimal amount,

        @JsonProperty("cliente")
        @NotBlank(message = "El cliente es obligatorio")
        @Size(max = 60, message = "El cliente no puede superar los 60 caracteres")
        @Pattern(
                regexp = "^[\\p{L} .'-]+$",
                message = "El cliente contiene caracteres no permitidos"
        )
        String customer,

        @JsonProperty("secreto")
        @NotBlank(message = "El secreto es obligatorio")
        @Size(
                max = 2048,
                message = "El secreto cifrado no puede superar los 2048 caracteres"
        )
        String secret
) {
}