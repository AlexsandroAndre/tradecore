package com.alexsandroandre.tradecore.infrastructure.persistence.batch;

import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BatchInsertService {
    private static final Logger logger = LoggerFactory.getLogger(BatchInsertService.class);

    private final BatchInsertEngine batchInsertEngine;
    private final BatchInsertConfiguration defaultConfiguration;

    public BatchInsertService(BatchInsertEngine batchInsertEngine) {
        this.batchInsertEngine = batchInsertEngine;
        this.defaultConfiguration = BatchInsertConfiguration.withDefaults();
    }

    public BatchInsertResult persistTransactions(List<TransactionEntity> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            logger.warn("Empty transaction list provided for batch insert");
            return createEmptyResult();
        }

        logger.info("Starting batch insert of {} transactions with batch size: {}",
            transactions.size(), defaultConfiguration.batchSize());

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions, defaultConfiguration);

        logResult(result);
        return result;
    }

    public BatchInsertResult persistTransactions(
        List<TransactionEntity> transactions,
        int customBatchSize
    ) {
        if (transactions == null || transactions.isEmpty()) {
            logger.warn("Empty transaction list provided for batch insert");
            return createEmptyResult();
        }

        BatchInsertConfiguration customConfig = BatchInsertConfiguration.custom(customBatchSize);

        logger.info("Starting batch insert of {} transactions with batch size: {}",
            transactions.size(), customBatchSize);

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions, customConfig);

        logResult(result);
        return result;
    }

    private BatchInsertResult createEmptyResult() {
        return BatchInsertResult.builder()
            .totalRecords(0)
            .successfulRecords(0)
            .failedRecords(0)
            .duplicateRecords(0)
            .executionTimeMs(0)
            .success(true)
            .build();
    }

    private void logResult(BatchInsertResult result) {
        logger.info(
            "Batch insert completed - Total: {}, Successful: {}, Failed: {}, Duplicates: {}, Time: {}ms, Throughput: {} records/sec",
            result.getTotalRecords(),
            result.getSuccessfulRecords(),
            result.getFailedRecords(),
            result.getDuplicateRecords(),
            result.getExecutionTimeMs(),
            String.format("%.2f", result.getThroughput())
        );

        if (!result.getErrors().isEmpty()) {
            logger.error("Batch insert errors: {}", result.getErrors());
        }

        if (!result.getDuplicateTransactionIds().isEmpty()) {
            logger.warn("Duplicate transactions detected: {}", result.getDuplicateTransactionIds());
        }
    }

    public BatchInsertConfiguration getDefaultConfiguration() {
        return defaultConfiguration;
    }
}
