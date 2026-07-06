package com.alexsandroandre.tradecore.application.dto;

public record ProcessingReport(
    String executionId,
    long startTime,
    long endTime,
    long totalRecords,
    long successfulRecords,
    long rejectedRecords,
    long failedRecords,
    long persistedRecords,
    long throughput
) {
    public long duration() {
        return endTime - startTime;
    }

    public double validationSuccessRate() {
        long totalReceived = totalRecords;
        if (totalReceived == 0) {
            return 0.0;
        }
        return (double) (successfulRecords) / totalReceived;
    }

    public double validationFailureRate() {
        long totalReceived = totalRecords;
        if (totalReceived == 0) {
            return 0.0;
        }
        return (double) rejectedRecords / totalReceived;
    }

    public double persistenceSuccessRate() {
        if (successfulRecords == 0) {
            return 0.0;
        }
        return (double) persistedRecords / successfulRecords;
    }

    public double averageProcessingTimePerRecord() {
        long duration = duration();
        if (totalRecords == 0) {
            return 0.0;
        }
        return (double) duration / totalRecords;
    }

    public static ProcessingReport empty(String executionId) {
        long currentTime = System.currentTimeMillis();
        return new ProcessingReport(executionId, currentTime, currentTime, 0, 0, 0, 0, 0, 0);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String executionId;
        private long startTime;
        private long endTime;
        private long totalRecords;
        private long successfulRecords;
        private long rejectedRecords;
        private long failedRecords;
        private long persistedRecords;
        private long throughput;

        public Builder executionId(String executionId) {
            this.executionId = executionId;
            return this;
        }

        public Builder startTime(long startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(long endTime) {
            this.endTime = endTime;
            return this;
        }

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

        public Builder persistedRecords(long persistedRecords) {
            this.persistedRecords = persistedRecords;
            return this;
        }

        public Builder throughput(long throughput) {
            this.throughput = throughput;
            return this;
        }

        public ProcessingReport build() {
            return new ProcessingReport(
                executionId,
                startTime,
                endTime,
                totalRecords,
                successfulRecords,
                rejectedRecords,
                failedRecords,
                persistedRecords,
                throughput
            );
        }
    }
}