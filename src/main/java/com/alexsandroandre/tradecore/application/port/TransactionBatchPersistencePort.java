package com.alexsandroandre.tradecore.application.port;

import com.alexsandroandre.tradecore.domain.model.Batch;
import java.util.List;

public interface TransactionBatchPersistencePort {
    void saveBatch(Batch batch);

    void saveAll(List<Batch> batches);
}
