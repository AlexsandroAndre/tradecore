package com.alexsandroandre.tradecore.application.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@DisplayName("StreamPipelineEngine Tests")
class StreamPipelineEngineTest {

    private StreamPipelineEngine engine;

    @Mock
    private BatchProcessor batchProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        engine = new StreamPipelineEngine(batchProcessor);
    }

    @Test
    @DisplayName("should process empty stream and return empty report")
    void testProcessEmptyStream() {
        Stream<Transaction> emptyStream = Stream.empty();
        when(batchProcessor.groupIntoBatches(anyList()))
            .thenReturn(new ArrayList<>());
        when(batchProcessor.executeBatches(anyList()))
            .thenReturn(new ArrayList<>());

        ProcessingReport report = engine.execute(emptyStream);

        assertEquals(0, report.totalRecords());
        assertEquals(0, report.successfulRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
    }

    @Test
    @DisplayName("should process successful transactions through batches")
    void testProcessSuccessfulTransactions() {
        List<Transaction> transactions = createTransactionList(5);
        List<Batch> batches = new ArrayList<>();
        batches.add(new Batch("batch-1", transactions, 1000));

        List<BatchProcessingResult> results = new ArrayList<>();
        results.add(BatchProcessingResult.success("batch-1", 5, 100L));

        when(batchProcessor.groupIntoBatches(any()))
            .thenReturn(batches);
        when(batchProcessor.executeBatches(any()))
            .thenReturn(results);

        ProcessingReport report = engine.execute(transactions.stream());

        assertEquals(5, report.totalRecords());
        assertEquals(5, report.successfulRecords());
        assertEquals(0, report.failedRecords());
    }

    @Test
    @DisplayName("should aggregate partial failure results")
    void testAggregatePartialFailureResults() {
        List<Transaction> transactions = createTransactionList(10);
        List<Batch> batches = new ArrayList<>();
        batches.add(new Batch("batch-1", transactions, 1000));

        List<BatchProcessingResult> results = new ArrayList<>();
        results.add(BatchProcessingResult.partialFailure(
            "batch-1",
            7,
            0,
            3,
            100L,
            new ArrayList<>()
        ));

        when(batchProcessor.groupIntoBatches(any()))
            .thenReturn(batches);
        when(batchProcessor.executeBatches(any()))
            .thenReturn(results);

        ProcessingReport report = engine.execute(transactions.stream());

        assertEquals(10, report.totalRecords());
        assertEquals(7, report.successfulRecords());
        assertEquals(3, report.failedRecords());
    }

    @Test
    @DisplayName("should handle multiple batches with mixed results")
    void testHandleMultipleBatchesMixedResults() {
        List<Transaction> batch1Txns = createTransactionList(5);
        List<Transaction> batch2Txns = createTransactionList(5);
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(batch1Txns);
        allTransactions.addAll(batch2Txns);

        List<Batch> batches = new ArrayList<>();
        batches.add(new Batch("batch-1", batch1Txns, 1000));
        batches.add(new Batch("batch-2", batch2Txns, 1000));

        List<BatchProcessingResult> results = new ArrayList<>();
        results.add(BatchProcessingResult.success("batch-1", 5, 50L));
        results.add(BatchProcessingResult.partialFailure(
            "batch-2",
            3,
            0,
            2,
            50L,
            new ArrayList<>()
        ));

        when(batchProcessor.groupIntoBatches(any()))
            .thenReturn(batches);
        when(batchProcessor.executeBatches(any()))
            .thenReturn(results);

        ProcessingReport report = engine.execute(allTransactions.stream());

        assertEquals(10, report.totalRecords());
        assertEquals(8, report.successfulRecords());
        assertEquals(2, report.failedRecords());
    }

    @Test
    @DisplayName("should record execution time")
    void testRecordExecutionTime() {
        List<Transaction> transactions = createTransactionList(1);
        List<Batch> batches = new ArrayList<>();
        batches.add(new Batch("batch-1", transactions, 1000));

        List<BatchProcessingResult> results = new ArrayList<>();
        results.add(BatchProcessingResult.success("batch-1", 1, 10L));

        when(batchProcessor.groupIntoBatches(any()))
            .thenReturn(batches);
        when(batchProcessor.executeBatches(any()))
            .thenReturn(results);

        ProcessingReport report = engine.execute(transactions.stream());

        assertTrue(report.executionTimeMillis() >= 0);
    }

    @Test
    @DisplayName("should handle duplicate detection failures")
    void testHandleDuplicateDetectionFailures() {
        List<Transaction> transactions = createTransactionList(5);
        List<Batch> batches = new ArrayList<>();
        batches.add(new Batch("batch-1", transactions, 1000));

        List<BatchProcessingResult> results = new ArrayList<>();
        results.add(BatchProcessingResult.partialFailure(
            "batch-1",
            0,
            5,
            0,
            50L,
            List.of(new BatchProcessingResult.BatchProcessingError(
                "batch-1",
                "DUPLICATED_TRANSACTION_IN_BATCH",
                "Duplicate detected"
            ))
        ));

        when(batchProcessor.groupIntoBatches(any()))
            .thenReturn(batches);
        when(batchProcessor.executeBatches(any()))
            .thenReturn(results);

        ProcessingReport report = engine.execute(transactions.stream());

        assertEquals(5, report.totalRecords());
        assertEquals(0, report.successfulRecords());
        assertEquals(5, report.rejectedRecords());
    }

    @Test
    @DisplayName("should aggregate metrics correctly for complex scenario")
    void testAggregateMetricsComplexScenario() {
        List<Transaction> batch1Txns = createTransactionList(3);
        List<Transaction> batch2Txns = createTransactionList(3);
        List<Transaction> batch3Txns = createTransactionList(4);
        List<Transaction> allTransactions = new ArrayList<>();
        allTransactions.addAll(batch1Txns);
        allTransactions.addAll(batch2Txns);
        allTransactions.addAll(batch3Txns);

        List<Batch> batches = new ArrayList<>();
        batches.add(new Batch("batch-1", batch1Txns, 1000));
        batches.add(new Batch("batch-2", batch2Txns, 1000));
        batches.add(new Batch("batch-3", batch3Txns, 1000));

        List<BatchProcessingResult> results = new ArrayList<>();
        results.add(BatchProcessingResult.success("batch-1", 3, 30L));
        results.add(BatchProcessingResult.partialFailure("batch-2", 2, 0, 1, 30L, new ArrayList<>()));
        results.add(BatchProcessingResult.success("batch-3", 4, 40L));

        when(batchProcessor.groupIntoBatches(any()))
            .thenReturn(batches);
        when(batchProcessor.executeBatches(any()))
            .thenReturn(results);

        ProcessingReport report = engine.execute(allTransactions.stream());

        assertEquals(10, report.totalRecords());
        assertEquals(9, report.successfulRecords());
        assertEquals(1, report.failedRecords());
    }

    private List<Transaction> createTransactionList(int count) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transactions.add(
                new Transaction(
                    "TXN-" + (i + 1),
                    "ACC-" + (i + 1),
                    BigDecimal.valueOf(100.00 + i),
                    "USD",
                    OffsetDateTime.now(),
                    "SYSTEM",
                    Transaction.TransactionStatus.PENDING
                )
            );
        }
        return transactions;
    }
}
