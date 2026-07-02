package com.alexsandroandre.tradecore.application.dto;

public record ProcessingReport(
    long totalRecords,
    long successfulRecords,
    long rejectedRecords,
    long failedRecords,
    long executionTimeMillis
) {
    public static ProcessingReport empty() {
        return new ProcessingReport(0, 0, 0, 0, 0);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private long totalRecords;
        private long successfulRecords;
        private long rejectedRecords;
        private long failedRecords;
        private long executionTimeMillis;

        public Builder totalRecords(long totalRecords) {
            this.totalRecords = totalRecords;
            return this;
        }

        public Builder successfulRecords(long successfulRecords) {
            this.successfulRecords = successfulRecords;
            return this;
        }

        public Builder rejectedRecords(long rejectedRecords) {
            this.rejectedRecords = rejectedRecords;
            return this;
        }

        public Builder failedRecords(long failedRecords) {
            this.failedRecords = failedRecords;
            return this;
        }

        public Builder executionTimeMillis(long executionTimeMillis) {
            this.executionTimeMillis = executionTimeMillis;
            return this;
        }

        public ProcessingReport build() {
            return new ProcessingReport(
                totalRecords,
                successfulRecords,
                rejectedRecords,
                failedRecords,
                executionTimeMillis
            );
        }
    }
}