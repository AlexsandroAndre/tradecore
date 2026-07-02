package com.alexsandroandre.tradecore.application.usecase;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.application.port.TransactionBatchPersistencePort;
import com.alexsandroandre.tradecore.application.processor.RecordProcessor;
import com.alexsandroandre.tradecore.application.processor.StandardBatchProcessor;
import com.alexsandroandre.tradecore.application.processor.StreamPipelineEngine;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationService;
import java.util.stream.Stream;

public final class ProcessingOrchestrator {

    private final DomainValidationService validationService;
    private final TransactionBatchPersistencePort batchPersistencePort;

    public ProcessingOrchestrator(
        DomainValidationService validationService,
        TransactionBatchPersistencePort batchPersistencePort
    ) {
        this.validationService = validationService;
        this.batchPersistencePort = batchPersistencePort;
    }

    public ProcessingReport orchestrate(Stream<Transaction> transactionStream) {
        RecordProcessor recordProcessor = new RecordProcessor(validationService);
        StandardBatchProcessor batchProcessor = new StandardBatchProcessor(
            batchPersistencePort,
            recordProcessor
        );
        StreamPipelineEngine engine = new StreamPipelineEngine(batchProcessor);

        return engine.execute(transactionStream);
    }
}