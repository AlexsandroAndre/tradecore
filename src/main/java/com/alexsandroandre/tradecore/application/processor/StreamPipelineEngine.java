package com.alexsandroandre.tradecore.application.processor;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.application.port.TransactionPersistencePort;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import java.util.stream.Stream;

public final class StreamPipelineEngine {

    private final TransactionProcessor processor;
    private final TransactionPersistencePort persistencePort;

    public StreamPipelineEngine(
        TransactionProcessor processor,
        TransactionPersistencePort persistencePort
    ) {
        this.processor = processor;
        this.persistencePort = persistencePort;
    }

    public ProcessingReport execute(Stream<Transaction> transactionStream) {
        long startTime = System.currentTimeMillis();
        ProcessingReportBuilder reportBuilder = new ProcessingReportBuilder();

        transactionStream.forEach(transaction -> {
            reportBuilder.incrementTotal();
            processAndPersistRecord(transaction, reportBuilder);
        });

        long executionTime = System.currentTimeMillis() - startTime;
        return reportBuilder.executionTimeMillis(executionTime).build();
    }

    private void processAndPersistRecord(
        Transaction transaction,
        ProcessingReportBuilder reportBuilder
    ) {
        try {
            TransactionProcessor.ProcessingResult result = processor.process(transaction);

            if (result.success()) {
                persistRecord(result.transaction(), reportBuilder);
            } else {
                reportBuilder.incrementRejected();
            }
        } catch (Exception exception) {
            reportBuilder.incrementFailed();
        }
    }

    private void persistRecord(
        Transaction transaction,
        ProcessingReportBuilder reportBuilder
    ) {
        try {
            persistencePort.save(transaction);
            reportBuilder.incrementSuccessful();
        } catch (Exception exception) {
            reportBuilder.incrementFailed();
        }
    }

    private static final class ProcessingReportBuilder {
        private long totalRecords;
        private long successfulRecords;
        private long rejectedRecords;
        private long failedRecords;
        private long executionTimeMillis;

        void incrementTotal() {
            this.totalRecords++;
        }

        void incrementSuccessful() {
            this.successfulRecords++;
        }

        void incrementRejected() {
            this.rejectedRecords++;
        }

        void incrementFailed() {
            this.failedRecords++;
        }

        ProcessingReportBuilder executionTimeMillis(long executionTimeMillis) {
            this.executionTimeMillis = executionTimeMillis;
            return this;
        }

        ProcessingReport build() {
            return new ProcessingReport(
                totalRecords,
                successfulRecords,
                rejectedRecords,
                failedRecords,
                executionTimeMillis
            );
        }
    }
}
