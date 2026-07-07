package com.alexsandroandre.tradecore.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProcessingMetricsDto(
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
}