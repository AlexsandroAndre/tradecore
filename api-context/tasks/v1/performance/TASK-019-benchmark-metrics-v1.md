# TASK-019 — Benchmark Metrics V1

## Phase
V1 — Performance

## Module
performance

---

# Objective

Implement a comprehensive metrics collection system to measure and track the performance characteristics of the financial data processing platform.

The goal is to provide real-time visibility into system behavior:

- transaction processing throughput
- memory consumption patterns
- CPU utilization
- processing latency
- error rates and types
- batch processing statistics

---

# Scope

This task includes:

- domain model for immutable metrics
- metrics collection service
- persistence layer for metrics storage
- REST API endpoints for metrics retrieval
- integration with Spring Boot Actuator
- comprehensive testing (unit + integration)

---

# Problem Context

The platform processes millions of financial records.

Without metrics, we cannot:

- measure processing throughput
- detect performance degradation
- identify resource bottlenecks
- validate optimization efforts
- monitor for failures
- generate performance reports

Benchmark metrics enable data-driven performance improvements and production readiness validation.

---

# Architecture Placement

## Domain Layer

Create immutable, framework-independent metrics domain models:

```
domain/model/ProcessingMetrics
domain/model/MetricsSnapshot
```

The domain must NOT depend on:

- Spring Framework
- databases
- HTTP

## Application Layer

Create services and ports for metrics operations:

```
application/service/MetricsCollector
application/port/ProcessingMetricsPort
application/dto/ProcessingMetricsDto
```

## Infrastructure Layer

Implement metrics persistence:

```
infrastructure/persistence/entity/ProcessingMetricsEntity
infrastructure/persistence/repository/ProcessingMetricsRepository
infrastructure/persistence/adapter/ProcessingMetricsRepositoryAdapter
```

## Interfaces Layer

Create REST endpoints:

```
interfaces/api/controller/MetricsController
interfaces/api/response/ProcessingMetricsResponse
```

---

# Metrics to Collect

## Processing Metrics

- **startTime**: when processing began
- **endTime**: when processing completed
- **totalRecordsProcessed**: total count
- **successfulRecords**: accepted count
- **failedRecords**: rejected count
- **duplicateRecords**: duplicate count
- **totalDuration**: execution time in milliseconds
- **throughput**: records per second
- **averageLatency**: average processing time per record in milliseconds

## Resource Metrics

- **peakMemoryUsage**: maximum heap usage in bytes
- **averageMemoryUsage**: average heap usage in bytes
- **cpuTimeMillis**: total CPU time used
- **threadCount**: maximum concurrent threads

## Error Metrics

- **validationErrors**: count
- **processingErrors**: count
- **systemErrors**: count
- **duplicateErrors**: count

## Batch Metrics

- **batchSize**: configurable batch size
- **batchCount**: number of batches processed
- **slowestBatch**: slowest batch execution time

---

# ProcessingMetrics Model

Immutable value object representing complete metrics for a processing operation.

Requirements:

- immutable (final fields)
- framework-independent
- all calculations performed in domain
- throughput = totalRecords / (totalDuration / 1000)
- averageLatency = totalDuration / totalRecords

---

# MetricsCollector Service

Application service responsible for:

- receiving processing data
- delegating to domain for calculations
- persisting via port
- returning collected metrics

Must NOT contain business logic (calculations are in domain).

---

# ProcessingMetricsPort

Port interface for metrics persistence.

Methods:

- save(ProcessingMetrics): void
- findById(UUID): Optional\<ProcessingMetrics\>
- findAll(): List\<ProcessingMetrics\>
- findByDateRange(LocalDateTime start, LocalDateTime end): List\<ProcessingMetrics\>

---

# ProcessingMetricsEntity

JPA entity mapping ProcessingMetrics to database.

Table: processing_metrics

Fields:

- id (UUID, primary key)
- startTime (timestamp)
- endTime (timestamp)
- totalRecordsProcessed (long)
- successfulRecords (long)
- failedRecords (long)
- duplicateRecords (long)
- totalDurationMillis (long)
- throughput (decimal)
- averageLatencyMillis (decimal)
- peakMemoryUsageBytes (long)
- averageMemoryUsageBytes (long)
- validationErrors (long)
- processingErrors (long)
- systemErrors (long)
- duplicateErrors (long)
- batchSize (int)
- batchCount (long)
- slowestBatchMillis (long)
- createdAt (timestamp)

---

# MetricsController

REST endpoints:

## Get all metrics
GET /api/v1/metrics

Response: List\<ProcessingMetricsResponse\>

## Get metrics by ID
GET /api/v1/metrics/{id}

Response: ProcessingMetricsResponse

## Get metrics by date range
GET /api/v1/metrics/range?start=2025-01-01T00:00:00&end=2025-01-02T00:00:00

Response: List\<ProcessingMetricsResponse\>

## Get latest metrics
GET /api/v1/metrics/latest

Response: ProcessingMetricsResponse

---

# ProcessingMetricsResponse

REST response DTO containing all metrics fields.

Uses Record for immutability.

---

# Metrics Integration Points

## After Processing Completion

The processing pipeline must notify MetricsCollector after each processing cycle:

```java
metricCollector.collectMetrics(processingResult);
```

## Spring Boot Actuator

Integrate with /actuator/metrics endpoints for system-level metrics.

---

# Coding Requirements

Follow:

- SOLID principles
- Clean Code
- Hexagonal Architecture
- Dependency Inversion

Naming conventions:

- Classes: ProcessingMetrics, MetricsCollector (PascalCase)
- Methods: collectMetrics(), findById() (camelCase)
- Constants: METRIC_VERSION (UPPER_CASE)

---

# Testing Requirements

## Unit Tests

Domain model tests:

- ProcessingMetrics creation and immutability
- calculations (throughput, latency)
- validation of metrics values

Application service tests:

- MetricsCollector saves via port
- proper delegation to domain
- error handling

## Integration Tests

- MetricsController endpoints return 200
- metrics persisted to database
- date range queries work correctly
- metrics retrieved from database match saved values

---

# Acceptance Criteria

This task is complete when:

- ProcessingMetrics immutable model created in domain
- MetricsCollector service implemented in application
- ProcessingMetricsPort interface defined
- ProcessingMetricsRepositoryAdapter implemented
- ProcessingMetricsEntity and repository created
- MetricsController with all endpoints implemented
- ProcessingMetricsResponse DTO created
- all metrics fields populated correctly
- unit tests pass (domain + service)
- integration tests pass (persistence + API)
- code follows architecture rules
- all calculations verified

---

# Out of Scope

This task does NOT include:

- Prometheus/Grafana integration (V5+)
- distributed metrics aggregation (V2+)
- real-time metrics streaming
- performance optimization based on metrics
- automated alerting

---

# Output

A complete, tested metrics collection system capable of capturing, storing, and exposing performance characteristics of the processing pipeline via REST API.

---

# Notes

Metrics are critical for validating the platform's performance claims and identifying optimization opportunities. All metrics must be collected automatically without manual intervention. Future versions will integrate with Prometheus for long-term metrics storage and Grafana for visualization.