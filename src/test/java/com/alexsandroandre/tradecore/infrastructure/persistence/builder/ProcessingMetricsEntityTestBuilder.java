package com.alexsandroandre.tradecore.infrastructure.persistence.builder;

import com.alexsandroandre.tradecore.infrastructure.persistence.entity.ProcessingMetricsEntity;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class ProcessingMetricsEntityTestBuilder {

    private UUID id = UUID.randomUUID();
    private LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
    private LocalDateTime endTime = LocalDateTime.of(2025, 1, 1, 10, 5, 0);
    private long totalRecordsProcessed = 1000L;
    private long successfulRecords = 950L;
    private long failedRecords = 30L;
    private long duplicateRecords = 20L;
    private long totalDurationMillis = 300000L;
    private BigDecimal throughput = BigDecimal.valueOf(3.33);
    private BigDecimal averageLatencyMillis = BigDecimal.valueOf(300.00);
    private long peakMemoryUsageBytes = 512000000L;
    private long averageMemoryUsageBytes = 400000000L;
    private long validationErrors = 10L;
    private long processingErrors = 15L;
    private long systemErrors = 5L;
    private long duplicateErrors = 20L;
    private int batchSize = 100;
    private long batchCount = 10L;
    private long slowestBatchMillis = 35000L;
    private LocalDateTime createdAt = LocalDateTime.of(2025, 1, 1, 10, 5, 0);

    public static ProcessingMetricsEntityTestBuilder builder() {
        return new ProcessingMetricsEntityTestBuilder();
    }

    public ProcessingMetricsEntityTestBuilder id(UUID id) {
        this.id = id;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder startTime(LocalDateTime startTime) {
        this.startTime = startTime;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder endTime(LocalDateTime endTime) {
        this.endTime = endTime;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder totalRecordsProcessed(long totalRecordsProcessed) {
        this.totalRecordsProcessed = totalRecordsProcessed;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder successfulRecords(long successfulRecords) {
        this.successfulRecords = successfulRecords;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder failedRecords(long failedRecords) {
        this.failedRecords = failedRecords;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder duplicateRecords(long duplicateRecords) {
        this.duplicateRecords = duplicateRecords;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder totalDurationMillis(long totalDurationMillis) {
        this.totalDurationMillis = totalDurationMillis;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder throughput(BigDecimal throughput) {
        this.throughput = throughput;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder averageLatencyMillis(BigDecimal averageLatencyMillis) {
        this.averageLatencyMillis = averageLatencyMillis;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder peakMemoryUsageBytes(long peakMemoryUsageBytes) {
        this.peakMemoryUsageBytes = peakMemoryUsageBytes;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder averageMemoryUsageBytes(long averageMemoryUsageBytes) {
        this.averageMemoryUsageBytes = averageMemoryUsageBytes;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder validationErrors(long validationErrors) {
        this.validationErrors = validationErrors;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder processingErrors(long processingErrors) {
        this.processingErrors = processingErrors;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder systemErrors(long systemErrors) {
        this.systemErrors = systemErrors;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder duplicateErrors(long duplicateErrors) {
        this.duplicateErrors = duplicateErrors;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder batchSize(int batchSize) {
        this.batchSize = batchSize;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder batchCount(long batchCount) {
        this.batchCount = batchCount;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder slowestBatchMillis(long slowestBatchMillis) {
        this.slowestBatchMillis = slowestBatchMillis;
        return this;
    }

    public ProcessingMetricsEntityTestBuilder createdAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ProcessingMetricsEntity build() {
        ProcessingMetricsEntity entity = new ProcessingMetricsEntity();
        entity.setId(id);
        entity.setStartTime(startTime);
        entity.setEndTime(endTime);
        entity.setTotalRecordsProcessed(totalRecordsProcessed);
        entity.setSuccessfulRecords(successfulRecords);
        entity.setFailedRecords(failedRecords);
        entity.setDuplicateRecords(duplicateRecords);
        entity.setTotalDurationMillis(totalDurationMillis);
        entity.setThroughput(throughput);
        entity.setAverageLatencyMillis(averageLatencyMillis);
        entity.setPeakMemoryUsageBytes(peakMemoryUsageBytes);
        entity.setAverageMemoryUsageBytes(averageMemoryUsageBytes);
        entity.setValidationErrors(validationErrors);
        entity.setProcessingErrors(processingErrors);
        entity.setSystemErrors(systemErrors);
        entity.setDuplicateErrors(duplicateErrors);
        entity.setBatchSize(batchSize);
        entity.setBatchCount(batchCount);
        entity.setSlowestBatchMillis(slowestBatchMillis);
        entity.setCreatedAt(createdAt);
        return entity;
    }

    public ProcessingMetricsEntity buildValidEntity() {
        return build();
    }

    public ProcessingMetricsEntity buildBatchEntities(int count) {
        return build();
    }
}