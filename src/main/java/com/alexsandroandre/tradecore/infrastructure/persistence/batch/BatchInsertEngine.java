package com.alexsandroandre.tradecore.infrastructure.persistence.batch;

import com.alexsandroandre.tradecore.infrastructure.persistence.batch.exception.BatchTransactionException;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import com.alexsandroandre.tradecore.infrastructure.persistence.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class BatchInsertEngine {
    private static final Logger logger = LoggerFactory.getLogger(BatchInsertEngine.class);

    private final TransactionRepository transactionRepository;
    private final EntityManager entityManager;
    private final BatchInsertConfiguration configuration;

    public BatchInsertEngine(
        TransactionRepository transactionRepository,
        EntityManager entityManager,
        BatchInsertConfiguration configuration
    ) {
        this.transactionRepository = transactionRepository;
        this.entityManager = entityManager;
        this.configuration = configuration;
    }

    public BatchInsertResult insertBatch(List<TransactionEntity> transactions) {
        return insertBatch(transactions, configuration);
    }

    public BatchInsertResult insertBatch(
        List<TransactionEntity> transactions,
        BatchInsertConfiguration config
    ) {
        long startTime = System.currentTimeMillis();
        int totalRecords = transactions.size();
        int successfulRecords = 0;
        int failedRecords = 0;
        int duplicateRecords = 0;

        List<String> errors = new ArrayList<>();
        List<String> duplicateTransactionIds = new ArrayList<>();
        boolean success = true;

        try {
            successfulRecords = processBatches(
                transactions,
                config,
                errors,
                duplicateTransactionIds
            );
            duplicateRecords = duplicateTransactionIds.size();
            failedRecords = totalRecords - successfulRecords - duplicateRecords;

        } catch (Exception e) {
            logger.error("Batch insert operation failed", e);
            errors.add(e.getMessage());
            success = false;
            failedRecords = totalRecords - successfulRecords - duplicateRecords;
        }

        long executionTime = System.currentTimeMillis() - startTime;

        BatchInsertResult.Builder builder = BatchInsertResult.builder()
            .totalRecords(totalRecords)
            .successfulRecords(successfulRecords)
            .failedRecords(failedRecords)
            .duplicateRecords(duplicateRecords)
            .executionTimeMs(executionTime)
            .success(success);

        for (String duplicateId : duplicateTransactionIds) {
            builder.addDuplicateTransactionId(duplicateId);
        }

        for (String error : errors) {
            builder.addError(error);
        }

        return builder.build();
    }

    private int processBatches(
        List<TransactionEntity> transactions,
        BatchInsertConfiguration config,
        List<String> errors,
        List<String> duplicateTransactionIds
    ) {
        int successCount = 0;
        int batchNumber = 0;

        for (int i = 0; i < transactions.size(); i += config.batchSize()) {
            batchNumber++;
            int endIndex = Math.min(i + config.batchSize(), transactions.size());
            List<TransactionEntity> batch = transactions.subList(i, endIndex);

            try {
                successCount += processSingleBatch(batch, batchNumber, config, duplicateTransactionIds);
            } catch (BatchTransactionException e) {
                logger.error("Batch {} transaction failed", batchNumber, e);
                errors.add("Batch " + batchNumber + ": " + e.getMessage());
            }
        }

        return successCount;
    }

    @Transactional
    protected int processSingleBatch(
        List<TransactionEntity> batch,
        int batchNumber,
        BatchInsertConfiguration config,
        List<String> duplicateTransactionIds
    ) {
        int successCount = 0;

        try {
            for (TransactionEntity entity : batch) {
                if (isDuplicate(entity.getTransactionId())) {
                    duplicateTransactionIds.add(entity.getTransactionId());
                    logger.warn("Duplicate transaction detected: {}", entity.getTransactionId());
                } else {
                    entityManager.persist(entity);
                    successCount++;
                }
            }

            if (config.flushAfterBatch()) {
                flush(config);
            }

            return successCount;

        } catch (Exception e) {
            throw new BatchTransactionException(
                "BATCH_TRANSACTION_FAILURE",
                batchNumber,
                "Failed to process batch " + batchNumber + ": " + e.getMessage(),
                e
            );
        }
    }

    private void flush(BatchInsertConfiguration config) {
        try {
            entityManager.flush();

            if (config.clearContextAfterFlush()) {
                entityManager.clear();
            }
        } catch (Exception e) {
            throw new BatchTransactionException(
                "PERSISTENCE_CONTEXT_OVERFLOW",
                0,
                "Failed to flush persistence context: " + e.getMessage(),
                e
            );
        }
    }

    private boolean isDuplicate(String transactionId) {
        return transactionRepository.existsByTransactionId(transactionId);
    }
}
