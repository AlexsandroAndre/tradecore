package com.alexsandroandre.tradecore.infrastructure.persistence.mapper;

import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.ProcessingMetricsEntity;
import java.util.List;

public interface ProcessingMetricsMapper {
    ProcessingMetricsEntity toEntity(ProcessingMetrics domain);
    
    ProcessingMetrics toDomain(ProcessingMetricsEntity entity);
    
    List<ProcessingMetricsEntity> toEntityList(List<ProcessingMetrics> domains);
    
    List<ProcessingMetrics> toDomainList(List<ProcessingMetricsEntity> entities);
}