package com.alexsandroandre.tradecore.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record Transaction(
    String transactionId,
    String accountId,
    BigDecimal amount,
    String currency,
    OffsetDateTime timestamp,
    String source,
    TransactionStatus status
) {

    public enum TransactionStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    public Transaction {
    }

    public Transaction withStatus(TransactionStatus newStatus) {
        return new Transaction(
            transactionId,
            accountId,
            amount,
            currency,
            timestamp,
            source,
            newStatus
        );
    }

    public Transaction asProcessing() {
        return withStatus(TransactionStatus.PROCESSING);
    }

    public Transaction asCompleted() {
        return withStatus(TransactionStatus.COMPLETED);
    }

    public Transaction asFailed() {
        return withStatus(TransactionStatus.FAILED);
    }
}