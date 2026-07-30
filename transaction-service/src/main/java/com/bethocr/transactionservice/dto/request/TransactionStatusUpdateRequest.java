package com.bethocr.transactionservice.dto.request;

import com.bethocr.transactionservice.entity.TransactionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record TransactionStatusUpdateRequest(
        @NotNull(message = "El id es obligatorio")
        Long id,

        @NotBlank(message = "La referencia es obligatoria")
        @Pattern(
                regexp = "\\d{6}",
                message = "La referencia debe contener 6 dígitos"
        )
        String reference,

        @NotNull(message = "El estatus es obligatorio")
        TransactionStatus status
) {
}
