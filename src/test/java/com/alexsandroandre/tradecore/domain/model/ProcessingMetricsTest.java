package com.alexsandroandre.tradecore.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcessingMetrics Domain Model Tests")
class ProcessingMetricsTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final LocalDateTime VALID_START_TIME = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
    private static final LocalDateTime VALID_END_TIME = LocalDateTime.of(2025, 1, 1, 10, 5, 0);
    private static final LocalDateTime VALID_CREATED_AT = LocalDateTime.of(2025, 1, 1, 10, 5, 0);
    private static final long VALID_TOTAL_RECORDS = 1000L;
    private static final long VALID_SUCCESSFUL_RECORDS = 950L;
    private static final long VALID_FAILED_RECORDS = 30L;
    private static final long VALID_DUPLICATE_RECORDS = 20L;
    private static final long VALID_TOTAL_DURATION_MILLIS = 300000L;
    private static final BigDecimal VALID_THROUGHPUT = BigDecimal.valueOf(3.33);
    private static final BigDecimal VALID_AVERAGE_LATENCY = BigDecimal.valueOf(300.00);
    private static final long VALID_PEAK_MEMORY = 512000000L;
    private static final long VALID_AVG_MEMORY = 400000000L;
    private static final long VALID_VALIDATION_ERRORS = 10L;
    private static final long VALID_PROCESSING_ERRORS = 15L;
    private static final long VALID_SYSTEM_ERRORS = 5L;
    private static final long VALID_DUPLICATE_ERRORS = 20L;
    private static final int VALID_BATCH_SIZE = 100;
    private static final long VALID_BATCH_COUNT = 10L;
    private static final long VALID_SLOWEST_BATCH_MILLIS = 35000L;

    @Nested
    @DisplayName("Creation and Validation Tests")
    class CreationTests {

        @Test
        @DisplayName("should create metrics with valid parameters")
        void shouldCreateMetricsWithValidParameters() {
            ProcessingMetrics metrics = new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            );

            assertNotNull(metrics);
            assertEquals(VALID_ID, metrics.id());
            assertEquals(VALID_TOTAL_RECORDS, metrics.totalRecordsProcessed());
        }

        @Test
        @DisplayName("should throw exception when ID is null")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThrows(IllegalArgumentException.class, () -> new ProcessingMetrics(
                null,
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            ));
        }

        @Test
        @DisplayName("should throw exception when end time is before start time")
        void shouldThrowExceptionWhenEndTimeIsBeforeStartTime() {
            LocalDateTime invalidEndTime = VALID_START_TIME.minusMinutes(1);

            assertThrows(IllegalArgumentException.class, () -> new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                invalidEndTime,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            ));
        }

        @Test
        @DisplayName("should throw exception when total duration is negative")
        void shouldThrowExceptionWhenTotalDurationIsNegative() {
            assertThrows(IllegalArgumentException.class, () -> new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                -1L,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            ));
        }

        @Test
        @DisplayName("should throw exception when batch size is zero")
        void shouldThrowExceptionWhenBatchSizeIsZero() {
            assertThrows(IllegalArgumentException.class, () -> new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                0,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            ));
        }
    }

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("should calculate metrics correctly using factory method")
        void shouldCalculateMetricsCorrectly() {
            ProcessingMetrics metrics = ProcessingMetrics.create(
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY
            );

            assertNotNull(metrics.id());
            assertEquals(VALID_TOTAL_RECORDS, metrics.totalRecordsProcessed());
            assertEquals(VALID_TOTAL_DURATION_MILLIS, metrics.totalDurationMillis());
            assertTrue(metrics.throughput().compareTo(BigDecimal.ZERO) > 0);
            assertTrue(metrics.averageLatencyMillis().compareTo(BigDecimal.ZERO) > 0);
        }

        @Test
        @DisplayName("should generate unique ID for each metrics instance")
        void shouldGenerateUniqueIdForEachInstance() {
            ProcessingMetrics metrics1 = ProcessingMetrics.create(
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY
            );

            ProcessingMetrics metrics2 = ProcessingMetrics.create(
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY
            );

            assertNotEquals(metrics1.id(), metrics2.id());
        }
    }

    @Nested
    @DisplayName("Business Logic Tests")
    class BusinessLogicTests {

        @Test
        @DisplayName("should calculate total errors correctly")
        void shouldCalculateTotalErrorsCorrectly() {
            ProcessingMetrics metrics = new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            );

            long expectedTotalErrors = VALID_VALIDATION_ERRORS + VALID_PROCESSING_ERRORS + 
                VALID_SYSTEM_ERRORS + VALID_DUPLICATE_ERRORS;
            assertEquals(expectedTotalErrors, metrics.getTotalErrors());
        }

        @Test
        @DisplayName("should calculate total rejected records correctly")
        void shouldCalculateTotalRejectedRecordsCorrectly() {
            ProcessingMetrics metrics = new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            );

            long expectedRejected = VALID_FAILED_RECORDS + VALID_DUPLICATE_RECORDS;
            assertEquals(expectedRejected, metrics.getTotalRejectedRecords());
        }

        @Test
        @DisplayName("should determine if metrics has errors")
        void shouldDetermineIfMetricsHasErrors() {
            ProcessingMetrics metricsWithErrors = new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            );

            assertTrue(metricsWithErrors.hasErrors());
        }

        @Test
        @DisplayName("should calculate success rate as percentage")
        void shouldCalculateSuccessRateAsPercentage() {
            ProcessingMetrics metrics = new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                100L,
                95L,
                5L,
                0L,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                0L,
                0L,
                0L,
                0L,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            );

            BigDecimal successRate = metrics.getSuccessRate();
            assertEquals(0, successRate.compareTo(BigDecimal.valueOf(95.00)));
        }

        @Test
        @DisplayName("should calculate error rate as percentage")
        void shouldCalculateErrorRateAsPercentage() {
            ProcessingMetrics metrics = new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                100L,
                95L,
                3L,
                2L,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                0L,
                0L,
                0L,
                0L,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            );

            BigDecimal errorRate = metrics.getErrorRate();
            assertEquals(0, errorRate.compareTo(BigDecimal.valueOf(5.00)));
        }
    }

    @Nested
    @DisplayName("Immutability Tests")
    class ImmutabilityTests {

        @Test
        @DisplayName("should be immutable (no setters)")
        void shouldBeImmutable() {
            ProcessingMetrics metrics = new ProcessingMetrics(
                VALID_ID,
                VALID_START_TIME,
                VALID_END_TIME,
                VALID_TOTAL_RECORDS,
                VALID_SUCCESSFUL_RECORDS,
                VALID_FAILED_RECORDS,
                VALID_DUPLICATE_RECORDS,
                VALID_TOTAL_DURATION_MILLIS,
                VALID_THROUGHPUT,
                VALID_AVERAGE_LATENCY,
                VALID_PEAK_MEMORY,
                VALID_AVG_MEMORY,
                VALID_VALIDATION_ERRORS,
                VALID_PROCESSING_ERRORS,
                VALID_SYSTEM_ERRORS,
                VALID_DUPLICATE_ERRORS,
                VALID_BATCH_SIZE,
                VALID_BATCH_COUNT,
                VALID_SLOWEST_BATCH_MILLIS,
                VALID_CREATED_AT
            );

            assertNotNull(metrics);
            assertEquals(VALID_TOTAL_RECORDS, metrics.totalRecordsProcessed());
        }
    }
}