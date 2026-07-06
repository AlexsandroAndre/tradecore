package com.alexsandroandre.tradecore.infrastructure.persistence.mapper;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import java.util.List;

public interface TransactionMapper {
    TransactionEntity toEntity(Transaction domain);
    
    Transaction toDomain(TransactionEntity entity);
    
    List<TransactionEntity> toEntityList(List<Transaction> domains);
    
    List<Transaction> toDomainList(List<TransactionEntity> entities);
}