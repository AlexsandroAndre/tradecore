package com.alexsandroandre.tradecore.application.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

@DisplayName("ProcessingReport Model Tests")
class ProcessingReportTest {

    @Test
    @DisplayName("Should calculate duration correctly")
    void shouldCalculateDurationCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_5000)
            .totalRecords(TOTAL_RECORDS_100)
            .successfulRecords(SUCCESSFUL_RECORDS_90)
            .rejectedRecords(REJECTED_RECORDS_5)
            .failedRecords(FAILED_RECORDS_5)
            .persistedRecords(PERSISTED_RECORDS_90)
            .throughput(THROUGHPUT_20000)
            .build();

        assertEquals(DURATION_4000, report.duration());
    }

    @Test
    @DisplayName("Should calculate validation success rate correctly")
    void shouldCalculateValidationSuccessRateCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_2000)
            .totalRecords(TOTAL_RECORDS_100)
            .successfulRecords(SUCCESSFUL_RECORDS_80)
            .rejectedRecords(REJECTED_RECORDS_10)
            .failedRecords(FAILED_RECORDS_10)
            .persistedRecords(PERSISTED_RECORDS_80)
            .throughput(THROUGHPUT_100000)
            .build();

        double rate = report.validationSuccessRate();
        assertEquals(RATE_0_8, rate, RATE_DELTA);
    }

    @Test
    @DisplayName("Should calculate validation failure rate correctly")
    void shouldCalculateValidationFailureRateCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_2000)
            .totalRecords(TOTAL_RECORDS_100)
            .successfulRecords(SUCCESSFUL_RECORDS_80)
            .rejectedRecords(REJECTED_RECORDS_20)
            .failedRecords(FAILED_RECORDS_0)
            .persistedRecords(PERSISTED_RECORDS_80)
            .throughput(THROUGHPUT_100000)
            .build();

        double rate = report.validationFailureRate();
        assertEquals(RATE_0_2, rate, RATE_DELTA);
    }

    @Test
    @DisplayName("Should calculate persistence success rate correctly")
    void shouldCalculatePersistenceSuccessRateCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_2000)
            .totalRecords(TOTAL_RECORDS_100)
            .successfulRecords(SUCCESSFUL_RECORDS_100)
            .rejectedRecords(REJECTED_RECORDS_0)
            .failedRecords(FAILED_RECORDS_0)
            .persistedRecords(PERSISTED_RECORDS_95)
            .throughput(THROUGHPUT_100000)
            .build();

        double rate = report.persistenceSuccessRate();
        assertEquals(RATE_0_95, rate, RATE_DELTA);
    }

    @Test
    @DisplayName("Should calculate average processing time per record correctly")
    void shouldCalculateAverageProcessingTimePerRecordCorrectly() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_6000)
            .totalRecords(TOTAL_RECORDS_100)
            .successfulRecords(SUCCESSFUL_RECORDS_100)
            .rejectedRecords(REJECTED_RECORDS_0)
            .failedRecords(FAILED_RECORDS_0)
            .persistedRecords(PERSISTED_RECORDS_100)
            .throughput(THROUGHPUT_20000)
            .build();

        double avgTime = report.averageProcessingTimePerRecord();
        assertEquals(RATE_50_0, avgTime, RATE_DELTA);
    }

    @Test
    @DisplayName("Should return zero rates when no records")
    void shouldReturnZeroRatesWhenNoRecords() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_2000)
            .totalRecords(TOTAL_RECORDS_0)
            .successfulRecords(SUCCESSFUL_RECORDS_0)
            .rejectedRecords(REJECTED_RECORDS_0)
            .failedRecords(FAILED_RECORDS_0)
            .persistedRecords(PERSISTED_RECORDS_0)
            .throughput(THROUGHPUT_0)
            .build();

        assertEquals(RATE_0_0, report.validationSuccessRate());
        assertEquals(RATE_0_0, report.validationFailureRate());
        assertEquals(RATE_0_0, report.persistenceSuccessRate());
        assertEquals(RATE_0_0, report.averageProcessingTimePerRecord());
    }

    @Test
    @DisplayName("Should create empty report with execution ID")
    void shouldCreateEmptyReportWithExecutionId() {
        ProcessingReport report = ProcessingReport.empty(EXECUTION_ID_TEST_EXEC);

        assertEquals(EXECUTION_ID_TEST_EXEC, report.executionId());
        assertEquals(TOTAL_RECORDS_0, report.totalRecords());
        assertEquals(SUCCESSFUL_RECORDS_0, report.successfulRecords());
        assertEquals(REJECTED_RECORDS_0, report.rejectedRecords());
        assertEquals(FAILED_RECORDS_0, report.failedRecords());
        assertEquals(PERSISTED_RECORDS_0, report.persistedRecords());
        assertEquals(THROUGHPUT_0, report.throughput());
    }

    @Test
    @DisplayName("Should build report with builder pattern")
    void shouldBuildReportWithBuilderPattern() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_123)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_2000)
            .totalRecords(TOTAL_RECORDS_50)
            .successfulRecords(SUCCESSFUL_RECORDS_45)
            .rejectedRecords(REJECTED_RECORDS_3)
            .failedRecords(FAILED_RECORDS_2)
            .persistedRecords(PERSISTED_RECORDS_45)
            .throughput(THROUGHPUT_50000)
            .build();

        assertEquals(EXECUTION_ID_EXEC_123, report.executionId());
        assertEquals(START_TIME_1000, report.startTime());
        assertEquals(END_TIME_2000, report.endTime());
        assertEquals(TOTAL_RECORDS_50, report.totalRecords());
        assertEquals(SUCCESSFUL_RECORDS_45, report.successfulRecords());
        assertEquals(REJECTED_RECORDS_3, report.rejectedRecords());
        assertEquals(FAILED_RECORDS_2, report.failedRecords());
        assertEquals(PERSISTED_RECORDS_45, report.persistedRecords());
        assertEquals(THROUGHPUT_50000, report.throughput());
    }

    @Test
    @DisplayName("Should validate all required fields in report")
    void shouldValidateAllRequiredFieldsInReport() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_2000)
            .totalRecords(TOTAL_RECORDS_100)
            .successfulRecords(SUCCESSFUL_RECORDS_80)
            .rejectedRecords(REJECTED_RECORDS_10)
            .failedRecords(FAILED_RECORDS_10)
            .persistedRecords(PERSISTED_RECORDS_80)
            .throughput(THROUGHPUT_100000)
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
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_2000)
            .totalRecords(TOTAL_RECORDS_100)
            .successfulRecords(SUCCESSFUL_RECORDS_100)
            .rejectedRecords(REJECTED_RECORDS_0)
            .failedRecords(FAILED_RECORDS_0)
            .persistedRecords(PERSISTED_RECORDS_100)
            .throughput(THROUGHPUT_100000)
            .build();

        ProcessingReport report2 = new ProcessingReport(
            EXECUTION_ID_EXEC_1, START_TIME_1000, END_TIME_2000, TOTAL_RECORDS_100, SUCCESSFUL_RECORDS_100, REJECTED_RECORDS_0, FAILED_RECORDS_0, PERSISTED_RECORDS_100, THROUGHPUT_100000
        );

        assertEquals(report1.executionId(), report2.executionId());
        assertEquals(report1.totalRecords(), report2.totalRecords());
    }

    @Test
    @DisplayName("Should handle zero successful records for persistence rate")
    void shouldHandleZeroSuccessfulRecordsForPersistenceRate() {
        ProcessingReport report = ProcessingReport.builder()
            .executionId(EXECUTION_ID_EXEC_1)
            .startTime(START_TIME_1000)
            .endTime(END_TIME_2000)
            .totalRecords(TOTAL_RECORDS_10)
            .successfulRecords(SUCCESSFUL_RECORDS_0)
            .rejectedRecords(REJECTED_RECORDS_10)
            .failedRecords(FAILED_RECORDS_0)
            .persistedRecords(PERSISTED_RECORDS_0)
            .throughput(THROUGHPUT_0)
            .build();

        assertEquals(RATE_0_0, report.persistenceSuccessRate());
    }


}
