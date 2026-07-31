package com.bethocr.transactionservice.service.impl;

import com.bethocr.transactionservice.dto.request.TransactionRequest;
import com.bethocr.transactionservice.dto.request.TransactionStatusUpdateRequest;
import com.bethocr.transactionservice.dto.response.PageResponse;
import com.bethocr.transactionservice.dto.response.TransactionResponse;
import com.bethocr.transactionservice.entity.Transaction;
import com.bethocr.transactionservice.entity.TransactionStatus;
import com.bethocr.transactionservice.exception.InvalidPaginationException;
import com.bethocr.transactionservice.exception.TransactionNotFoundException;
import com.bethocr.transactionservice.mapper.TransactionMapper;
import com.bethocr.transactionservice.repository.TransactionRepository;
import com.bethocr.transactionservice.service.ReferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private ReferenceService referenceService;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(transactionRepository, transactionMapper, referenceService);
    }

    @Nested
    @DisplayName("create")
    class CreateTests {

        @Test
        @DisplayName("Debe crear una transacción con referencia y estado APPROVED")
        void shouldCreateTransaction() {
            TransactionRequest request = createTransactionRequest();

            Transaction mappedTransaction = createTransaction(null);
            Transaction savedTransaction = createTransaction(1L);
            TransactionResponse expectedResponse = createTransactionResponse(1L);

            when(transactionMapper.toEntity(request)).thenReturn(mappedTransaction);

            when(referenceService.generate()).thenReturn("262737");

            when(transactionRepository.save(mappedTransaction)).thenReturn(savedTransaction);

            when(transactionMapper.toResponse(savedTransaction)).thenReturn(expectedResponse);

            TransactionResponse response = transactionService.create(request);

            assertThat(response).isEqualTo(expectedResponse);

            assertThat(mappedTransaction.getReference()).isEqualTo("262737");

            assertThat(mappedTransaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);

            verify(transactionMapper).toEntity(request);
            verify(referenceService).generate();
            verify(transactionRepository).save(mappedTransaction);
            verify(transactionMapper).toResponse(savedTransaction);

            verifyNoMoreInteractions(transactionRepository, transactionMapper, referenceService);
        }

        @Test
        @DisplayName("Debe guardar la misma entidad producida por el mapper")
        void shouldSaveMappedTransaction() {
            TransactionRequest request = createTransactionRequest();
            Transaction transaction = createTransaction(null);
            Transaction savedTransaction = createTransaction(1L);
            TransactionResponse response = createTransactionResponse(1L);

            when(transactionMapper.toEntity(request)).thenReturn(transaction);

            when(referenceService.generate()).thenReturn("ABC123");

            when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTransaction);

            when(transactionMapper.toResponse(savedTransaction)).thenReturn(response);

            transactionService.create(request);

            ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);

            verify(transactionRepository).save(captor.capture());

            Transaction capturedTransaction = captor.getValue();

            assertThat(capturedTransaction).isSameAs(transaction);

            assertThat(capturedTransaction.getReference()).isEqualTo("ABC123");

            assertThat(capturedTransaction.getStatus()).isEqualTo(TransactionStatus.APPROVED);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("Debe devolver la transacción cuando existe")
        void shouldFindTransactionById() {
            Long id = 1L;

            Transaction transaction = createTransaction(id);
            TransactionResponse expectedResponse = createTransactionResponse(id);

            when(transactionRepository.findById(id)).thenReturn(Optional.of(transaction));

            when(transactionMapper.toResponse(transaction)).thenReturn(expectedResponse);

            TransactionResponse response = transactionService.findById(id);

            assertThat(response).isEqualTo(expectedResponse);

            verify(transactionRepository).findById(id);
            verify(transactionMapper).toResponse(transaction);
            verifyNoInteractions(referenceService);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la transacción no existe")
        void shouldThrowExceptionWhenTransactionDoesNotExist() {
            Long id = 99L;

            when(transactionRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.findById(id)).isInstanceOf(TransactionNotFoundException.class);

            verify(transactionRepository).findById(id);
            verifyNoInteractions(transactionMapper, referenceService);
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatusTests {

        @Test
        @DisplayName("Debe actualizar el estado y devolver la transacción actualizada")
        void shouldUpdateTransactionStatus() {
            TransactionStatusUpdateRequest request = new TransactionStatusUpdateRequest(1L, "262737", TransactionStatus.CANCELLED);

            Transaction updatedTransaction = createTransaction(1L);
            updatedTransaction.setStatus(TransactionStatus.CANCELLED);

            TransactionResponse expectedResponse = createTransactionResponse(1L);

            when(transactionRepository.updateStatus(request.id(), request.reference(), request.status())).thenReturn(1);

            when(transactionRepository.findById(request.id())).thenReturn(Optional.of(updatedTransaction));

            when(transactionMapper.toResponse(updatedTransaction)).thenReturn(expectedResponse);

            TransactionResponse response = transactionService.updateStatus(request);

            assertThat(response).isEqualTo(expectedResponse);

            verify(transactionRepository).updateStatus(1L, "262737", TransactionStatus.CANCELLED);

            verify(transactionRepository).findById(1L);
            verify(transactionMapper).toResponse(updatedTransaction);
            verifyNoInteractions(referenceService);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando ninguna transacción fue actualizada")
        void shouldThrowExceptionWhenNoTransactionWasUpdated() {
            TransactionStatusUpdateRequest request = new TransactionStatusUpdateRequest(99L, "NO-EXISTE", TransactionStatus.CANCELLED);

            when(transactionRepository.updateStatus(request.id(), request.reference(), request.status())).thenReturn(0);

            assertThatThrownBy(() -> transactionService.updateStatus(request)).isInstanceOf(TransactionNotFoundException.class);

            verify(transactionRepository).updateStatus(99L, "NO-EXISTE", TransactionStatus.CANCELLED);

            verify(transactionRepository, never()).findById(anyLong());

            verifyNoInteractions(transactionMapper, referenceService);
        }

        @Test
        @DisplayName("Debe lanzar excepción si se actualizó pero después no se encuentra por ID")
        void shouldThrowExceptionWhenUpdatedTransactionCannotBeFound() {
            TransactionStatusUpdateRequest request = new TransactionStatusUpdateRequest(1L, "262737", TransactionStatus.CANCELLED);

            when(transactionRepository.updateStatus(request.id(), request.reference(), request.status())).thenReturn(1);

            when(transactionRepository.findById(request.id())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> transactionService.updateStatus(request)).isInstanceOf(TransactionNotFoundException.class);

            verify(transactionRepository).updateStatus(1L, "262737", TransactionStatus.CANCELLED);

            verify(transactionRepository).findById(1L);
            verifyNoInteractions(transactionMapper, referenceService);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("Debe devolver una página de transacciones ordenada ascendentemente")
        void shouldReturnTransactionPageSortedAscending() {
            Transaction transaction1 = createTransaction(1L);
            Transaction transaction2 = createTransaction(2L);

            TransactionResponse response1 = createTransactionResponse(1L);
            TransactionResponse response2 = createTransactionResponse(2L);

            Page<Transaction> transactionPage = new PageImpl<>(List.of(transaction1, transaction2), org.springframework.data.domain.PageRequest.of(0, 10), 2);

            when(transactionRepository.findAll(any(Pageable.class))).thenReturn(transactionPage);

            when(transactionMapper.toResponse(transaction1)).thenReturn(response1);

            when(transactionMapper.toResponse(transaction2)).thenReturn(response2);

            PageResponse<TransactionResponse> result = transactionService.findAll(0, 10, "createdAt", "asc");

            assertThat(result.content()).containsExactly(response1, response2);

            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isEqualTo(2);
            assertThat(result.totalPages()).isEqualTo(1);
            assertThat(result.first()).isTrue();
            assertThat(result.last()).isTrue();
            assertThat(result.empty()).isFalse();

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(transactionRepository).findAll(pageableCaptor.capture());

            Pageable pageable = pageableCaptor.getValue();

            assertThat(pageable.getPageNumber()).isZero();
            assertThat(pageable.getPageSize()).isEqualTo(10);

            Sort.Order order = pageable.getSort().getOrderFor("createdAt");

            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);

            verify(transactionMapper).toResponse(transaction1);
            verify(transactionMapper).toResponse(transaction2);
            verifyNoInteractions(referenceService);
        }

        @Test
        @DisplayName("Debe aceptar dirección DESC en mayúsculas")
        void shouldAcceptUppercaseDescendingDirection() {
            Page<Transaction> emptyPage = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(0, 20), 0);

            when(transactionRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            PageResponse<TransactionResponse> result = transactionService.findAll(0, 20, "amount", "DESC");

            assertThat(result.content()).isEmpty();
            assertThat(result.empty()).isTrue();

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

            verify(transactionRepository).findAll(pageableCaptor.capture());

            Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("amount");

            assertThat(order).isNotNull();
            assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
        }

        @Test
        @DisplayName("Debe devolver correctamente una página vacía")
        void shouldReturnEmptyPage() {
            Page<Transaction> emptyPage = new PageImpl<>(List.of(), org.springframework.data.domain.PageRequest.of(2, 10), 0);

            when(transactionRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            PageResponse<TransactionResponse> result = transactionService.findAll(2, 10, "id", "asc");

            assertThat(result.content()).isEmpty();
            assertThat(result.page()).isEqualTo(2);
            assertThat(result.size()).isEqualTo(10);
            assertThat(result.totalElements()).isZero();
            assertThat(result.totalPages()).isZero();
            assertThat(result.first()).isFalse();
            assertThat(result.last()).isTrue();
            assertThat(result.empty()).isTrue();

            verify(transactionRepository).findAll(any(Pageable.class));

            verifyNoInteractions(transactionMapper, referenceService);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la página es negativa")
        void shouldThrowExceptionWhenPageIsNegative() {
            assertThatThrownBy(() -> transactionService.findAll(-1, 10, "id", "asc")).isInstanceOf(InvalidPaginationException.class).hasMessage("El número de página no puede ser negativo");

            verifyNoInteractions(transactionRepository, transactionMapper, referenceService);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el tamaño es cero")
        void shouldThrowExceptionWhenSizeIsZero() {
            assertThatThrownBy(() -> transactionService.findAll(0, 0, "id", "asc")).isInstanceOf(InvalidPaginationException.class).hasMessage("El número de registros por página debe estar entre 1 y 100");

            verifyNoInteractions(transactionRepository, transactionMapper, referenceService);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el tamaño supera 100")
        void shouldThrowExceptionWhenSizeExceedsMaximum() {
            assertThatThrownBy(() -> transactionService.findAll(0, 101, "id", "asc")).isInstanceOf(InvalidPaginationException.class).hasMessage("El número de registros por página debe estar entre 1 y 100");

            verifyNoInteractions(transactionRepository, transactionMapper, referenceService);
        }

        @Test
        @DisplayName("Debe aceptar tamaños límite de 1 y 100")
        void shouldAcceptMinimumAndMaximumPageSizes() {
            Page<Transaction> emptyPage = Page.empty();

            when(transactionRepository.findAll(any(Pageable.class))).thenReturn(emptyPage);

            transactionService.findAll(0, 1, "id", "asc");

            transactionService.findAll(0, 100, "id", "desc");

            verify(transactionRepository, times(2)).findAll(any(Pageable.class));
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando el campo de ordenamiento no es válido")
        void shouldThrowExceptionWhenSortFieldIsInvalid() {
            assertThatThrownBy(() -> transactionService.findAll(0, 10, "password", "asc")).isInstanceOf(InvalidPaginationException.class).hasMessageContaining("El campo de ordenamiento no es válido");

            verifyNoInteractions(transactionRepository, transactionMapper, referenceService);
        }

        @Test
        @DisplayName("Debe lanzar excepción cuando la dirección no es válida")
        void shouldThrowExceptionWhenDirectionIsInvalid() {
            assertThatThrownBy(() -> transactionService.findAll(0, 10, "id", "ascending")).isInstanceOf(InvalidPaginationException.class).hasMessage("La dirección debe ser asc o desc");

            verifyNoInteractions(transactionRepository, transactionMapper, referenceService);
        }
    }

    private TransactionRequest createTransactionRequest() {
        return new TransactionRequest("venta", new BigDecimal("100.00"), "Angel", "secreto-cifrado");
    }

    private Transaction createTransaction(Long id) {
        Transaction transaction = new Transaction();

        transaction.setId(id);
        transaction.setOperation("venta");
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setCustomer("Angel");
        transaction.setSecret("secreto-descifrado");
        transaction.setReference("262737");
        transaction.setStatus(TransactionStatus.APPROVED);

        return transaction;
    }

    private TransactionResponse createTransactionResponse(Long id) {
        return new TransactionResponse(id, TransactionStatus.APPROVED.name(), "262737", "venta", "usuario", BigDecimal.valueOf(100.00), LocalDateTime.now());
    }
}