package com.alexsandroandre.tradecore.application.processor;

import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import java.util.List;

public interface BatchProcessor {
    List<Batch> groupIntoBatches(List<Transaction> transactions);

    BatchProcessingResult executeBatch(Batch batch);

    List<BatchProcessingResult> executeBatches(List<Batch> batches);
}
