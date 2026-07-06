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
        BatchResultAggregator aggregator = new BatchResultAggregator();

        batchProcessor.processStreamInBatches(transactionStream, aggregator::aggregateBatch);

        return aggregator.build();
    }

    public ProcessingReport executeFromList(List<Transaction> transactions) {
        long startTime = System.currentTimeMillis();

        List<Batch> batches = batchProcessor.groupIntoBatches(transactions);

        List<BatchProcessingResult> batchResults = batchProcessor.executeBatches(batches);

        long executionTime = System.currentTimeMillis() - startTime;

        return aggregateResults(transactions.size(), batchResults, executionTime);
    }

    private ProcessingReport aggregateResults(
        int totalRecords,
        List<BatchProcessingResult> batchResults,
        long executionTime
    ) {
        long successfulRecords = 0;
        long rejectedRecords = 0;
        long failedRecords = 0;
        long startTime = System.currentTimeMillis() - executionTime;

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

        long endTime = startTime + executionTime;

        return ProcessingReport.builder()
            .executionId("exec-" + System.currentTimeMillis())
            .startTime(startTime)
            .endTime(endTime)
            .totalRecords(totalRecords)
            .successfulRecords(successfulRecords)
            .rejectedRecords(rejectedRecords)
            .failedRecords(failedRecords)
            .persistedRecords(successfulRecords)
            .throughput((totalRecords * 1000) / (executionTime > 0 ? executionTime : 1))
            .build();
    }
}