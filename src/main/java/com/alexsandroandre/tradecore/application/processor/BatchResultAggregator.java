package com.alexsandroandre.tradecore.application.processor;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;

public final class BatchResultAggregator {

    private long totalRecords = 0;
    private long successfulRecords = 0;
    private long rejectedRecords = 0;
    private long failedRecords = 0;
    private long startTime;

    public BatchResultAggregator() {
        this.startTime = System.currentTimeMillis();
    }

    public void aggregateBatch(BatchProcessingResult result) {
        int batchTotalRecords = result.totalProcessed();
        totalRecords += batchTotalRecords;

        if (result.status() == BatchProcessingResult.BatchStatus.SUCCESS) {
            successfulRecords += result.processedCount();
        } else if (result.status() == BatchProcessingResult.BatchStatus.PARTIAL_FAILURE) {
            successfulRecords += result.processedCount();
            if (result.hasErrors() && StreamPipelineEngine.DUPLICATED_TRANSACTION_IN_BATCH.equals(
                result.getErrors().getFirst().errorCode())) {
                rejectedRecords += result.rejectedCount();
            } else {
                rejectedRecords += result.rejectedCount();
                failedRecords += result.failedCount();
            }
        } else if (result.status() == BatchProcessingResult.BatchStatus.FAILURE) {
            if (result.hasErrors() && StreamPipelineEngine.DUPLICATED_TRANSACTION_IN_BATCH.equals(
                result.getErrors().getFirst().errorCode())) {
                rejectedRecords += result.totalProcessed();
            } else {
                failedRecords += result.totalProcessed();
            }
        }
    }

    public ProcessingReport build() {
        long executionTime = System.currentTimeMillis() - startTime;
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
