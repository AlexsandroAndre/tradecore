package com.alexsandroandre.tradecore.infrastructure.persistence.mapper;

import com.alexsandroandre.tradecore.domain.exception.DomainValidationException;
import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.ProcessingMetricsEntity;
import java.util.List;

public class ProcessingMetricsMapperImpl implements ProcessingMetricsMapper {

    private static final String INVALID_DOMAIN_MAPPING = "INVALID_DOMAIN_MAPPING";
    private static final String INVALID_ENTITY_MAPPING = "INVALID_ENTITY_MAPPING";
    private static final String NULL_MAPPING = "NULL_MAPPING";

    @Override
    public ProcessingMetricsEntity toEntity(ProcessingMetrics domain) {
        if (domain == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "ProcessingMetrics domain object cannot be null",
                "toEntity"
            );
        }

        try {
            return new ProcessingMetricsEntity(
                domain.id(),
                domain.startTime(),
                domain.endTime(),
                domain.totalRecordsProcessed(),
                domain.successfulRecords(),
                domain.failedRecords(),
                domain.duplicateRecords(),
                domain.totalDurationMillis(),
                domain.throughput(),
                domain.averageLatencyMillis(),
                domain.peakMemoryUsageBytes(),
                domain.averageMemoryUsageBytes(),
                domain.validationErrors(),
                domain.processingErrors(),
                domain.systemErrors(),
                domain.duplicateErrors(),
                domain.batchSize(),
                domain.batchCount(),
                domain.slowestBatchMillis(),
                domain.createdAt()
            );
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_DOMAIN_MAPPING,
                "Failed to map ProcessingMetrics domain to entity: " + e.getMessage(),
                "toEntity",
                e
            );
        }
    }

    @Override
    public ProcessingMetrics toDomain(ProcessingMetricsEntity entity) {
        if (entity == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "ProcessingMetrics entity cannot be null",
                "toDomain"
            );
        }

        try {
            return new ProcessingMetrics(
                entity.getId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getTotalRecordsProcessed(),
                entity.getSuccessfulRecords(),
                entity.getFailedRecords(),
                entity.getDuplicateRecords(),
                entity.getTotalDurationMillis(),
                entity.getThroughput(),
                entity.getAverageLatencyMillis(),
                entity.getPeakMemoryUsageBytes(),
                entity.getAverageMemoryUsageBytes(),
                entity.getValidationErrors(),
                entity.getProcessingErrors(),
                entity.getSystemErrors(),
                entity.getDuplicateErrors(),
                entity.getBatchSize(),
                entity.getBatchCount(),
                entity.getSlowestBatchMillis(),
                entity.getCreatedAt()
            );
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_ENTITY_MAPPING,
                "Failed to map ProcessingMetrics entity to domain: " + e.getMessage(),
                "toDomain",
                e
            );
        }
    }

    @Override
    public List<ProcessingMetricsEntity> toEntityList(List<ProcessingMetrics> domains) {
        if (domains == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "ProcessingMetrics domain list cannot be null",
                "toEntityList"
            );
        }

        try {
            return domains.stream()
                .map(this::toEntity)
                .toList();
        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_DOMAIN_MAPPING,
                "Failed to map ProcessingMetrics domain list to entities: " + e.getMessage(),
                "toEntityList",
                e
            );
        }
    }

    @Override
    public List<ProcessingMetrics> toDomainList(List<ProcessingMetricsEntity> entities) {
        if (entities == null) {
            throw new DomainValidationException(
                NULL_MAPPING,
                "ProcessingMetrics entity list cannot be null",
                "toDomainList"
            );
        }

        try {
            return entities.stream()
                .map(this::toDomain)
                .toList();
        } catch (DomainValidationException e) {
            throw e;
        } catch (Exception e) {
            throw new DomainValidationException(
                INVALID_ENTITY_MAPPING,
                "Failed to map ProcessingMetrics entity list to domains: " + e.getMessage(),
                "toDomainList",
                e
            );
        }
    }
}