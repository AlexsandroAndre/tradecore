package com.alexsandroandre.tradecore.infrastructure.persistence.adapter;

import com.alexsandroandre.tradecore.application.port.ProcessingMetricsPort;
import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;
import com.alexsandroandre.tradecore.infrastructure.persistence.mapper.ProcessingMetricsMapper;
import com.alexsandroandre.tradecore.infrastructure.persistence.repository.ProcessingMetricsRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ProcessingMetricsRepositoryAdapter implements ProcessingMetricsPort {

    private final ProcessingMetricsRepository repository;
    private final ProcessingMetricsMapper mapper;

    public ProcessingMetricsRepositoryAdapter(
        ProcessingMetricsRepository repository,
        ProcessingMetricsMapper mapper
    ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void save(ProcessingMetrics metrics) {
        repository.save(mapper.toEntity(metrics));
    }

    @Override
    public Optional<ProcessingMetrics> findById(UUID id) {
        return repository.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ProcessingMetrics> findAll() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public List<ProcessingMetrics> findByDateRange(LocalDateTime startTime, LocalDateTime endTime) {
        return mapper.toDomainList(repository.findByDateRange(startTime, endTime));
    }
}