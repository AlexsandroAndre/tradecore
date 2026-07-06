package com.alexsandroandre.tradecore.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcessingReport Model Tests")
class ProcessingReportTest {

    @Test
    @DisplayName("Should calculate duration correctly")
    void shouldCalculateDurationCorrectly() {
        long startTime = 1000;
        long endTime = 5000;
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(startTime)
            .endTime(endTime)
            .totalRecords(100)
            .successfulRecords(90)
            .rejectedRecords(5)
            .failedRecords(5)
            .persistedRecords(90)
            .throughput(20000)
            .build();

        assertEquals(4000, report.duration());
    }

    @Test
    @DisplayName("Should calculate validation success rate correctly")
    void shouldCalculateValidationSuccessRateCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(1000)
            .endTime(2000)
            .totalRecords(100)
            .successfulRecords(80)
            .rejectedRecords(10)
            .failedRecords(10)
            .persistedRecords(80)
            .throughput(100000)
            .build();

        double rate = report.validationSuccessRate();
        assertEquals(0.8, rate, 0.0001);
    }

    @Test
    @DisplayName("Should calculate validation failure rate correctly")
    void shouldCalculateValidationFailureRateCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(1000)
            .endTime(2000)
            .totalRecords(100)
            .successfulRecords(80)
            .rejectedRecords(20)
            .failedRecords(0)
            .persistedRecords(80)
            .throughput(100000)
            .build();

        double rate = report.validationFailureRate();
        assertEquals(0.2, rate, 0.0001);
    }

    @Test
    @DisplayName("Should calculate persistence success rate correctly")
    void shouldCalculatePersistenceSuccessRateCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(1000)
            .endTime(2000)
            .totalRecords(100)
            .successfulRecords(100)
            .rejectedRecords(0)
            .failedRecords(0)
            .persistedRecords(95)
            .throughput(100000)
            .build();

        double rate = report.persistenceSuccessRate();
        assertEquals(0.95, rate, 0.0001);
    }

    @Test
    @DisplayName("Should calculate average processing time per record correctly")
    void shouldCalculateAverageProcessingTimePerRecordCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(1000)
            .endTime(6000)
            .totalRecords(100)
            .successfulRecords(100)
            .rejectedRecords(0)
            .failedRecords(0)
            .persistedRecords(100)
            .throughput(20000)
            .build();

        double avgTime = report.averageProcessingTimePerRecord();
        assertEquals(50.0, avgTime, 0.0001);
    }

    @Test
    @DisplayName("Should return zero rates when no records")
    void shouldReturnZeroRatesWhenNoRecords() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(1000)
            .endTime(2000)
            .totalRecords(0)
            .successfulRecords(0)
            .rejectedRecords(0)
            .failedRecords(0)
            .persistedRecords(0)
            .throughput(0)
            .build();

        assertEquals(0.0, report.validationSuccessRate());
        assertEquals(0.0, report.validationFailureRate());
        assertEquals(0.0, report.persistenceSuccessRate());
        assertEquals(0.0, report.averageProcessingTimePerRecord());
    }

    @Test
    @DisplayName("Should create empty report with execution ID")
    void shouldCreateEmptyReportWithExecutionId() {
        ProcessingReport report = ProcessingReport.empty("test-exec");

        assertEquals("test-exec", report.executionId());
        assertEquals(0, report.totalRecords());
        assertEquals(0, report.successfulRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
        assertEquals(0, report.persistedRecords());
        assertEquals(0, report.throughput());
    }

    @Test
    @DisplayName("Should build report with builder pattern")
    void shouldBuildReportWithBuilderPattern() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-123")
            .startTime(1000)
            .endTime(2000)
            .totalRecords(50)
            .successfulRecords(45)
            .rejectedRecords(3)
            .failedRecords(2)
            .persistedRecords(45)
            .throughput(50000)
            .build();

        assertEquals("exec-123", report.executionId());
        assertEquals(1000, report.startTime());
        assertEquals(2000, report.endTime());
        assertEquals(50, report.totalRecords());
        assertEquals(45, report.successfulRecords());
        assertEquals(3, report.rejectedRecords());
        assertEquals(2, report.failedRecords());
        assertEquals(45, report.persistedRecords());
        assertEquals(50000, report.throughput());
    }

    @Test
    @DisplayName("Should validate all required fields in report")
    void shouldValidateAllRequiredFieldsInReport() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(1000)
            .endTime(2000)
            .totalRecords(100)
            .successfulRecords(80)
            .rejectedRecords(10)
            .failedRecords(10)
            .persistedRecords(80)
            .throughput(100000)
            .build();

        assertNotNull(report.executionId());
        assertTrue(report.startTime() > 0);
        assertTrue(report.endTime() >= report.startTime());
        assertTrue(report.totalRecords() >= 0);
        assertTrue(report.successfulRecords() >= 0);
        assertTrue(report.rejectedRecords() >= 0);
        assertTrue(report.failedRecords() >= 0);
        assertTrue(report.persistedRecords() >= 0);
        assertTrue(report.throughput() >= 0);
    }

    @Test
    @DisplayName("Should be immutable record")
    void shouldBeImmutableRecord() {
        ProcessingReport report1 = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(1000)
            .endTime(2000)
            .totalRecords(100)
            .successfulRecords(100)
            .rejectedRecords(0)
            .failedRecords(0)
            .persistedRecords(100)
            .throughput(100000)
            .build();

        ProcessingReport report2 = new ProcessingReport(
            "exec-1", 1000, 2000, 100, 100, 0, 0, 100, 100000
        );

        assertEquals(report1.executionId(), report2.executionId());
        assertEquals(report1.totalRecords(), report2.totalRecords());
    }

    @Test
    @DisplayName("Should handle zero successful records for persistence rate")
    void shouldHandleZeroSuccessfulRecordsForPersistenceRate() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId("exec-1")
            .startTime(1000)
            .endTime(2000)
            .totalRecords(10)
            .successfulRecords(0)
            .rejectedRecords(10)
            .failedRecords(0)
            .persistedRecords(0)
            .throughput(0)
            .build();

        assertEquals(0.0, report.persistenceSuccessRate());
    }
}
