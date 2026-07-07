package com.alexsandroandre.tradecore.interfaces.api.controller;

import com.alexsandroandre.tradecore.application.dto.ProcessingMetricsDto;
import com.alexsandroandre.tradecore.application.service.MetricsCollector;
import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/metrics")
public class MetricsController {

    private final MetricsCollector metricsCollector;

    public MetricsController(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }

    @GetMapping
    public ResponseEntity<List<ProcessingMetricsDto>> getAllMetrics() {
        List<ProcessingMetrics> metrics = metricsCollector.getAllMetrics();
        List<ProcessingMetricsDto> dtos = metrics.stream()
            .map(this::toDto)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProcessingMetricsDto> getMetricsById(@PathVariable UUID id) {
        Optional<ProcessingMetrics> metrics = metricsCollector.findMetricsById(id);
        return metrics
            .map(m -> ResponseEntity.ok(toDto(m)))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/latest")
    public ResponseEntity<ProcessingMetricsDto> getLatestMetrics() {
        List<ProcessingMetrics> allMetrics = metricsCollector.getAllMetrics();
        if (allMetrics.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        ProcessingMetrics latest = allMetrics.stream()
            .max((m1, m2) -> m1.createdAt().compareTo(m2.createdAt()))
            .orElse(null);
        return ResponseEntity.ok(toDto(latest));
    }

    @GetMapping("/range")
    public ResponseEntity<List<ProcessingMetricsDto>> getMetricsByDateRange(
        @RequestParam LocalDateTime start,
        @RequestParam LocalDateTime end
    ) {
        List<ProcessingMetrics> metrics = metricsCollector.getMetricsByDateRange(start, end);
        List<ProcessingMetricsDto> dtos = metrics.stream()
            .map(this::toDto)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    private ProcessingMetricsDto toDto(ProcessingMetrics metrics) {
        return new ProcessingMetricsDto(
            metrics.id(),
            metrics.startTime(),
            metrics.endTime(),
            metrics.totalRecordsProcessed(),
            metrics.successfulRecords(),
            metrics.failedRecords(),
            metrics.duplicateRecords(),
            metrics.totalDurationMillis(),
            metrics.throughput(),
            metrics.averageLatencyMillis(),
            metrics.peakMemoryUsageBytes(),
            metrics.averageMemoryUsageBytes(),
            metrics.validationErrors(),
            metrics.processingErrors(),
            metrics.systemErrors(),
            metrics.duplicateErrors(),
            metrics.batchSize(),
            metrics.batchCount(),
            metrics.slowestBatchMillis(),
            metrics.createdAt()
        );
    }
}