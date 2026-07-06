package com.alexsandroandre.tradecore.infrastructure.persistence.builder;

import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TransactionEntityTestBuilder {

    private String transactionId;
    private String accountId;
    private BigDecimal amount;
    private String currency;
    private String source;
    private Instant timestamp;
    private String processingStatus;
    private Instant createdAt;

    private TransactionEntityTestBuilder() {
        this.transactionId = UUID.randomUUID().toString();
        this.accountId = IntegrationTestConstants.VALID_ACCOUNT_ID;
        this.amount = IntegrationTestConstants.VALID_AMOUNT;
        this.currency = IntegrationTestConstants.VALID_CURRENCY;
        this.source = IntegrationTestConstants.VALID_SOURCE;
        this.timestamp = IntegrationTestConstants.VALID_TIMESTAMP;
        this.processingStatus = IntegrationTestConstants.VALID_PROCESSING_STATUS;
        this.createdAt = IntegrationTestConstants.VALID_CREATED_AT;
    }

    public static TransactionEntityTestBuilder builder() {
        return new TransactionEntityTestBuilder();
    }

    public TransactionEntityTestBuilder transactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public TransactionEntityTestBuilder accountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public TransactionEntityTestBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionEntityTestBuilder currency(String currency) {
        this.currency = currency;
        return this;
    }

    public TransactionEntityTestBuilder source(String source) {
        this.source = source;
        return this;
    }

    public TransactionEntityTestBuilder timestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public TransactionEntityTestBuilder processingStatus(String processingStatus) {
        this.processingStatus = processingStatus;
        return this;
    }

    public TransactionEntityTestBuilder createdAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public TransactionEntity build() {
        return new TransactionEntity(
                transactionId,
                accountId,
                amount,
                currency,
                source,
                timestamp,
                processingStatus,
                createdAt
        );
    }

    public TransactionEntity buildValidEntity() {
        return build();
    }

    public List<TransactionEntity> buildBatchEntities(int count) {
        List<TransactionEntity> entities = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            entities.add(new TransactionEntityTestBuilder()
                    .transactionId(UUID.randomUUID().toString())
                    .accountId(IntegrationTestConstants.VALID_ACCOUNT_ID)
                    .amount(IntegrationTestConstants.VALID_AMOUNT)
                    .currency(IntegrationTestConstants.VALID_CURRENCY)
                    .source(IntegrationTestConstants.VALID_SOURCE)
                    .timestamp(IntegrationTestConstants.VALID_TIMESTAMP)
                    .processingStatus(IntegrationTestConstants.VALID_PROCESSING_STATUS)
                    .createdAt(IntegrationTestConstants.VALID_CREATED_AT)
                    .build());
        }
        return entities;
    }

    public TransactionEntity buildInvalidEntity() {
        return new TransactionEntity(
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
