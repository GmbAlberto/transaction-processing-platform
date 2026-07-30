package com.bethocr.transactionapi.service.impl;

import com.bethocr.transactionapi.dto.request.TransactionCancellationRequest;
import com.bethocr.transactionapi.dto.request.TransactionRequest;
import com.bethocr.transactionapi.dto.request.TransactionServiceRequest;
import com.bethocr.transactionapi.dto.request.TransactionStatusUpdateRequest;
import com.bethocr.transactionapi.dto.response.ApiResponse;
import com.bethocr.transactionapi.dto.response.PageResponse;
import com.bethocr.transactionapi.dto.response.TransactionResponse;
import com.bethocr.transactionapi.dto.response.TransactionServiceResponse;
import com.bethocr.transactionapi.entity.TransactionStatus;
import com.bethocr.transactionapi.exception.InvalidRemoteResponseException;
import com.bethocr.transactionapi.exception.InvalidTransactionStatusException;
import com.bethocr.transactionapi.exception.TransactionServiceUnavailableException;
import com.bethocr.transactionapi.infrastructure.TransactionServiceClient;
import com.bethocr.transactionapi.mapper.TransactionMapper;
import com.bethocr.transactionapi.service.EncryptionService;
import com.bethocr.transactionapi.service.TransactionApplicationService;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class TransactionApplicationServiceImpl implements TransactionApplicationService {
    private final EncryptionService encryptionService;
    private final TransactionServiceClient transactionServiceClient;

    @Override
    public TransactionResponse processTransaction(TransactionRequest request) {
        String decryptedSecret = encryptionService.decrypt(request.secret());

        TransactionServiceRequest transactionServiceRequest = new TransactionServiceRequest(
                request.operation(),
                request.amount(),
                request.customer(),
                decryptedSecret
        );

        ApiResponse<TransactionServiceResponse> response = executeRemoteCall(() ->
                        transactionServiceClient.createTransaction(transactionServiceRequest));

        return extractTransactionResponse(response);
    }

    @Override
    public TransactionResponse cancelTransaction(TransactionCancellationRequest request) {
        TransactionResponse transaction = findById(request.id());

        if (!TransactionStatus.APPROVED.getDescription()
                .equalsIgnoreCase(transaction.status())) {
            throw new InvalidTransactionStatusException("Solo se pueden cancelar transacciones aprobadas");
        }

        TransactionStatusUpdateRequest requestTransactionService = new TransactionStatusUpdateRequest(request.id(), request.reference(), TransactionStatus.CANCELLED);

        ApiResponse<TransactionServiceResponse> response = executeRemoteCall(() ->
                        transactionServiceClient.updateStatus(requestTransactionService));

        return extractTransactionResponse(response);
    }

    @Override
    public TransactionResponse findById(Long id) {
        ApiResponse<TransactionServiceResponse> response = executeRemoteCall(() ->
                        transactionServiceClient.findById(id));

        return extractTransactionResponse(response);
    }

    @Override
    public PageResponse<TransactionResponse> findAll(int page, int size, String sortBy, String direction) {
        ApiResponse<PageResponse<TransactionServiceResponse>> response = executeRemoteCall(() ->
                        transactionServiceClient.findAll(page, size, sortBy, direction));

        return extractPageTransactionResponse(response);
    }

    private PageResponse<TransactionResponse> extractPageTransactionResponse(ApiResponse<PageResponse<TransactionServiceResponse>> response) {
        PageResponse<TransactionServiceResponse> servicePage = response.data();

        List<TransactionResponse> transactions =
                servicePage.content()
                        .stream()
                        .map(TransactionMapper::toAPIResponse)
                        .toList();

        return new PageResponse<>(
                transactions,
                servicePage.page(),
                servicePage.size(),
                servicePage.totalElements(),
                servicePage.totalPages(),
                servicePage.first(),
                servicePage.last(),
                servicePage.empty()
        );
    }

    private <T> T executeRemoteCall(Supplier<T> action) {
        try {
            return action.get();

        } catch (RetryableException exception) {
            throw new TransactionServiceUnavailableException(
                    "El servicio de transacciones no se encuentra disponible",
                    exception
            );

        } catch (FeignException exception) {
            throw new TransactionServiceUnavailableException(
                    "No fue posible comunicarse correctamente con el servicio de transacciones",
                    exception
            );
        }
    }

    private TransactionResponse extractTransactionResponse(ApiResponse<TransactionServiceResponse> response) {
        if (response == null) {
            throw new InvalidRemoteResponseException("El servicio de transacciones devolvió una respuesta vacía");
        }

        if (!response.success()) {
            throw new InvalidRemoteResponseException("El servicio de transacciones no devolvió una respuesta satisfactoria");
        }

        if (response.data() == null) {
            throw new InvalidRemoteResponseException("El servicio de transacciones no devolvió infromación de la transacción");
        }

        return TransactionMapper.toAPIResponse(response.data());
    }
}