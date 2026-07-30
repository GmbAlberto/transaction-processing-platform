package com.bethocr.transactionapi.exception;

import com.bethocr.transactionapi.dto.response.ApiResponse;
import com.bethocr.transactionapi.dto.response.ErrorCode;
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

    // Errores en DTO @Valid
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<List<ErrorDetail>>> handleValidation(MethodArgumentNotValidException exception) {
        LOGGER.debug("Solicitud con datos inválidos", exception);

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

    // El secreto enviado por el cliente no pudo descifrarse
    @ExceptionHandler(DecryptionException.class)
    public ResponseEntity<ApiResponse<Void>> handleDecryptionException(DecryptionException exception) {
        LOGGER.warn("No fue posible descifrar el secreto recibido: {}", exception.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "El secreto enviado no tiene un formato válido o no pudo ser descifrado",
                ErrorCode.DECRYPTION_ERROR,
                null
        );
    }

    // No fue posible establecer comunicación con transaction-service
    @ExceptionHandler(TransactionServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionServiceUnavailable(TransactionServiceUnavailableException exception) {
        LOGGER.error("transaction-service no se encuentra disponible: {}", exception.getMessage(), exception);

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de transacciones no se encuentra disponible temporalmente",
                ErrorCode.TRANSACTION_SERVICE_UNAVAILABLE,
                null
        );
    }

    // transaction-service respondió correctamente a nivel HTTP, pero regresó una respuesta de error
    @ExceptionHandler(TransactionServiceException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionServiceException(TransactionServiceException exception) {
        LOGGER.error("transaction-service respondió con estatus {} y código {}", exception.getStatus().value(), exception.getResponse().code());

        return buildErrorResponse(
                HttpStatus.valueOf(exception.getStatus().value()),
                exception.getResponse().message(),
                mapRemoteErrorCode(exception.getResponse().code()),
                null
        );
    }

    // transaction-service respondió, pero el cuerpo no tiene la estructura esperada
    @ExceptionHandler(InvalidRemoteResponseException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidRemoteResponse(InvalidRemoteResponseException exception) {
        LOGGER.error("transaction-service devolvió una respuesta inválida: {}", exception.getMessage());

        return buildErrorResponse(
                HttpStatus.BAD_GATEWAY,
                "El servicio de transacciones devolvió una respuesta inválida",
                ErrorCode.INVALID_TRANSACTION_SERVICE_RESPONSE,
                null
        );
    }

    @ExceptionHandler(InvalidTransactionStatusException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidTransactionStatus(InvalidTransactionStatusException exception) {
        LOGGER.warn("Cambio de estatus no permitido: {}", exception.getMessage());

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                ErrorCode.INVALID_TRANSACTION_STATUS,
                null
        );
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidCredentials(InvalidCredentialsException exception) {
        return buildErrorResponse(
                HttpStatus.UNAUTHORIZED,
                exception.getMessage(),
                ErrorCode.INVALID_CREDENTIALS,
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

    private ErrorCode mapRemoteErrorCode(ErrorCode remoteCode) {
        if (remoteCode == null) {
            return ErrorCode.TRANSACTION_SERVICE_ERROR;
        }

        return remoteCode;
    }
}