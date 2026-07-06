package com.alexsandroandre.tradecore.interfaces.api.controller;

import com.alexsandroandre.tradecore.application.dto.ProcessingMetricsDto;
import com.alexsandroandre.tradecore.infrastructure.persistence.builder.ProcessingMetricsEntityTestBuilder;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.ProcessingMetricsEntity;
import com.alexsandroandre.tradecore.infrastructure.persistence.repository.ProcessingMetricsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class MetricsControllerTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("financial_processor_test")
        .withUsername("postgres")
        .withPassword("postgres");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ProcessingMetricsRepository metricsRepository;

    private ProcessingMetricsEntity testMetrics;
    private UUID testMetricsId;

    @BeforeEach
    void setUp() {
        metricsRepository.deleteAll();
        
        LocalDateTime now = LocalDateTime.now();
        testMetrics = ProcessingMetricsEntityTestBuilder.builder()
            .id(UUID.randomUUID())
            .startTime(now.minusHours(1))
            .endTime(now)
            .totalRecordsProcessed(1000)
            .successfulRecords(950)
            .failedRecords(30)
            .duplicateRecords(20)
            .totalDurationMillis(3600000)
            .throughput(BigDecimal.valueOf(277.78))
            .averageLatencyMillis(BigDecimal.valueOf(3600.0))
            .peakMemoryUsageBytes(1024000000)
            .averageMemoryUsageBytes(512000000)
            .validationErrors(20)
            .processingErrors(10)
            .systemErrors(0)
            .duplicateErrors(20)
            .batchSize(100)
            .batchCount(10)
            .slowestBatchMillis(5000)
            .createdAt(now)
            .build();
        
        metricsRepository.save(testMetrics);
        testMetricsId = testMetrics.getId();
    }

    @Test
    void testGetAllMetrics() {
        ResponseEntity<ProcessingMetricsDto[]> response = restTemplate.getForEntity(
            "/api/v1/metrics",
            ProcessingMetricsDto[].class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 1);
        
        ProcessingMetricsDto dto = Arrays.stream(response.getBody())
            .filter(m -> m.id().equals(testMetricsId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(dto);
        assertEquals(testMetricsId, dto.id());
        assertEquals(1000, dto.totalRecordsProcessed());
        assertEquals(950, dto.successfulRecords());
        assertEquals(30, dto.failedRecords());
        assertEquals(BigDecimal.valueOf(277.78), dto.throughput());
    }

    @Test
    void testGetMetricsById() {
        ResponseEntity<ProcessingMetricsDto> response = restTemplate.getForEntity(
            "/api/v1/metrics/{id}",
            ProcessingMetricsDto.class,
            testMetricsId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        
        ProcessingMetricsDto dto = response.getBody();
        assertEquals(testMetricsId, dto.id());
        assertEquals(1000, dto.totalRecordsProcessed());
        assertEquals(950, dto.successfulRecords());
        assertEquals(30, dto.failedRecords());
        assertEquals(20, dto.duplicateRecords());
        assertEquals(3600000, dto.totalDurationMillis());
        assertEquals(100, dto.batchSize());
        assertEquals(10, dto.batchCount());
        assertEquals(5000, dto.slowestBatchMillis());
        assertEquals(1024000000, dto.peakMemoryUsageBytes());
        assertEquals(512000000, dto.averageMemoryUsageBytes());
    }

    @Test
    void testGetMetricsById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        ResponseEntity<ProcessingMetricsDto> response = restTemplate.getForEntity(
            "/api/v1/metrics/{id}",
            ProcessingMetricsDto.class,
            nonExistentId
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetLatestMetrics() {
        LocalDateTime now = LocalDateTime.now();
        ProcessingMetricsEntity olderMetrics = ProcessingMetricsEntityTestBuilder.builder()
            .id(UUID.randomUUID())
            .startTime(now.minusHours(5))
            .endTime(now.minusHours(4))
            .totalRecordsProcessed(500)
            .successfulRecords(450)
            .failedRecords(50)
            .duplicateRecords(0)
            .totalDurationMillis(3600000)
            .throughput(BigDecimal.valueOf(138.89))
            .averageLatencyMillis(BigDecimal.valueOf(7200.0))
            .peakMemoryUsageBytes(512000000)
            .averageMemoryUsageBytes(256000000)
            .validationErrors(30)
            .processingErrors(20)
            .systemErrors(0)
            .duplicateErrors(0)
            .batchSize(50)
            .batchCount(10)
            .slowestBatchMillis(4000)
            .createdAt(now.minusHours(4))
            .build();

        metricsRepository.save(olderMetrics);

        ResponseEntity<ProcessingMetricsDto> response = restTemplate.getForEntity(
            "/api/v1/metrics/latest",
            ProcessingMetricsDto.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());

        ProcessingMetricsDto dto = response.getBody();
        assertEquals(testMetricsId, dto.id());
    }

    @Test
    void testGetMetricsByDateRange() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startRange = now.minusHours(2);
        LocalDateTime endRange = now.plusHours(1);

        String url = "/api/v1/metrics/range?start={start}&end={end}";
        ResponseEntity<ProcessingMetricsDto[]> response = restTemplate.getForEntity(
            url,
            ProcessingMetricsDto[].class,
            startRange,
            endRange
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().length >= 1);
        
        ProcessingMetricsDto dto = Arrays.stream(response.getBody())
            .filter(m -> m.id().equals(testMetricsId))
            .findFirst()
            .orElse(null);
        
        assertNotNull(dto);
        assertEquals(testMetricsId, dto.id());
    }

    @Test
    void testGetMetricsByDateRange_NoResults() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startRange = now.plusDays(1);
        LocalDateTime endRange = now.plusDays(2);

        String url = "/api/v1/metrics/range?start={start}&end={end}";
        ResponseEntity<ProcessingMetricsDto[]> response = restTemplate.getForEntity(
            url,
            ProcessingMetricsDto[].class,
            startRange,
            endRange
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().length);
    }

    @Test
    void testResponseJsonStructure() {
        ResponseEntity<ProcessingMetricsDto> response = restTemplate.getForEntity(
            "/api/v1/metrics/{id}",
            ProcessingMetricsDto.class,
            testMetricsId
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ProcessingMetricsDto dto = response.getBody();
        
        assertNotNull(dto.id());
        assertNotNull(dto.startTime());
        assertNotNull(dto.endTime());
        assertNotNull(dto.throughput());
        assertNotNull(dto.averageLatencyMillis());
        assertNotNull(dto.createdAt());
        
        assertFalse(dto.totalRecordsProcessed() < 0);
        assertFalse(dto.successfulRecords() < 0);
        assertFalse(dto.failedRecords() < 0);
        assertFalse(dto.peakMemoryUsageBytes() < 0);
    }

    @Test
    void testAllMetricsFieldsArePresent() {
        ResponseEntity<ProcessingMetricsDto> response = restTemplate.getForEntity(
            "/api/v1/metrics/{id}",
            ProcessingMetricsDto.class,
            testMetricsId
        );

        ProcessingMetricsDto dto = response.getBody();
        assertNotNull(dto.id());
        assertNotNull(dto.startTime());
        assertNotNull(dto.endTime());
        assertNotNull(dto.totalRecordsProcessed());
        assertNotNull(dto.successfulRecords());
        assertNotNull(dto.failedRecords());
        assertNotNull(dto.duplicateRecords());
        assertNotNull(dto.totalDurationMillis());
        assertNotNull(dto.throughput());
        assertNotNull(dto.averageLatencyMillis());
        assertNotNull(dto.peakMemoryUsageBytes());
        assertNotNull(dto.averageMemoryUsageBytes());
        assertNotNull(dto.validationErrors());
        assertNotNull(dto.processingErrors());
        assertNotNull(dto.systemErrors());
        assertNotNull(dto.duplicateErrors());
        assertNotNull(dto.batchSize());
        assertNotNull(dto.batchCount());
        assertNotNull(dto.slowestBatchMillis());
        assertNotNull(dto.createdAt());
    }
}
