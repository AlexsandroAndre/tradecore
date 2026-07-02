package com.alexsandroandre.tradecore.domain.model;

import java.util.Collections;
import java.util.List;

public record BatchProcessingResult(
    String batchId,
    BatchStatus status,
    int processedCount,
    int rejectedCount,
    int failedCount,
    long executionTimeMillis,
    List<BatchProcessingError> errors
) {

    public static final String STATUS_CANNOT_BE_NULL = "Status cannot be null";
    public static final String ERRORS_CANNOT_BE_NULL = "Errors cannot be null";

    public enum BatchStatus {
        SUCCESS,
        PARTIAL_FAILURE,
        FAILURE
    }

    public BatchProcessingResult {
        if (status == null) {
            throw new IllegalArgumentException(STATUS_CANNOT_BE_NULL);
        }
        if (errors == null) {
            throw new IllegalArgumentException(ERRORS_CANNOT_BE_NULL);
        }
    }

    public int totalProcessed() {
        return processedCount + rejectedCount + failedCount;
    }

    public boolean isSuccess() {
        return status == BatchStatus.SUCCESS;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public List<BatchProcessingError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public record BatchProcessingError(
        String transactionId,
        String errorCode,
        String errorMessage
    ) {
    }

    public static BatchProcessingResult success(
        String batchId,
        int processedCount,
        long executionTimeMillis
    ) {
        return new BatchProcessingResult(
            batchId,
            BatchStatus.SUCCESS,
            processedCount,
            0,
            0,
            executionTimeMillis,
            Collections.emptyList()
        );
    }

    public static BatchProcessingResult partialFailure(
        String batchId,
        int processedCount,
        int rejectedCount,
        int failedCount,
        long executionTimeMillis,
        List<BatchProcessingError> errors
    ) {
        return new BatchProcessingResult(
            batchId,
            BatchStatus.PARTIAL_FAILURE,
            processedCount,
            rejectedCount,
            failedCount,
            executionTimeMillis,
            errors
        );
    }

    public static BatchProcessingResult failure(
        String batchId,
        long executionTimeMillis,
        List<BatchProcessingError> errors
    ) {
        return new BatchProcessingResult(
            batchId,
            BatchStatus.FAILURE,
            0,
            0,
            0,
            executionTimeMillis,
            errors
        );
    }
}
