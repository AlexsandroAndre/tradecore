package com.alexsandroandre.tradecore.infrastructure.persistence.adapter;

import com.alexsandroandre.tradecore.application.port.TransactionBatchPersistencePort;
import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.infrastructure.persistence.batch.BatchInsertService;
import com.alexsandroandre.tradecore.infrastructure.persistence.mapper.BatchMapper;
import java.util.List;

public class TransactionBatchPersistenceAdapter implements TransactionBatchPersistencePort {

    private final BatchInsertService batchInsertService;
    private final BatchMapper batchMapper;

    public TransactionBatchPersistenceAdapter(
        BatchInsertService batchInsertService,
        BatchMapper batchMapper
    ) {
        this.batchInsertService = batchInsertService;
        this.batchMapper = batchMapper;
    }

    @Override
    public void saveBatch(Batch batch) {
        var transactionEntities = batchMapper.toEntityList(batch);
        batchInsertService.persistTransactions(transactionEntities);
    }

    @Override
    public void saveAll(List<Batch> batches) {
        for (Batch batch : batches) {
            saveBatch(batch);
        }
    }
}
