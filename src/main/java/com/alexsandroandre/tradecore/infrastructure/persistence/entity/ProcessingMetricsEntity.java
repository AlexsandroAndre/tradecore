package com.alexsandroandre.tradecore.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processing_metrics", indexes = {
    @Index(name = "idx_metrics_start_time", columnList = "start_time"),
    @Index(name = "idx_metrics_created_at", columnList = "created_at")
})
public class ProcessingMetricsEntity {

    @Id
    @Column(name = "id", columnDefinition = "UUID")
    private UUID id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "total_records_processed", nullable = false)
    private long totalRecordsProcessed;

    @Column(name = "successful_records", nullable = false)
    private long successfulRecords;

    @Column(name = "failed_records", nullable = false)
    private long failedRecords;

    @Column(name = "duplicate_records", nullable = false)
    private long duplicateRecords;

    @Column(name = "total_duration_millis", nullable = false)
    private long totalDurationMillis;

    @Column(name = "throughput", nullable = false, precision = 19, scale = 2)
    private BigDecimal throughput;

    @Column(name = "average_latency_millis", nullable = false, precision = 19, scale = 2)
    private BigDecimal averageLatencyMillis;

    @Column(name = "peak_memory_usage_bytes", nullable = false)
    private long peakMemoryUsageBytes;

    @Column(name = "average_memory_usage_bytes", nullable = false)
    private long averageMemoryUsageBytes;

    @Column(name = "validation_errors", nullable = false)
    private long validationErrors;

    @Column(name = "processing_errors", nullable = false)
    private long processingErrors;

    @Column(name = "system_errors", nullable = false)
    private long systemErrors;

    @Column(name = "duplicate_errors", nullable = false)
    private long duplicateErrors;

    @Column(name = "batch_size", nullable = false)
    private int batchSize;

    @Column(name = "batch_count", nullable = false)
    private long batchCount;

    @Column(name = "slowest_batch_millis", nullable = false)
    private long slowestBatchMillis;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public ProcessingMetricsEntity() {
    }

    public ProcessingMetricsEntity(
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
        this.id = id;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalRecordsProcessed = totalRecordsProcessed;
        this.successfulRecords = successfulRecords;
        this.failedRecords = failedRecords;
        this.duplicateRecords = duplicateRecords;
        this.totalDurationMillis = totalDurationMillis;
        this.throughput = throughput;
        this.averageLatencyMillis = averageLatencyMillis;
        this.peakMemoryUsageBytes = peakMemoryUsageBytes;
        this.averageMemoryUsageBytes = averageMemoryUsageBytes;
        this.validationErrors = validationErrors;
        this.processingErrors = processingErrors;
        this.systemErrors = systemErrors;
        this.duplicateErrors = duplicateErrors;
        this.batchSize = batchSize;
        this.batchCount = batchCount;
        this.slowestBatchMillis = slowestBatchMillis;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public long getTotalRecordsProcessed() {
        return totalRecordsProcessed;
    }

    public void setTotalRecordsProcessed(long totalRecordsProcessed) {
        this.totalRecordsProcessed = totalRecordsProcessed;
    }

    public long getSuccessfulRecords() {
        return successfulRecords;
    }

    public void setSuccessfulRecords(long successfulRecords) {
        this.successfulRecords = successfulRecords;
    }

    public long getFailedRecords() {
        return failedRecords;
    }

    public void setFailedRecords(long failedRecords) {
        this.failedRecords = failedRecords;
    }

    public long getDuplicateRecords() {
        return duplicateRecords;
    }

    public void setDuplicateRecords(long duplicateRecords) {
        this.duplicateRecords = duplicateRecords;
    }

    public long getTotalDurationMillis() {
        return totalDurationMillis;
    }

    public void setTotalDurationMillis(long totalDurationMillis) {
        this.totalDurationMillis = totalDurationMillis;
    }

    public BigDecimal getThroughput() {
        return throughput;
    }

    public void setThroughput(BigDecimal throughput) {
        this.throughput = throughput;
    }

    public BigDecimal getAverageLatencyMillis() {
        return averageLatencyMillis;
    }

    public void setAverageLatencyMillis(BigDecimal averageLatencyMillis) {
        this.averageLatencyMillis = averageLatencyMillis;
    }

    public long getPeakMemoryUsageBytes() {
        return peakMemoryUsageBytes;
    }

    public void setPeakMemoryUsageBytes(long peakMemoryUsageBytes) {
        this.peakMemoryUsageBytes = peakMemoryUsageBytes;
    }

    public long getAverageMemoryUsageBytes() {
        return averageMemoryUsageBytes;
    }

    public void setAverageMemoryUsageBytes(long averageMemoryUsageBytes) {
        this.averageMemoryUsageBytes = averageMemoryUsageBytes;
    }

    public long getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(long validationErrors) {
        this.validationErrors = validationErrors;
    }

    public long getProcessingErrors() {
        return processingErrors;
    }

    public void setProcessingErrors(long processingErrors) {
        this.processingErrors = processingErrors;
    }

    public long getSystemErrors() {
        return systemErrors;
    }

    public void setSystemErrors(long systemErrors) {
        this.systemErrors = systemErrors;
    }

    public long getDuplicateErrors() {
        return duplicateErrors;
    }

    public void setDuplicateErrors(long duplicateErrors) {
        this.duplicateErrors = duplicateErrors;
    }

    public int getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public long getBatchCount() {
        return batchCount;
    }

    public void setBatchCount(long batchCount) {
        this.batchCount = batchCount;
    }

    public long getSlowestBatchMillis() {
        return slowestBatchMillis;
    }

    public void setSlowestBatchMillis(long slowestBatchMillis) {
        this.slowestBatchMillis = slowestBatchMillis;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}