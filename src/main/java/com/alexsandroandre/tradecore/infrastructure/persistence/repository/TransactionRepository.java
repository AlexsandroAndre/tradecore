package com.alexsandroandre.tradecore.infrastructure.persistence.repository;

import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.time.Instant;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

    Optional<TransactionEntity> findByTransactionId(String transactionId);

    List<TransactionEntity> findByAccountId(String accountId);

    List<TransactionEntity> findByProcessingStatus(String processingStatus);

    @Query("SELECT t FROM TransactionEntity t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<TransactionEntity> findByDateRange(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    boolean existsByTransactionId(String transactionId);
}