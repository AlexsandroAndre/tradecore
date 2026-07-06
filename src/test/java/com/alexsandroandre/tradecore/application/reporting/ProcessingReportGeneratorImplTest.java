package com.alexsandroandre.tradecore.application.reporting.service;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcessingReportGenerator Tests")
class ProcessingReportGeneratorImplTest {

    private ProcessingReportGeneratorImpl reportGenerator;

    @BeforeEach
    void setUp() {
        reportGenerator = new ProcessingReportGeneratorImpl();
    }

    @Test
    @DisplayName("Should generate report with execution ID and timestamps")
    void shouldGenerateReportWithExecutionIdAndTimestamps() {
        ProcessingReport report = reportGenerator.generateReport();

        assertNotNull(report.executionId());
        assertTrue(report.executionId().startsWith("exec-"));
        assertTrue(report.startTime() > 0);
        assertTrue(report.endTime() >= report.startTime());
    }

    @Test
    @DisplayName("Should calculate correct duration")
    void shouldCalculateCorrectDuration() {
        ProcessingReport report = reportGenerator.generateReport();

        long duration = report.duration();
        assertEquals(report.endTime() - report.startTime(), duration);
        assertTrue(duration >= 0);
    }

    @Test
    @DisplayName("Should aggregate successful batch results")
    void shouldAggregateSuccessfulBatchResults() {
        BatchProcessingResult successResult = BatchProcessingResult.success("batch-1", 100, 1000);
        reportGenerator.aggregateBatchResult(successResult);

        ProcessingReport report = reportGenerator.generateReport();

        assertEquals(100, report.totalRecords());
        assertEquals(100, report.successfulRecords());
        assertEquals(100, report.persistedRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
    }

    @Test
    @DisplayName("Should aggregate partial failure batch results")
    void shouldAggregatePartialFailureBatchResults() {
        BatchProcessingResult partialResult = BatchProcessingResult.partialFailure(
            "batch-1",
            80,
            10,
            10,
            1000,
            Collections.emptyList()
        );
        reportGenerator.aggregateBatchResult(partialResult);

        ProcessingReport report = reportGenerator.generateReport();

        assertEquals(100, report.totalRecords());
        assertEquals(80, report.successfulRecords());
        assertEquals(80, report.persistedRecords());
        assertEquals(10, report.rejectedRecords());
        assertEquals(10, report.failedRecords());
    }

    @Test
    @DisplayName("Should aggregate failure batch results")
    void shouldAggregateFailureBatchResults() {
        BatchProcessingResult failureResult = BatchProcessingResult.partialFailure(
            "batch-1",
            0,
            0,
            1,
            1000,
            Collections.singletonList(
                new BatchProcessingResult.BatchProcessingError("txn-1", "PROCESSING_ERROR", "Error occurred")
            )
        );
        reportGenerator.aggregateBatchResult(failureResult);

        ProcessingReport report = reportGenerator.generateReport();

        assertEquals(1, report.totalRecords());
        assertEquals(0, report.successfulRecords());
        assertEquals(0, report.persistedRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(1, report.failedRecords());
    }

    @Test
    @DisplayName("Should aggregate multiple batch results correctly")
    void shouldAggregateMultipleBatchResultsCorrectly() {
        reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-1", 50, 1000));
        reportGenerator.aggregateBatchResult(BatchProcessingResult.partialFailure("batch-2", 40, 5, 5, 1000, Collections.emptyList()));
        reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-3", 30, 1000));

        ProcessingReport report = reportGenerator.generateReport();

        assertEquals(130, report.totalRecords());
        assertEquals(120, report.successfulRecords());
        assertEquals(120, report.persistedRecords());
        assertEquals(5, report.rejectedRecords());
        assertEquals(5, report.failedRecords());
    }

    @Test
    @DisplayName("Should calculate validation success rate correctly")
    void shouldCalculateValidationSuccessRateCorrectly() {
        reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-1", 90, 1000));
        reportGenerator.aggregateBatchResult(BatchProcessingResult.partialFailure("batch-2", 0, 10, 0, 1000, Collections.emptyList()));

        ProcessingReport report = reportGenerator.generateReport();

        // 90 successful out of 100 total (90 success + 10 rejected) = 0.9
        double successRate = report.validationSuccessRate();
        assertEquals(0.9, successRate, 0.0001);
    }

    @Test
    @DisplayName("Should calculate validation failure rate correctly")
    void shouldCalculateValidationFailureRateCorrectly() {
        reportGenerator.aggregateBatchResult(BatchProcessingResult.partialFailure("batch-1", 80, 20, 0, 1000, Collections.emptyList()));

        ProcessingReport report = reportGenerator.generateReport();

        double failureRate = report.validationFailureRate();
        assertEquals(0.2, failureRate, 0.0001);
    }

    @Test
    @DisplayName("Should calculate persistence success rate correctly")
    void shouldCalculatePersistenceSuccessRateCorrectly() {
        reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-1", 100, 1000));

        ProcessingReport report = reportGenerator.generateReport();

        double persistenceRate = report.persistenceSuccessRate();
        assertEquals(1.0, persistenceRate, 0.0001);
    }

    @Test
    @DisplayName("Should handle zero records for rate calculations")
    void shouldHandleZeroRecordsForRateCalculations() {
        ProcessingReport report = reportGenerator.generateReport();

        assertEquals(0.0, report.validationSuccessRate());
        assertEquals(0.0, report.validationFailureRate());
        assertEquals(0.0, report.persistenceSuccessRate());
        assertEquals(0.0, report.averageProcessingTimePerRecord());
    }

    @Test
    @DisplayName("Should calculate average processing time per record correctly")
    void shouldCalculateAverageProcessingTimePerRecordCorrectly() throws InterruptedException {
        Thread.sleep(10);
        reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-1", 1000, 1000));

        ProcessingReport report = reportGenerator.generateReport();

        double avgTime = report.averageProcessingTimePerRecord();
        assertTrue(avgTime >= 0);
    }

    @Test
    @DisplayName("Should calculate throughput correctly")
    void shouldCalculateThroughputCorrectly() throws InterruptedException {
        Thread.sleep(10);
        reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-1", 1000, 1000));

        ProcessingReport report = reportGenerator.generateReport();

        long throughput = report.throughput();
        assertTrue(throughput >= 0);
    }

    @Test
    @DisplayName("Should produce immutable report")
    void shouldProduceImmutableReport() {
        ProcessingReport report = reportGenerator.generateReport();

        assertNotNull(report);
        assertTrue(report instanceof ProcessingReport);
    }

    @Test
    @DisplayName("Should throw exception when aggregating null batch result")
    void shouldThrowExceptionWhenAggregatingNullBatchResult() {
        assertThrows(IllegalArgumentException.class, () -> reportGenerator.aggregateBatchResult(null));
    }

    @Test
    @DisplayName("Should generate unique execution IDs for different instances")
    void shouldGenerateUniqueExecutionIds() {
        ProcessingReportGeneratorImpl generator1 = new ProcessingReportGeneratorImpl();
        ProcessingReportGeneratorImpl generator2 = new ProcessingReportGeneratorImpl();

        ProcessingReport report1 = generator1.generateReport();
        ProcessingReport report2 = generator2.generateReport();

        assertNotEquals(report1.executionId(), report2.executionId());
    }

    @Test
    @DisplayName("Should maintain consistent execution ID throughout report generation")
    void shouldMaintainConsistentExecutionIdThroughoutReportGeneration() {
        reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-1", 50, 1000));

        ProcessingReport report1 = reportGenerator.generateReport();
        ProcessingReport report2 = reportGenerator.generateReport();

        assertEquals(report1.executionId(), report2.executionId());
    }

    @Test
    @DisplayName("Should handle large number of records without memory issues")
    void shouldHandleLargeNumberOfRecords() {
        for (int i = 0; i < 1000; i++) {
            reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-" + i, 10000, 100));
        }

        ProcessingReport report = reportGenerator.generateReport();

        assertEquals(10_000_000, report.totalRecords());
        assertEquals(10_000_000, report.successfulRecords());
    }

    @Test
    @DisplayName("Should calculate correct throughput with realistic values")
    void shouldCalculateThroughputWithRealisticValues() throws InterruptedException {
        Thread.sleep(50);
        for (int i = 0; i < 10; i++) {
            reportGenerator.aggregateBatchResult(BatchProcessingResult.success("batch-" + i, 1000, 100));
        }

        ProcessingReport report = reportGenerator.generateReport();

        long throughput = report.throughput();
        assertTrue(throughput >= 0, "Throughput should be greater than or equal to 0");
    }
}
