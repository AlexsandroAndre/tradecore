package com.alexsandroandre.tradecore.infrastructure.persistence.mapper;

import com.alexsandroandre.tradecore.domain.exception.DomainValidationException;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

public class TransactionMapperImpl implements TransactionMapper {

    private static final String INVALID_DOMAIN_MAPPING = "INVALID_DOMAIN_MAPPING";
    private static final String INVALID_ENTITY_MAPPING = "INVALID_ENTITY_MAPPING";
    private static final String NULL_MAPPING = "NULL_MAPPING";

    @Override
    public TransactionEntity toEntity(Transaction domain) {
        if (domain == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "Transaction domain object cannot be null",
                "toEntity"
            );
        }

        try {
            Instant transactionInstant = domain.timestamp() != null
                ? domain.timestamp().toInstant()
                : null;

            String processingStatus = domain.status() != null
                ? domain.status().name()
                : null;

            return new TransactionEntity(
                domain.transactionId(),
                domain.accountId(),
                domain.amount(),
                domain.currency(),
                domain.source(),
                transactionInstant,
                processingStatus,
                Instant.now()
            );
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_DOMAIN_MAPPING,
                "Failed to map Transaction domain to entity: " + e.getMessage(),
                "toEntity",
                e
            );
        }
    }

    @Override
    public Transaction toDomain(TransactionEntity entity) {
        if (entity == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "Transaction entity cannot be null",
                "toDomain"
            );
        }

        try {
            Transaction.TransactionStatus status = entity.getProcessingStatus() != null
                ? Transaction.TransactionStatus.valueOf(entity.getProcessingStatus())
                : Transaction.TransactionStatus.PENDING;

            java.time.OffsetDateTime transactionTimestamp = entity.getTimestamp() != null
                ? entity.getTimestamp().atZone(ZoneId.systemDefault()).toOffsetDateTime()
                : null;

            return new Transaction(
                entity.getTransactionId(),
                entity.getAccountId(),
                entity.getAmount(),
                entity.getCurrency(),
                transactionTimestamp,
                entity.getSource(),
                status
            );
        } catch (IllegalArgumentException e) {
            throw new DomainValidationException(
                INVALID_ENTITY_MAPPING,
                "Failed to map Transaction entity to domain - invalid status value: " + entity.getProcessingStatus(),
                "toDomain",
                e
            );
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_ENTITY_MAPPING,
                "Failed to map Transaction entity to domain: " + e.getMessage(),
                "toDomain",
                e
            );
        }
    }

    @Override
    public List<TransactionEntity> toEntityList(List<Transaction> domains) {
        if (domains == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "Transaction domain list cannot be null",
                "toEntityList"
            );
        }

        try {
            return domains.stream()
                .map(this::toEntity)
                .toList();
        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_DOMAIN_MAPPING,
                "Failed to map Transaction domain list to entities: " + e.getMessage(),
                "toEntityList",
                e
            );
        }
    }

    @Override
    public List<Transaction> toDomainList(List<TransactionEntity> entities) {
        if (entities == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "Transaction entity list cannot be null",
                "toDomainList"
            );
        }

        try {
            return entities.stream()
                .map(this::toDomain)
                .toList();
        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_ENTITY_MAPPING,
                "Failed to map Transaction entity list to domains: " + e.getMessage(),
                "toDomainList",
                e
            );
        }
    }
}