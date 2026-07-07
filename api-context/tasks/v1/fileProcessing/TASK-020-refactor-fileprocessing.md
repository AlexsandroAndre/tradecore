# TASK-020 — Refactor FileProcessingController for Streaming JSON Input

## Phase
V1 — File Processing

## Module
fileProcessing

---

# Objective

Refactor the `FileProcessingController` to add a new endpoint that reads and processes the 20 million transactions from a static JSON file (`transactions-20M.json`) instead of relying on file uploads.

The primary goal is to:
- Enable rapid processing of large-scale transaction files via file system access
- Replace multipart file upload approach with direct file stream reading
- Maintain project's performance-first principle using streaming JSON parsing
- Properly integrate metrics collection with the processing pipeline
- Support batch processing with configurable batch sizes

---

# Problem Context

Current implementation (`uploadAndProcessTransactions`) requires:
- Client-side file upload via HTTP multipart
- Network I/O overhead
- Client must have the file locally

For testing and benchmarking scenarios with a fixed 20M transaction dataset:
- Direct file system access is faster
- No network latency
- Enables repeatable performance measurements
- Better utilization of system resources

---

# Scope

This task includes:

- New endpoint in `FileProcessingController` for direct file processing
- Streaming JSON parser implementation (similar to existing upload method)
- Batch processing with configurable batch size
- Full metrics collection and persistence
- Error handling and validation
- Integration with `MetricsCollector` service
- Integration with `ProcessingOrchestrator`
- Unit and integration tests

---

# Architecture

## Controller Layer (FileProcessingController)

### Existing Method (Keep as-is)
- `uploadAndProcessTransactions()` - handles multipart file uploads

### New Method to Add
- `processLocalTransactionFile()` - reads from file system

Both methods should:
- Use streaming JSON parsing for memory efficiency
- Process transactions in batches (BATCH_SIZE = 10000)
- Collect comprehensive metrics
- Persist metrics to database via `MetricsCollector`

---

# New Endpoint Specification

## Endpoint

```
GET /api/v1/files/process-local
```

## Response

```json
{
  "processingId": "uuid",
  "totalRecordsProcessed": 20000000,
  "successfulRecords": 19995000,
  "failedRecords": 5000,
  "rejectedRecords": 0,
  "totalDurationMillis": 45000,
  "throughput": 444444.44,
  "peakMemoryUsageBytes": 1073741824,
  "averageMemoryUsageBytes": 536870912,
  "validationErrors": 3000,
  "processingErrors": 2000,
  "systemErrors": 0,
  "duplicateErrors": 0,
  "status": "SUCCESS",
  "message": "File processed successfully: 20000000 transactions processed in 45000ms"
}
```

## HTTP Status Codes

- `200 OK` - Processing completed successfully
- `404 NOT_FOUND` - File not found
- `500 INTERNAL_SERVER_ERROR` - Processing error

---

# Metrics Collection Integration

The new endpoint MUST properly integrate with `MetricsCollector`:

```java
ProcessingMetrics metrics = metricsCollector.collectMetrics(
    startTime,
    endTime,
    totalRecordsProcessed,
    successfulRecords,
    failedRecords,
    duplicateRecords,
    validationErrors,
    processingErrors,
    systemErrors,
    duplicateErrors,
    BATCH_SIZE,
    batchCount,
    slowestBatchMillis,
    peakMemoryUsageBytes,
    averageMemoryUsageBytes
);
```

All metrics must be:
- Calculated accurately
- Persisted to database via `MetricsCollector`
- Retrievable via metrics endpoints (from TASK-019)

---

# File Processing Details

## File Path

- Location: `transactions-20M.json` (in project resources or configurable path)
- Format: JSON array of transaction objects
- Structure:
  ```json
  [
    {
      "transactionId": "TRX000001",
      "accountId": "ACC000001",
      "amount": 1234.56,
      "currency": "USD",
      "timestamp": "2025-01-01T10:00:00Z",
      "source": "WEB"
    },
    ...
  ]
  ```

## Batch Processing

- Batch size: 10000 transactions (same as upload method)
- Process each batch via `processingOrchestrator.orchestrate(stream)`
- Measure batch processing time for `slowestBatchMillis`
- Track `batchCount`

## Resource Monitoring

Track during processing:
- Peak memory usage
- Average memory usage
- CPU time (if available)
- Thread count

---

# Transaction Validation

Same validation rules as upload method:

- `transactionId` - required, non-empty
- `accountId` - required, non-empty
- `amount` - required, valid decimal
- `currency` - required, 3-letter code
- `timestamp` - required, valid ISO-8601 format
- `source` - required, non-empty

Rejected transactions:
- Count as `rejectedRecords`
- Do NOT count as `failedRecords`
- Do NOT stop processing

Invalid transactions:
- Increment appropriate error counter
- Log validation errors
- Continue processing

---

# Error Handling

## IOException (File not found, read errors)
- Return HTTP 404 if file not found
- Return HTTP 500 for read errors
- Include error message in response

## Processing Errors
- Catch per-transaction exceptions
- Increment error counters
- Continue processing remaining transactions
- Do not throw exceptions

## Memory Errors
- Implement memory monitoring
- Log warnings if peak memory exceeded
- Continue processing

---

# Performance Requirements

The implementation MUST:

- Use streaming JSON parsing (already in use)
- Minimize object creation
- Process 20M records efficiently
- Complete within reasonable time (target: < 60 seconds)
- Memory footprint < 2GB for batch processing

---

# Testing Requirements

## Unit Tests

- `testProcessLocalTransactionFile_Success()` - verify successful processing
- `testProcessLocalTransactionFile_FileNotFound()` - verify 404 error
- `testProcessLocalTransactionFile_MetricsCollection()` - verify metrics saved
- `testProcessLocalTransactionFile_BatchProcessing()` - verify batch boundaries
- `testProcessLocalTransactionFile_ErrorHandling()` - verify error counters
- `testProcessLocalTransactionFile_InvalidRecords()` - verify validation

## Integration Tests

- Full file processing with metrics persistence
- Verify metrics retrievable via MetricsController endpoints
- Verify database contains expected records
- Test with sample dataset (1000-10000 transactions)

---

# Implementation Checklist

- [ ] Create `processLocalTransactionFile()` endpoint
- [ ] Implement streaming JSON parsing for file
- [ ] Implement batch processing loop
- [ ] Implement transaction validation
- [ ] Implement error handling
- [ ] Implement memory monitoring
- [ ] Integrate with `MetricsCollector`
- [ ] Integrate with `ProcessingOrchestrator`
- [ ] Create proper error responses
- [ ] Add unit tests (all scenarios)
- [ ] Add integration tests
- [ ] Verify metrics persisted correctly
- [ ] Verify metrics calculable via endpoints
- [ ] Performance validation (throughput, latency)

---

# Critical Integration Points

1. **MetricsCollector** - Must call `collectMetrics()` with all parameters
2. **ProcessingOrchestrator** - Must call `orchestrate(stream)` for each batch
3. **ProcessingMetrics** - Must use domain model for calculations
4. **Database** - Must persist metrics via `ProcessingMetricsPort`

---

# Coding Standards

Follow existing patterns:
- Use streaming JSON parsing (Jackson)
- Batch processing with fixed batch size
- Exception handling per transaction
- Proper resource cleanup (try-with-resources)
- Consistent naming conventions
- Clear error messages

---

# Expected Outcome

A production-ready endpoint that:
- Processes large transaction files efficiently
- Collects comprehensive performance metrics
- Persists metrics to database
- Provides accurate performance reporting
- Handles errors gracefully
- Maintains system performance under load

---

# Related Tasks

- TASK-019 — Benchmark Metrics V1 (metrics infrastructure)
- TASK-016 — Transaction Domain Model
- TASK-018 — Processing Orchestrator 
