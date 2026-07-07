package com.alexsandroandre.tradecore.application.port;

import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProcessingMetricsPort {
    void save(ProcessingMetrics metrics);
    Optional<ProcessingMetrics> findById(UUID id);
    List<ProcessingMetrics> findAll();
    List<ProcessingMetrics> findByDateRange(LocalDateTime startTime, LocalDateTime endTime);
}