package com.alexsandroandre.tradecore.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessingMetrics(
    UUID id,
    LocalDateTime startTime,
    LocalDateTime endTime,
    long totalRecordsProcessed,
    long successfulRecords,
    long failedRecords,
    long duplicateRecords,
    long totalDurationMillis,
    BigDecimal throughput,
    BigDecimal averageLatencyMillis,
    long peakMemoryUsageBytes,
    long averageMemoryUsageBytes,
    long validationErrors,
    long processingErrors,
    long systemErrors,
    long duplicateErrors,
    int batchSize,
    long batchCount,
    long slowestBatchMillis,
    LocalDateTime createdAt
) {

    public static final String ID_CANNOT_BE_NULL = "ID cannot be null";
    public static final String START_TIME_CANNOT_BE_NULL = "Start time cannot be null";
    public static final String END_TIME_CANNOT_BE_NULL = "End time cannot be null";
    public static final String CREATED_AT_CANNOT_BE_NULL = "Created at cannot be null";
    public static final String TOTAL_RECORDS_MUST_BE_NON_NEGATIVE = "Total records must be non-negative";
    public static final String SUCCESSFUL_RECORDS_MUST_BE_NON_NEGATIVE = "Successful records must be non-negative";
    public static final String FAILED_RECORDS_MUST_BE_NON_NEGATIVE = "Failed records must be non-negative";
    public static final String DUPLICATE_RECORDS_MUST_BE_NON_NEGATIVE = "Duplicate records must be non-negative";
    public static final String TOTAL_DURATION_MUST_BE_NON_NEGATIVE = "Total duration must be non-negative";
    public static final String THROUGHPUT_MUST_BE_NON_NEGATIVE = "Throughput must be non-negative";
    public static final String AVERAGE_LATENCY_MUST_BE_NON_NEGATIVE = "Average latency must be non-negative";
    public static final String BATCH_SIZE_MUST_BE_POSITIVE = "Batch size must be positive";
    public static final String BATCH_COUNT_MUST_BE_NON_NEGATIVE = "Batch count must be non-negative";
    public static final String END_TIME_MUST_BE_AFTER_START_TIME = "End time must be after start time";

    public ProcessingMetrics {
        if (id == null) {
            throw new IllegalArgumentException(ID_CANNOT_BE_NULL);
        }
        if (startTime == null) {
            throw new IllegalArgumentException(START_TIME_CANNOT_BE_NULL);
        }
        if (endTime == null) {
            throw new IllegalArgumentException(END_TIME_CANNOT_BE_NULL);
        }
        if (createdAt == null) {
            throw new IllegalArgumentException(CREATED_AT_CANNOT_BE_NULL);
        }
        if (totalRecordsProcessed < 0) {
            throw new IllegalArgumentException(TOTAL_RECORDS_MUST_BE_NON_NEGATIVE);
        }
        if (successfulRecords < 0) {
            throw new IllegalArgumentException(SUCCESSFUL_RECORDS_MUST_BE_NON_NEGATIVE);
        }
        if (failedRecords < 0) {
            throw new IllegalArgumentException(FAILED_RECORDS_MUST_BE_NON_NEGATIVE);
        }
        if (duplicateRecords < 0) {
            throw new IllegalArgumentException(DUPLICATE_RECORDS_MUST_BE_NON_NEGATIVE);
        }
        if (totalDurationMillis < 0) {
            throw new IllegalArgumentException(TOTAL_DURATION_MUST_BE_NON_NEGATIVE);
        }
        if (throughput.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(THROUGHPUT_MUST_BE_NON_NEGATIVE);
        }
        if (averageLatencyMillis.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(AVERAGE_LATENCY_MUST_BE_NON_NEGATIVE);
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException(BATCH_SIZE_MUST_BE_POSITIVE);
        }
        if (batchCount < 0) {
            throw new IllegalArgumentException(BATCH_COUNT_MUST_BE_NON_NEGATIVE);
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException(END_TIME_MUST_BE_AFTER_START_TIME);
        }
    }

    public static ProcessingMetrics create(
        LocalDateTime startTime,
        LocalDateTime endTime,
        long totalRecordsProcessed,
        long successfulRecords,
        long failedRecords,
        long duplicateRecords,
        long validationErrors,
        long processingErrors,
        long systemErrors,
        long duplicateErrors,
        int batchSize,
        long batchCount,
        long slowestBatchMillis,
        long peakMemoryUsageBytes,
        long averageMemoryUsageBytes
    ) {
        long totalDurationMillis = calculateTotalDuration(startTime, endTime);
        BigDecimal throughput = calculateThroughput(totalRecordsProcessed, totalDurationMillis);
        BigDecimal averageLatency = calculateAverageLatency(totalDurationMillis, totalRecordsProcessed);

        return new ProcessingMetrics(
            UUID.randomUUID(),
            startTime,
            endTime,
            totalRecordsProcessed,
            successfulRecords,
            failedRecords,
            duplicateRecords,
            totalDurationMillis,
            throughput,
            averageLatency,
            peakMemoryUsageBytes,
            averageMemoryUsageBytes,
            validationErrors,
            processingErrors,
            systemErrors,
            duplicateErrors,
            batchSize,
            batchCount,
            slowestBatchMillis,
            LocalDateTime.now()
        );
    }

    private static long calculateTotalDuration(LocalDateTime startTime, LocalDateTime endTime) {
        return java.time.Duration.between(startTime, endTime).toMillis();
    }

    private static BigDecimal calculateThroughput(long totalRecords, long durationMillis) {
        if (durationMillis == 0 || totalRecords == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal duration = BigDecimal.valueOf(durationMillis);
        BigDecimal records = BigDecimal.valueOf(totalRecords);
        return records.divide(duration.divide(BigDecimal.valueOf(1000), 10, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal calculateAverageLatency(long totalDurationMillis, long totalRecords) {
        if (totalRecords == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal duration = BigDecimal.valueOf(totalDurationMillis);
        BigDecimal records = BigDecimal.valueOf(totalRecords);
        return duration.divide(records, 2, RoundingMode.HALF_UP);
    }

    public long getTotalErrors() {
        return validationErrors + processingErrors + systemErrors + duplicateErrors;
    }

    public long getTotalRejectedRecords() {
        return failedRecords + duplicateRecords;
    }

    public boolean hasErrors() {
        return getTotalErrors() > 0;
    }

    public BigDecimal getSuccessRate() {
        if (totalRecordsProcessed == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal successful = BigDecimal.valueOf(successfulRecords);
        BigDecimal total = BigDecimal.valueOf(totalRecordsProcessed);
        return successful.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }

    public BigDecimal getErrorRate() {
        if (totalRecordsProcessed == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal errors = BigDecimal.valueOf(getTotalRejectedRecords());
        BigDecimal total = BigDecimal.valueOf(totalRecordsProcessed);
        return errors.divide(total, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100));
    }
}