package com.alexsandroandre.tradecore.application.service;

import com.alexsandroandre.tradecore.application.port.ProcessingMetricsPort;
import com.alexsandroandre.tradecore.domain.model.ProcessingMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("MetricsCollector Service Tests")
class MetricsCollectorTest {

    @Mock
    private ProcessingMetricsPort metricsPort;

    private MetricsCollector metricsCollector;

    private static final LocalDateTime VALID_START_TIME = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
    private static final LocalDateTime VALID_END_TIME = LocalDateTime.of(2025, 1, 1, 10, 5, 0);
    private static final long VALID_TOTAL_RECORDS = 1000L;
    private static final long VALID_SUCCESSFUL_RECORDS = 950L;
    private static final long VALID_FAILED_RECORDS = 30L;
    private static final long VALID_DUPLICATE_RECORDS = 20L;
    private static final long VALID_VALIDATION_ERRORS = 10L;
    private static final long VALID_PROCESSING_ERRORS = 15L;
    private static final long VALID_SYSTEM_ERRORS = 5L;
    private static final long VALID_DUPLICATE_ERRORS = 20L;
    private static final int VALID_BATCH_SIZE = 100;
    private static final long VALID_BATCH_COUNT = 10L;
    private static final long VALID_SLOWEST_BATCH_MILLIS = 35000L;
    private static final long VALID_PEAK_MEMORY = 512000000L;
    private static final long VALID_AVG_MEMORY = 400000000L;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        metricsCollector = new MetricsCollector(metricsPort);
    }

    @Nested
    @DisplayName("collectMetrics Method Tests")
    class CollectMetricsTests {

        @Test
        @DisplayName("should collect and save metrics via port")
        void shouldCollectAndSaveMetricsViaPort() {
            ProcessingMetrics result = metricsCollector.collectMetrics(
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

            assertNotNull(result);
            assertEquals(VALID_TOTAL_RECORDS, result.totalRecordsProcessed());
            verify(metricsPort, times(1)).save(any(ProcessingMetrics.class));
        }

        @Test
        @DisplayName("should delegate to port for persistence")
        void shouldDelegateToPortForPersistence() {
            metricsCollector.collectMetrics(
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

            verify(metricsPort, times(1)).save(any(ProcessingMetrics.class));
        }
    }

    @Nested
    @DisplayName("findMetricsById Method Tests")
    class FindMetricsByIdTests {

        @Test
        @DisplayName("should retrieve metrics by ID from port")
        void shouldRetrieveMetricsById() {
            UUID metricId = UUID.randomUUID();
            ProcessingMetrics expectedMetrics = ProcessingMetrics.create(
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

            when(metricsPort.findById(eq(metricId))).thenReturn(Optional.of(expectedMetrics));

            Optional<ProcessingMetrics> result = metricsCollector.findMetricsById(metricId);

            assertTrue(result.isPresent());
            assertEquals(expectedMetrics.id(), result.get().id());
            verify(metricsPort, times(1)).findById(metricId);
        }

        @Test
        @DisplayName("should return empty optional when metrics not found")
        void shouldReturnEmptyOptionalWhenNotFound() {
            UUID metricId = UUID.randomUUID();
            when(metricsPort.findById(eq(metricId))).thenReturn(Optional.empty());

            Optional<ProcessingMetrics> result = metricsCollector.findMetricsById(metricId);

            assertFalse(result.isPresent());
            verify(metricsPort, times(1)).findById(metricId);
        }
    }

    @Nested
    @DisplayName("getAllMetrics Method Tests")
    class GetAllMetricsTests {

        @Test
        @DisplayName("should retrieve all metrics from port")
        void shouldRetrieveAllMetrics() {
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

            when(metricsPort.findAll()).thenReturn(List.of(metrics1, metrics2));

            List<ProcessingMetrics> result = metricsCollector.getAllMetrics();

            assertEquals(2, result.size());
            verify(metricsPort, times(1)).findAll();
        }

        @Test
        @DisplayName("should return empty list when no metrics exist")
        void shouldReturnEmptyListWhenNoMetricsExist() {
            when(metricsPort.findAll()).thenReturn(List.of());

            List<ProcessingMetrics> result = metricsCollector.getAllMetrics();

            assertTrue(result.isEmpty());
            verify(metricsPort, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("getMetricsByDateRange Method Tests")
    class GetMetricsByDateRangeTests {

        @Test
        @DisplayName("should retrieve metrics by date range from port")
        void shouldRetrieveMetricsByDateRange() {
            LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 1, 2, 0, 0, 0);

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

            when(metricsPort.findByDateRange(eq(startTime), eq(endTime)))
                .thenReturn(List.of(metrics));

            List<ProcessingMetrics> result = metricsCollector.getMetricsByDateRange(startTime, endTime);

            assertEquals(1, result.size());
            verify(metricsPort, times(1)).findByDateRange(startTime, endTime);
        }

        @Test
        @DisplayName("should return empty list when no metrics in date range")
        void shouldReturnEmptyListWhenNoMetricsInRange() {
            LocalDateTime startTime = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
            LocalDateTime endTime = LocalDateTime.of(2025, 1, 2, 0, 0, 0);

            when(metricsPort.findByDateRange(eq(startTime), eq(endTime)))
                .thenReturn(List.of());

            List<ProcessingMetrics> result = metricsCollector.getMetricsByDateRange(startTime, endTime);

            assertTrue(result.isEmpty());
            verify(metricsPort, times(1)).findByDateRange(startTime, endTime);
        }
    }
}