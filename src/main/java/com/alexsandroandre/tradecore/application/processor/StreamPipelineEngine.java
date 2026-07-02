package com.alexsandroandre.tradecore.application.processor;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import java.util.List;
import java.util.stream.Stream;

public final class StreamPipelineEngine {

    public static final String DUPLICATED_TRANSACTION_IN_BATCH = "DUPLICATED_TRANSACTION_IN_BATCH";
    private final BatchProcessor batchProcessor;

    public StreamPipelineEngine(BatchProcessor batchProcessor) {
        this.batchProcessor = batchProcessor;
    }

    public ProcessingReport execute(Stream<Transaction> transactionStream) {
        long startTime = System.currentTimeMillis();

        List<Transaction> allTransactions = transactionStream.toList();

        List<Batch> batches = batchProcessor.groupIntoBatches(allTransactions);

        List<BatchProcessingResult> batchResults = batchProcessor.executeBatches(batches);

        long executionTime = System.currentTimeMillis() - startTime;

        return aggregateResults(allTransactions.size(), batchResults, executionTime);
    }

    private ProcessingReport aggregateResults(
        int totalRecords,
        List<BatchProcessingResult> batchResults,
        long executionTime
    ) {
        long successfulRecords = 0;
        long rejectedRecords = 0;
        long failedRecords = 0;

        for (BatchProcessingResult result : batchResults) {
            if (result.status() == BatchProcessingResult.BatchStatus.SUCCESS) {
                successfulRecords += result.processedCount();
            } else if (result.status() == BatchProcessingResult.BatchStatus.PARTIAL_FAILURE) {
                successfulRecords += result.processedCount();
                if (result.hasErrors() && DUPLICATED_TRANSACTION_IN_BATCH.equals(
                    result.getErrors().getFirst().errorCode())) {
                    rejectedRecords += result.rejectedCount();
                } else {
                    rejectedRecords += result.rejectedCount();
                    failedRecords += result.failedCount();
                }
            } else if (result.status() == BatchProcessingResult.BatchStatus.FAILURE) {
                if (result.hasErrors() && DUPLICATED_TRANSACTION_IN_BATCH.equals(
                    result.getErrors().getFirst().errorCode())) {
                    rejectedRecords += result.totalProcessed();
                } else {
                    failedRecords += result.totalProcessed();
                }
            }
        }

        return new ProcessingReport(
            totalRecords,
            successfulRecords,
            rejectedRecords,
            failedRecords,
            executionTime
        );
    }
}
