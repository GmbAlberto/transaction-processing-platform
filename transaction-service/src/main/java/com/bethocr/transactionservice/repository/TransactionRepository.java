package com.bethocr.transactionservice.repository;

import com.bethocr.transactionservice.entity.Transaction;
import com.bethocr.transactionservice.entity.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByReference(String reference);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Transaction t
           SET t.status = :status
         WHERE t.id = :id
           AND t.reference = :reference
        """)
    int updateStatus(
            @Param("id") Long id,
            @Param("reference") String reference,
            @Param("status") TransactionStatus status
    );
}
