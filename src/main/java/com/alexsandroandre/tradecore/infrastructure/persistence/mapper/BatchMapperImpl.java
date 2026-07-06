package com.alexsandroandre.tradecore.infrastructure.persistence.mapper;

import com.alexsandroandre.tradecore.domain.exception.DomainValidationException;
import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import java.util.List;

public class BatchMapperImpl implements BatchMapper {

    private final TransactionMapper transactionMapper;

    private static final String INVALID_BATCH_MAPPING = "INVALID_BATCH_MAPPING";
    private static final String NULL_MAPPING = "NULL_MAPPING";

    public BatchMapperImpl(TransactionMapper transactionMapper) {
        this.transactionMapper = transactionMapper;
    }

    @Override
    public List<TransactionEntity> toEntityList(Batch batch) {
        if (batch == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "Batch domain object cannot be null",
                "toEntityList"
            );
        }

        try {
            return transactionMapper.toEntityList(batch.transactions());
        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_BATCH_MAPPING,
                "Failed to map Batch domain to entity list: " + e.getMessage(),
                "toEntityList",
                e
            );
        }
    }

    @Override
    public Batch toDomain(List<TransactionEntity> entities, String batchId, int batchSize) {
        if (entities == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "TransactionEntity list cannot be null",
                "toDomain"
            );
        }

        if (batchId == null || batchId.isEmpty()) {
            throw new DomainValidationException(
                INVALID_BATCH_MAPPING,
                "Batch ID cannot be null or empty",
                "toDomain"
            );
        }

        if (batchSize <= 0) {
            throw new DomainValidationException(
                INVALID_BATCH_MAPPING,
                "Batch size must be greater than zero",
                "toDomain"
            );
        }

        try {
            var transactions = transactionMapper.toDomainList(entities);
            return new Batch(batchId, transactions, batchSize);
        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_BATCH_MAPPING,
                "Failed to map entity list to Batch domain: " + e.getMessage(),
                "toDomain",
                e
            );
        }
    }
}