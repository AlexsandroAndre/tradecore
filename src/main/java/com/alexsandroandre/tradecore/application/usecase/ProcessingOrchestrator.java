package com.alexsandroandre.tradecore.application.usecase;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.application.port.TransactionPersistencePort;
import com.alexsandroandre.tradecore.application.processor.RecordProcessor;
import com.alexsandroandre.tradecore.application.processor.StreamPipelineEngine;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationService;
import java.util.stream.Stream;

public final class ProcessingOrchestrator {

    private final DomainValidationService validationService;
    private final TransactionPersistencePort persistencePort;

    public ProcessingOrchestrator(
        DomainValidationService validationService,
        TransactionPersistencePort persistencePort
    ) {
        this.validationService = validationService;
        this.persistencePort = persistencePort;
    }

    public ProcessingReport orchestrate(Stream<Transaction> transactionStream) {
        RecordProcessor recordProcessor = new RecordProcessor(validationService);
        StreamPipelineEngine engine = new StreamPipelineEngine(
            recordProcessor,
            persistencePort
        );

        return engine.execute(transactionStream);
    }
}
