package com.bethocr.transactionapi.config;

import com.bethocr.transactionapi.dto.response.ApiResponse;
import com.bethocr.transactionapi.dto.response.ErrorCode;
import com.bethocr.transactionapi.exception.TransactionServiceException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatusCode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;

public class TransactionServiceErrorDecoder implements ErrorDecoder {
    private final ObjectMapper objectMapper;

    public TransactionServiceErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        HttpStatusCode status = HttpStatusCode.valueOf(response.status());

        if (response.body() == null) {
            return new TransactionServiceException(
                    status,
                    ApiResponse.error(
                            "El servicio de transacciones respondió con un error",
                            ErrorCode.TRANSACTION_SERVICE_ERROR,
                            null
                    )
            );
        }

        try (InputStream inputStream = response.body().asInputStream()) {
            ApiResponse<?> apiResponse = objectMapper.readValue(
                    inputStream,
                    ApiResponse.class
            );

            return new TransactionServiceException(status, apiResponse);

        } catch (IOException exception) {
            return new TransactionServiceException(
                    status,
                    ApiResponse.error(
                            "El servicio de transacciones devolvió una respuesta no válida",
                            ErrorCode.TRANSACTION_SERVICE_ERROR,
                            null
                    )
            );
        }
    }
}