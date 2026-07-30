package com.bethocr.transactionapi.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TransactionResponse(
        @JsonProperty("id")
        Long id,

        @JsonProperty("estatus")
        String status,

        @JsonProperty("referencia")
        String reference,

        @JsonProperty("operacion")
        String operation
) {
}