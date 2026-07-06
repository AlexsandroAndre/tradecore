package com.alexsandroandre.tradecore.infrastructure.persistence.batch;

import java.util.*;

public class BatchInsertResult {
    private final long totalRecords;
    private final long successfulRecords;
    private final long failedRecords;
    private final long duplicateRecords;
    private final long executionTimeMs;
    private final List<String> errors;
    private final List<String> duplicateTransactionIds;
    private final boolean success;

    public BatchInsertResult(
        long totalRecords,
        long successfulRecords,
        long failedRecords,
        long duplicateRecords,
        long executionTimeMs,
        List<String> errors,
        List<String> duplicateTransactionIds,
        boolean success
    ) {
        this.totalRecords = totalRecords;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.duplicateRecords = duplicateRecords;
        this.executionTimeMs = executionTimeMs;
        this.errors = new ArrayList<>(errors);
        this.duplicateTransactionIds = new ArrayList<>(duplicateTransactionIds);
        this.success = success;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getTotalRecords() {
        return totalRecords;
    }

    public long getSuccessfulRecords() {
        return successfulRecords;
    }

    public long getFailedRecords() {
        return failedRecords;
    }

    public long getDuplicateRecords() {
        return duplicateRecords;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> getDuplicateTransactionIds() {
        return Collections.unmodifiableList(duplicateTransactionIds);
    }

    public boolean isSuccess() {
        return success;
    }

    public double getThroughput() {
        if (executionTimeMs == 0) return 0;
        return (successfulRecords * 1000.0) / executionTimeMs;
    }

    public static class Builder {
        private long totalRecords;
        private long successfulRecords;
        private long failedRecords;
        private long duplicateRecords;
        private long executionTimeMs;
        private final List<String> errors = new ArrayList<>();
        private final List<String> duplicateTransactionIds = new ArrayList<>();
        private boolean success = true;

        public Builder totalRecords(long totalRecords) {
            this.totalRecords = totalRecords;
            return this;
        }

        public Builder successfulRecords(long successfulRecords) {
            this.successfulRecords = successfulRecords;
            return this;
        }

        public Builder failedRecords(long failedRecords) {
            this.failedRecords = failedRecords;
            return this;
        }

        public Builder duplicateRecords(long duplicateRecords) {
            this.duplicateRecords = duplicateRecords;
            return this;
        }

        public Builder executionTimeMs(long executionTimeMs) {
            this.executionTimeMs = executionTimeMs;
            return this;
        }

        public Builder addError(String error) {
            this.errors.add(error);
            return this;
        }

        public Builder addDuplicateTransactionId(String transactionId) {
            this.duplicateTransactionIds.add(transactionId);
            return this;
        }

        public Builder success(boolean success) {
            this.success = success;
            return this;
        }

        public BatchInsertResult build() {
            return new BatchInsertResult(
                totalRecords,
                successfulRecords,
                failedRecords,
                duplicateRecords,
                executionTimeMs,
                errors,
                duplicateTransactionIds,
                success
            );
        }
    }
}
