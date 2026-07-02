package com.alexsandroandre.tradecore.application.processor;

import com.alexsandroandre.tradecore.application.port.TransactionBatchPersistencePort;
import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class StandardBatchProcessor implements BatchProcessor {

    private static final int DEFAULT_BATCH_SIZE = 1000;
    public static final String BATCH_SIZE_MUST_BE_GREATER_THAN_ZERO = "Batch size must be greater than zero";
    public static final String VALIDATION_FAILURE = "VALIDATION_FAILURE";
    public static final String TRANSACTION_VALIDATION_FAILED = "Transaction validation failed";
    public static final String PROCESSING_ERROR = "PROCESSING_ERROR";
    public static final String DUPLICATED_TRANSACTION_IN_BATCH = "DUPLICATED_TRANSACTION_IN_BATCH";
    public static final String DUPLICATE_TRANSACTION_IDS_DETECTED_WITHIN_BATCH = "Duplicate transaction IDs detected within batch";

    private final int batchSize;
    private final TransactionBatchPersistencePort persistencePort;
    private final TransactionProcessor transactionProcessor;

    public StandardBatchProcessor(
        TransactionBatchPersistencePort persistencePort,
        TransactionProcessor transactionProcessor
    ) {
        this(DEFAULT_BATCH_SIZE, persistencePort, transactionProcessor);
    }

    public StandardBatchProcessor(
        int batchSize,
        TransactionBatchPersistencePort persistencePort,
        TransactionProcessor transactionProcessor
    ) {
        if (batchSize <= 0) {
            throw new IllegalArgumentException(BATCH_SIZE_MUST_BE_GREATER_THAN_ZERO);
        }
        this.batchSize = batchSize;
        this.persistencePort = persistencePort;
        this.transactionProcessor = transactionProcessor;
    }

    @Override
    public List<Batch> groupIntoBatches(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return new ArrayList<>();
        }

        List<Batch> batches = new ArrayList<>();
        List<Transaction> currentBatch = new ArrayList<>();

        for (Transaction transaction : transactions) {
            currentBatch.add(transaction);

            if (currentBatch.size() >= batchSize) {
                batches.add(createBatch(currentBatch));
                currentBatch = new ArrayList<>();
            }
        }

        if (!currentBatch.isEmpty()) {
            batches.add(createBatch(currentBatch));
        }

        return batches;
    }

    @Override
    public BatchProcessingResult executeBatch(Batch batch) {
        if (batch == null || batch.isEmpty()) {
            return createEmptyBatchResult();
        }

        long startTime = System.currentTimeMillis();

        if (batch.hasDuplicates()) {
            return createDuplicateDetectionError(batch, startTime);
        }

        return processBatchWithErrorIsolation(batch, startTime);
    }

    @Override
    public List<BatchProcessingResult> executeBatches(List<Batch> batches) {
        if (batches == null || batches.isEmpty()) {
            return new ArrayList<>();
        }

        List<BatchProcessingResult> results = new ArrayList<>();

        for (Batch batch : batches) {
            results.add(executeBatch(batch));
        }

        return results;
    }

    private BatchProcessingResult processBatchWithErrorIsolation(Batch batch, long startTime) {
        List<BatchProcessingResult.BatchProcessingError> errors = new ArrayList<>();
        List<Transaction> successfulTransactions = new ArrayList<>();
        int processedCount = 0;
        int rejectedCount = 0;
        int failedCount = 0;

        for (Transaction transaction : batch.getTransactions()) {
            try {
                TransactionProcessor.ProcessingResult result = transactionProcessor.process(transaction);

                if (result.success()) {
                    successfulTransactions.add(result.transaction());
                    processedCount++;
                } else {
                    recordProcessingError(errors, transaction, VALIDATION_FAILURE, TRANSACTION_VALIDATION_FAILED);
                    rejectedCount++;
                }
            } catch (Exception exception) {
                recordProcessingError(errors, transaction, PROCESSING_ERROR, exception.getMessage());
                failedCount++;
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;

        persistSuccessfulTransactions(successfulTransactions);

        return createBatchResult(batch.batchId(), processedCount, rejectedCount, failedCount, executionTime, errors);
    }

    private void persistSuccessfulTransactions(List<Transaction> transactions) {
        if (!transactions.isEmpty()) {
            try {
                Batch successBatch = new Batch(
                    UUID.randomUUID().toString(),
                    transactions,
                    batchSize
                );
                persistencePort.saveBatch(successBatch);
            } catch (Exception exception) {
            }
        }
    }

    private BatchProcessingResult createBatchResult(
        String batchId,
        int processedCount,
        int rejectedCount,
        int failedCount,
        long executionTime,
        List<BatchProcessingResult.BatchProcessingError> errors
    ) {
        if (rejectedCount == 0 && failedCount == 0) {
            return BatchProcessingResult.success(batchId, processedCount, executionTime);
        } else if (processedCount > 0) {
            return BatchProcessingResult.partialFailure(
                batchId,
                processedCount,
                rejectedCount,
                failedCount,
                executionTime,
                errors
            );
        } else {
            return BatchProcessingResult.failure(batchId, executionTime, errors);
        }
    }

    private void recordProcessingError(
        List<BatchProcessingResult.BatchProcessingError> errors,
        Transaction transaction,
        String errorCode,
        String errorMessage
    ) {
        errors.add(new BatchProcessingResult.BatchProcessingError(
            transaction.transactionId(),
            errorCode,
            errorMessage
        ));
    }

    private BatchProcessingResult createDuplicateDetectionError(Batch batch, long startTime) {
        long executionTime = System.currentTimeMillis() - startTime;
        List<BatchProcessingResult.BatchProcessingError> errors = new ArrayList<>();
        errors.add(new BatchProcessingResult.BatchProcessingError(
            batch.batchId(),
                DUPLICATED_TRANSACTION_IN_BATCH,
                DUPLICATE_TRANSACTION_IDS_DETECTED_WITHIN_BATCH
        ));
        return BatchProcessingResult.partialFailure(
            batch.batchId(),
            0,
            batch.size(),
            0,
            executionTime,
            errors
        );
    }

    private BatchProcessingResult createEmptyBatchResult() {
        return BatchProcessingResult.success(
            UUID.randomUUID().toString(),
            0,
            0L
        );
    }

    private Batch createBatch(List<Transaction> transactions) {
        return new Batch(
            UUID.randomUUID().toString(),
            new ArrayList<>(transactions),
            batchSize
        );
    }
}
