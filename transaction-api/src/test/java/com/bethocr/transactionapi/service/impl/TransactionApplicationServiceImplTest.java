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
import feign.FeignException;
import feign.RetryableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionApplicationServiceImplTest {

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private TransactionServiceClient transactionServiceClient;


    private TransactionApplicationServiceImpl createService() {
        return new TransactionApplicationServiceImpl(encryptionService, transactionServiceClient);
    }

    @Test
    @DisplayName("Debe procesar una transacción correctamente")
    void shouldProcessTransactionSuccessfully() {
        TransactionApplicationServiceImpl service = createService();

        TransactionRequest request = new TransactionRequest("venta", new BigDecimal("100.00"), "Angel", "secreto-cifrado");

        ApiResponse<TransactionServiceResponse> remoteResponse = mockApiResponse();

        TransactionServiceResponse serviceResponse = mock(TransactionServiceResponse.class);

        TransactionResponse expectedResponse = mock(TransactionResponse.class);

        when(encryptionService.decrypt("secreto-cifrado"))
                .thenReturn("secreto-descifrado");

        when(remoteResponse.success()).thenReturn(true);
        when(remoteResponse.data()).thenReturn(serviceResponse);

        when(transactionServiceClient.createTransaction(any()))
                .thenReturn(remoteResponse);

        try (MockedStatic<TransactionMapper> mapper = mockStatic(TransactionMapper.class)) {

            mapper.when(() ->
                            TransactionMapper.toAPIResponse(serviceResponse))
                    .thenReturn(expectedResponse);


            TransactionResponse result = service.processTransaction(request);


            assertThat(result).isSameAs(expectedResponse);

            verify(encryptionService)
                    .decrypt("secreto-cifrado");

            ArgumentCaptor<TransactionServiceRequest> captor =
                    ArgumentCaptor.forClass(
                            TransactionServiceRequest.class
                    );

            verify(transactionServiceClient)
                    .createTransaction(captor.capture());

            TransactionServiceRequest capturedRequest = captor.getValue();

            assertThat(capturedRequest.operation())
                    .isEqualTo("venta");

            assertThat(capturedRequest.amount())
                    .isEqualByComparingTo("100.00");

            assertThat(capturedRequest.customer())
                    .isEqualTo("Angel");

            assertThat(capturedRequest.secret())
                    .isEqualTo("secreto-descifrado");

            mapper.verify(() ->
                    TransactionMapper.toAPIResponse(serviceResponse)
            );
        }
    }

    @Test
    @DisplayName("No debe llamar al servicio remoto cuando falla el descifrado")
    void shouldNotCallRemoteServiceWhenDecryptionFails() {
        TransactionApplicationServiceImpl service = createService();

        TransactionRequest request = new TransactionRequest("venta", new BigDecimal("100.00"), "Angel","secreto-invalido");

        RuntimeException decryptionException = new RuntimeException("Error de descifrado");

        when(encryptionService.decrypt("secreto-invalido"))
                .thenThrow(decryptionException);

        assertThatThrownBy(() ->
                service.processTransaction(request)
        ).isSameAs(decryptionException);

        verifyNoInteractions(transactionServiceClient);
    }

    @Test
    @DisplayName("Debe buscar una transacción por ID correctamente")
    void shouldFindTransactionByIdSuccessfully() {
        TransactionApplicationServiceImpl service = createService();

        ApiResponse<TransactionServiceResponse> remoteResponse = mockApiResponse();

        TransactionServiceResponse serviceResponse = mock(TransactionServiceResponse.class);

        TransactionResponse expectedResponse = mock(TransactionResponse.class);

        when(transactionServiceClient.findById(10L))
                .thenReturn(remoteResponse);

        when(remoteResponse.success()).thenReturn(true);
        when(remoteResponse.data()).thenReturn(serviceResponse);

        try (MockedStatic<TransactionMapper> mapper = mockStatic(TransactionMapper.class)) {

            mapper.when(() ->
                            TransactionMapper.toAPIResponse(serviceResponse))
                    .thenReturn(expectedResponse);

            TransactionResponse result = service.findById(10L);

            assertThat(result).isSameAs(expectedResponse);

            verify(transactionServiceClient).findById(10L);
        }
    }

    @Test
    @DisplayName("Debe cancelar una transacción aprobada")
    void shouldCancelApprovedTransactionSuccessfully() {
        TransactionApplicationServiceImpl service = createService();

        Long transactionId = 10L;
        String reference = "262737";

        TransactionCancellationRequest request = new TransactionCancellationRequest(transactionId, reference);

        ApiResponse<TransactionServiceResponse> findResponse = mockApiResponse();

        TransactionServiceResponse findServiceResponse = mock(TransactionServiceResponse.class);

        TransactionResponse approvedTransaction = mock(TransactionResponse.class);

        ApiResponse<TransactionServiceResponse> updateResponse =mockApiResponse();

        TransactionServiceResponse updatedServiceResponse = mock(TransactionServiceResponse.class);

        TransactionResponse cancelledTransaction = mock(TransactionResponse.class);

        when(transactionServiceClient.findById(transactionId))
                .thenReturn(findResponse);

        when(findResponse.success()).thenReturn(true);
        when(findResponse.data()).thenReturn(findServiceResponse);

        when(approvedTransaction.status())
                .thenReturn(
                        TransactionStatus.APPROVED.getDescription()
                );

        when(transactionServiceClient.updateStatus(any()))
                .thenReturn(updateResponse);

        when(updateResponse.success()).thenReturn(true);
        when(updateResponse.data()).thenReturn(updatedServiceResponse);

        try (MockedStatic<TransactionMapper> mapper = mockStatic(TransactionMapper.class)) {

            mapper.when(() ->
                            TransactionMapper.toAPIResponse(
                                    findServiceResponse
                            ))
                    .thenReturn(approvedTransaction);

            mapper.when(() ->
                            TransactionMapper.toAPIResponse(
                                    updatedServiceResponse
                            ))
                    .thenReturn(cancelledTransaction);

            TransactionResponse result = service.cancelTransaction(request);


            assertThat(result).isSameAs(cancelledTransaction);

            ArgumentCaptor<TransactionStatusUpdateRequest> captor =
                    ArgumentCaptor.forClass(
                            TransactionStatusUpdateRequest.class
                    );

            verify(transactionServiceClient).updateStatus(captor.capture());

            TransactionStatusUpdateRequest capturedRequest = captor.getValue();

            assertThat(capturedRequest.id()).isEqualTo(transactionId);

            assertThat(capturedRequest.reference()).isEqualTo(reference);

            assertThat(capturedRequest.status()).isEqualTo(TransactionStatus.CANCELLED);
        }
    }

    @Test
    @DisplayName("Debe rechazar la cancelación cuando la transacción no está aprobada")
    void shouldRejectCancellationWhenTransactionIsNotApproved() {
        TransactionApplicationServiceImpl service = createService();

        TransactionCancellationRequest request = new TransactionCancellationRequest(10L, "262737");

        ApiResponse<TransactionServiceResponse> remoteResponse = mockApiResponse();

        TransactionServiceResponse serviceResponse = mock(TransactionServiceResponse.class);

        TransactionResponse transaction = mock(TransactionResponse.class);

        when(transactionServiceClient.findById(10L))
                .thenReturn(remoteResponse);

        when(remoteResponse.success()).thenReturn(true);
        when(remoteResponse.data()).thenReturn(serviceResponse);

        when(transaction.status())
                .thenReturn(
                        TransactionStatus.CANCELLED.getDescription()
                );

        try (MockedStatic<TransactionMapper> mapper = mockStatic(TransactionMapper.class)) {

            mapper.when(() ->
                            TransactionMapper.toAPIResponse(serviceResponse))
                    .thenReturn(transaction);

            assertThatThrownBy(() ->
                    service.cancelTransaction(request)
            )
                    .isInstanceOf(
                            InvalidTransactionStatusException.class
                    )
                    .hasMessage(
                            "Solo se pueden cancelar transacciones aprobadas"
                    );

            verify(transactionServiceClient, never()).updateStatus(any());
        }
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando el servicio remoto devuelve null")
    void shouldThrowExceptionWhenRemoteResponseIsNull() {
        TransactionApplicationServiceImpl service = createService();

        when(transactionServiceClient.findById(10L))
                .thenReturn(null);

        assertThatThrownBy(() -> service.findById(10L))
                .isInstanceOf(
                        InvalidRemoteResponseException.class
                )
                .hasMessage(
                        "El servicio de transacciones devolvió una respuesta vacía"
                );
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la respuesta remota no es exitosa")
    void shouldThrowExceptionWhenRemoteResponseIsNotSuccessful() {
        TransactionApplicationServiceImpl service = createService();

        ApiResponse<TransactionServiceResponse> remoteResponse = mockApiResponse();

        when(transactionServiceClient.findById(10L))
                .thenReturn(remoteResponse);

        when(remoteResponse.success()).thenReturn(false);

        assertThatThrownBy(() -> service.findById(10L))
                .isInstanceOf(
                        InvalidRemoteResponseException.class
                )
                .hasMessage(
                        "El servicio de transacciones no devolvió una respuesta satisfactoria"
                );
    }

    @Test
    @DisplayName("Debe lanzar excepción cuando la respuesta remota no contiene datos")
    void shouldThrowExceptionWhenRemoteResponseHasNoData() {
        TransactionApplicationServiceImpl service = createService();

        ApiResponse<TransactionServiceResponse> remoteResponse = mockApiResponse();

        when(transactionServiceClient.findById(10L))
                .thenReturn(remoteResponse);

        when(remoteResponse.success()).thenReturn(true);
        when(remoteResponse.data()).thenReturn(null);

        assertThatThrownBy(() -> service.findById(10L))
                .isInstanceOf(
                        InvalidRemoteResponseException.class
                )
                .hasMessage(
                        "El servicio de transacciones no devolvió infromación de la transacción"
                );
    }

    @Test
    @DisplayName("Debe convertir RetryableException en TransactionServiceUnavailableException")
    void shouldConvertRetryableException() {
        TransactionApplicationServiceImpl service = createService();

        RetryableException retryableException = mock(RetryableException.class);

        when(transactionServiceClient.findById(10L))
                .thenThrow(retryableException);

        assertThatThrownBy(() -> service.findById(10L))
                .isInstanceOf(
                        TransactionServiceUnavailableException.class
                )
                .hasMessage(
                        "El servicio de transacciones no se encuentra disponible"
                )
                .hasCause(retryableException);
    }

    @Test
    @DisplayName("Debe convertir FeignException en TransactionServiceUnavailableException")
    void shouldConvertFeignException() {
        TransactionApplicationServiceImpl service = createService();

        FeignException feignException = mock(FeignException.class);

        when(transactionServiceClient.findById(10L))
                .thenThrow(feignException);

        assertThatThrownBy(() -> service.findById(10L))
                .isInstanceOf(
                        TransactionServiceUnavailableException.class
                )
                .hasMessage(
                        "No fue posible comunicarse correctamente con el servicio de transacciones"
                )
                .hasCause(feignException);
    }

    @Test
    @DisplayName("Debe obtener y transformar una página de transacciones")
    void shouldFindAllTransactionsSuccessfully() {
        TransactionApplicationServiceImpl service = createService();

        ApiResponse<PageResponse<TransactionServiceResponse>> remoteResponse = mockApiResponse();

        PageResponse<TransactionServiceResponse> servicePage = mockPageResponse();

        TransactionServiceResponse firstServiceTransaction = mock(TransactionServiceResponse.class);

        TransactionServiceResponse secondServiceTransaction = mock(TransactionServiceResponse.class);

        TransactionResponse firstTransaction = mock(TransactionResponse.class);

        TransactionResponse secondTransaction = mock(TransactionResponse.class);

        when(transactionServiceClient.findAll(0, 10, "id", "desc"
        )).thenReturn(remoteResponse);

        when(remoteResponse.data()).thenReturn(servicePage);

        when(servicePage.content()).thenReturn(
                List.of(
                        firstServiceTransaction,
                        secondServiceTransaction
                )
        );

        when(servicePage.page()).thenReturn(0);
        when(servicePage.size()).thenReturn(10);
        when(servicePage.totalElements()).thenReturn(2L);
        when(servicePage.totalPages()).thenReturn(1);
        when(servicePage.first()).thenReturn(true);
        when(servicePage.last()).thenReturn(true);
        when(servicePage.empty()).thenReturn(false);

        try (MockedStatic<TransactionMapper> mapper = mockStatic(TransactionMapper.class)) {

            mapper.when(() ->
                            TransactionMapper.toAPIResponse(
                                    firstServiceTransaction
                            ))
                    .thenReturn(firstTransaction);

            mapper.when(() ->
                            TransactionMapper.toAPIResponse(
                                    secondServiceTransaction
                            ))
                    .thenReturn(secondTransaction);

            PageResponse<TransactionResponse> result = service.findAll(0, 10, "id", "desc");

            assertThat(result.content())
                    .containsExactly(
                            firstTransaction,
                            secondTransaction
                    );

            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(2L);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            assertThat(result.empty()).isFalse();

            verify(transactionServiceClient).findAll(0, 10, "id", "desc");
        }
    }

    private <T> ApiResponse<T> mockApiResponse() {
        return mock(ApiResponse.class);
    }

    private <T> PageResponse<T> mockPageResponse() {
        return mock(PageResponse.class);
    }
}