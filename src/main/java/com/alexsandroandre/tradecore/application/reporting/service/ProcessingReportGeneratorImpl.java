package com.alexsandroandre.tradecore.application.reporting.service;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.application.port.ProcessingReportGenerator;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;
import org.springframework.stereotype.Service;

@Service
public class ProcessingReportGeneratorImpl implements ProcessingReportGenerator {

    private final String executionId;
    private final long startTime;

    private long totalRecords = 0;
    private long successfulRecords = 0;
    private long rejectedRecords = 0;
    private long failedRecords = 0;
    private long persistedRecords = 0;

    public ProcessingReportGeneratorImpl() {
        this.executionId = generateExecutionId();
        this.startTime = System.currentTimeMillis();
    }

    @Override
    public void aggregateBatchResult(BatchProcessingResult result) {
        if (result == null) {
            throw new IllegalArgumentException("BatchProcessingResult cannot be null");
        }

        int batchTotalRecords = result.totalProcessed();
        totalRecords += batchTotalRecords;

        if (result.status() == BatchProcessingResult.BatchStatus.SUCCESS) {
            successfulRecords += result.processedCount();
            persistedRecords += result.processedCount();
        } else if (result.status() == BatchProcessingResult.BatchStatus.PARTIAL_FAILURE) {
            successfulRecords += result.processedCount();
            persistedRecords += result.processedCount();
            rejectedRecords += result.rejectedCount();
            failedRecords += result.failedCount();
        } else if (result.status() == BatchProcessingResult.BatchStatus.FAILURE) {
            failedRecords += batchTotalRecords;
        }
    }

    @Override
    public ProcessingReport generateReport() {
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        long throughput = calculateThroughput(duration);

        return ProcessingReport.builder()
            .executionId(executionId)
            .startTime(startTime)
            .endTime(endTime)
            .totalRecords(totalRecords)
            .successfulRecords(successfulRecords)
            .rejectedRecords(rejectedRecords)
            .failedRecords(failedRecords)
            .persistedRecords(persistedRecords)
            .throughput(throughput)
            .build();
    }

    private long calculateThroughput(long duration) {
        if (duration == 0) {
            return 0;
        }
        return (totalRecords * 1000) / duration;
    }

    private String generateExecutionId() {
        return "exec-" + System.currentTimeMillis() + "-" + System.nanoTime();
    }
}
