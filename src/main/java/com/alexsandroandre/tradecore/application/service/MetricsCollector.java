package com.alexsandroandre.tradecore.application.service;

import com.alexsandroandre.tradecore.application.port.ProcessingMetricsPort;
import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MetricsCollector {

    private final ProcessingMetricsPort metricsPort;

    public MetricsCollector(ProcessingMetricsPort metricsPort) {
        this.metricsPort = metricsPort;
    }

    public ProcessingMetrics collectMetrics(
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
        ProcessingMetrics metrics = ProcessingMetrics.create(
            startTime,
            endTime,
            totalRecordsProcessed,
            successfulRecords,
            failedRecords,
            duplicateRecords,
            validationErrors,
            processingErrors,
            systemErrors,
            duplicateErrors,
            batchSize,
            batchCount,
            slowestBatchMillis,
            peakMemoryUsageBytes,
            averageMemoryUsageBytes
        );

        metricsPort.save(metrics);
        return metrics;
    }

    public Optional<ProcessingMetrics> findMetricsById(UUID id) {
        return metricsPort.findById(id);
    }

    public List<ProcessingMetrics> getAllMetrics() {
        return metricsPort.findAll();
    }

    public List<ProcessingMetrics> getMetricsByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return metricsPort.findByDateRange(startTime, endTime);
    }
}