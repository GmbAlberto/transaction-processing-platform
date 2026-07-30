package com.bethocr.transactionservice.exception;

import com.bethocr.transactionservice.dto.response.ApiResponse;
import com.bethocr.transactionservice.dto.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionNotFound(TransactionNotFoundException exception) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                ErrorCode.TRANSACTION_NOT_FOUND,
                null
        );
    }

    // Errores en DTO @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorDetail>>> handleValidation(MethodArgumentNotValidException exception) {
        List<ErrorDetail> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorDetail(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "La solicitud contiene datos inválidos",
                ErrorCode.VALIDATION_ERROR,
                errors
        );
    }

    // No se puede convertir JSON a DTO
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleMalformedJson(HttpMessageNotReadableException exception) {
        LOGGER.debug("No fue posible interpretar la solicitud", exception);

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "El cuerpo de la solicitud no tiene un formato válido",
                ErrorCode.MALFORMED_JSON,
                null
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        LOGGER.error("Violación de integridad de datos", exception);

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "La información no está completa o entra en conflicto con un registro existente",
                ErrorCode.DATA_INTEGRITY_VIOLATION,
                null
        );
    }

    // Se alcanzó el número máximo de intentos de generar una referencia unica
    @ExceptionHandler(ReferenceGenerationException.class)
    public ResponseEntity<ApiResponse<Void>> handleReferenceGeneration(ReferenceGenerationException exception) {
        LOGGER.error("No fue posible generar una referencia única", exception);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                exception.getMessage(),
                ErrorCode.REFERENCE_GENERATION_ERROR,
                null
        );
    }

    @ExceptionHandler(InvalidPaginationException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidPagination(InvalidPaginationException exception) {
        LOGGER.warn("Parámetros de paginación inválidos: {}", exception.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                ErrorCode.INVALID_PAGINATION,
                null
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpectedException(Exception exception) {
        LOGGER.error("Error inesperado al procesar la solicitud", exception);

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado al procesar la solicitud",
                ErrorCode.INTERNAL_SERVER_ERROR,
                null
        );
    }

    private <T> ResponseEntity<ApiResponse<T>> buildErrorResponse(HttpStatus status, String message, ErrorCode code, T data) {
        ApiResponse<T> response = ApiResponse.error(message, code, data);

        return ResponseEntity
                .status(status)
                .body(response);
    }
}