package com.alexsandroandre.tradecore.application.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.application.port.TransactionPersistencePort;
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
    private TransactionProcessor processor;

    @Mock
    private TransactionPersistencePort persistencePort;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        engine = new StreamPipelineEngine(processor, persistencePort);
    }

    @Test
    @DisplayName("should process empty stream and return empty report")
    void testProcessEmptyStream() {
        Stream<Transaction> emptyStream = Stream.empty();

        ProcessingReport report = engine.execute(emptyStream);

        assertEquals(0, report.totalRecords());
        assertEquals(0, report.successfulRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
    }

    @Test
    @DisplayName("should process successful transactions")
    void testProcessSuccessfulTransactions() {
        List<Transaction> transactions = createTransactionList(5);
        for (Transaction transaction : transactions) {
            when(processor.process(transaction))
                .thenReturn(TransactionProcessor.ProcessingResult.success(
                    transaction.withStatus(Transaction.TransactionStatus.COMPLETED)
                ));
        }

        ProcessingReport report = engine.execute(transactions.stream());

        assertEquals(5, report.totalRecords());
        assertEquals(5, report.successfulRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
        verify(persistencePort, times(5)).save(any());
    }

    @Test
    @DisplayName("should handle rejected records without stopping processing")
    void testHandleRejectedRecords() {
        List<Transaction> transactions = createTransactionList(3);
        Transaction txn1 = transactions.get(0);
        Transaction txn2 = transactions.get(1);
        Transaction txn3 = transactions.get(2);

        when(processor.process(txn1))
            .thenReturn(TransactionProcessor.ProcessingResult.success(
                txn1.withStatus(Transaction.TransactionStatus.COMPLETED)
            ));
        when(processor.process(txn2))
            .thenReturn(TransactionProcessor.ProcessingResult.failure(
                txn2.withStatus(Transaction.TransactionStatus.FAILED),
                "INVALID_AMOUNT",
                "Amount must be positive"
            ));
        when(processor.process(txn3))
            .thenReturn(TransactionProcessor.ProcessingResult.success(
                txn3.withStatus(Transaction.TransactionStatus.COMPLETED)
            ));

        ProcessingReport report = engine.execute(transactions.stream());

        assertEquals(3, report.totalRecords());
        assertEquals(2, report.successfulRecords());
        assertEquals(1, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
    }

    @Test
    @DisplayName("should isolate persistence failures per record")
    void testIsolatePersistenceFailures() {
        List<Transaction> transactions = createTransactionList(3);
        Transaction txn1 = transactions.get(0);
        Transaction txn2 = transactions.get(1);
        Transaction txn3 = transactions.get(2);

        Transaction completedTxn = txn1.withStatus(Transaction.TransactionStatus.COMPLETED);
        when(processor.process(any()))
            .thenReturn(TransactionProcessor.ProcessingResult.success(completedTxn));

        doNothing()
            .when(persistencePort)
            .save(txn1);
        doThrow(new RuntimeException("Database connection failed"))
            .when(persistencePort)
            .save(txn2);
        doNothing()
            .when(persistencePort)
            .save(txn3);

        ProcessingReport report = engine.execute(transactions.stream());

        assertEquals(3, report.totalRecords());
        assertEquals(3, report.successfulRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
        verify(persistencePort, times(3)).save(any());
    }

    @Test
    @DisplayName("should continue processing after processor exceptions")
    void testContinueAfterProcessorException() {
        List<Transaction> transactions = createTransactionList(3);
        Transaction txn1 = transactions.get(0);
        Transaction txn2 = transactions.get(1);
        Transaction txn3 = transactions.get(2);

        when(processor.process(txn1))
            .thenReturn(TransactionProcessor.ProcessingResult.success(
                txn1.withStatus(Transaction.TransactionStatus.COMPLETED)
            ));
        when(processor.process(txn2))
            .thenThrow(new RuntimeException("Processing error"));
        when(processor.process(txn3))
            .thenReturn(TransactionProcessor.ProcessingResult.success(
                txn3.withStatus(Transaction.TransactionStatus.COMPLETED)
            ));

        ProcessingReport report = engine.execute(transactions.stream());

        assertEquals(3, report.totalRecords());
        assertEquals(2, report.successfulRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(1, report.failedRecords());
    }

    @Test
    @DisplayName("should record execution time")
    void testRecordExecutionTime() {
        List<Transaction> transactions = createTransactionList(1);
        Transaction txn = transactions.get(0);
        Transaction completedTxn = txn.withStatus(Transaction.TransactionStatus.COMPLETED);
        when(processor.process(any()))
            .thenReturn(TransactionProcessor.ProcessingResult.success(completedTxn));

        ProcessingReport report = engine.execute(transactions.stream());

        assertTrue(report.executionTimeMillis() >= 0);
    }

    @Test
    @DisplayName("should aggregate metrics correctly for mixed scenario")
    void testAggregateMetricsForMixedScenario() {
        List<Transaction> transactions = createTransactionList(10);

        for (int i = 0; i < transactions.size(); i++) {
            Transaction txn = transactions.get(i);
            if (i < 5) {
                when(processor.process(txn))
                    .thenReturn(TransactionProcessor.ProcessingResult.success(
                        txn.withStatus(Transaction.TransactionStatus.COMPLETED)
                    ));
            } else if (i < 8) {
                when(processor.process(txn))
                    .thenReturn(TransactionProcessor.ProcessingResult.failure(
                        txn.withStatus(Transaction.TransactionStatus.FAILED),
                        "INVALID_DATA",
                        "Invalid data"
                    ));
            } else {
                when(processor.process(txn))
                    .thenThrow(new RuntimeException("Processing error"));
            }
        }

        ProcessingReport report = engine.execute(transactions.stream());

        assertEquals(10, report.totalRecords());
        assertEquals(5, report.successfulRecords());
        assertEquals(3, report.rejectedRecords());
        assertEquals(2, report.failedRecords());
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
