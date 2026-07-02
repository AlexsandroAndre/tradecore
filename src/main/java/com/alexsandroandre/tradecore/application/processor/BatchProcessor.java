package com.alexsandroandre.tradecore.application.processor;

import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public interface BatchProcessor {
    List<Batch> groupIntoBatches(List<Transaction> transactions);

    BatchProcessingResult executeBatch(Batch batch);

    List<BatchProcessingResult> executeBatches(List<Batch> batches);

    void processStreamInBatches(Stream<Transaction> transactionStream, Consumer<BatchProcessingResult> resultConsumer);
}