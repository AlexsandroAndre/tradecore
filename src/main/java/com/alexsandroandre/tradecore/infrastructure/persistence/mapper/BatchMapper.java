package com.alexsandroandre.tradecore.infrastructure.persistence.mapper;

import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import java.util.List;

public interface BatchMapper {
    List<TransactionEntity> toEntityList(Batch batch);
    
    Batch toDomain(List<TransactionEntity> entities, String batchId, int batchSize);
}