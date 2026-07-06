package com.alexsandroandre.tradecore.application.processor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.alexsandroandre.tradecore.application.port.TransactionBatchPersistencePort;
import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.domain.model.BatchProcessingResult;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

@DisplayName("StandardBatchProcessor Tests")
class StandardBatchProcessorTest {

    private StandardBatchProcessor batchProcessor;

    @Mock
    private TransactionBatchPersistencePort persistencePort;

    @Mock
    private TransactionProcessor transactionProcessor;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        batchProcessor = new StandardBatchProcessor(
            1000,
            persistencePort,
            transactionProcessor
        );
    }

    @Test
    @DisplayName("should group transactions into fixed-size batches")
    void testGroupIntoBatches() {
        List<Transaction> transactions = createTransactionList(2500);

        List<Batch> batches = batchProcessor.groupIntoBatches(transactions);

        assertEquals(3, batches.size());
        assertEquals(1000, batches.get(0).size());
        assertEquals(1000, batches.get(1).size());
        assertEquals(500, batches.get(2).size());
    }

    @Test
    @DisplayName("should handle empty transaction list")
    void testGroupEmptyTransactionList() {
        List<Transaction> transactions = new ArrayList<>();

        List<Batch> batches = batchProcessor.groupIntoBatches(transactions);

        assertEquals(0, batches.size());
    }

    @Test
    @DisplayName("should group transactions exactly matching batch size")
    void testGroupExactBatchSize() {
        List<Transaction> transactions = createTransactionList(2000);

        List<Batch> batches = batchProcessor.groupIntoBatches(transactions);

        assertEquals(2, batches.size());
        assertEquals(1000, batches.get(0).size());
        assertEquals(1000, batches.get(1).size());
    }

    @Test
    @DisplayName("should group transactions with single incomplete batch")
    void testGroupSingleIncompleteBatch() {
        List<Transaction> transactions = createTransactionList(500);

        List<Batch> batches = batchProcessor.groupIntoBatches(transactions);

        assertEquals(1, batches.size());
        assertEquals(500, batches.get(0).size());
    }

    @Test
    @DisplayName("should execute batch successfully with all valid transactions")
    void testExecuteBatchSuccess() {
        List<Transaction> transactions = createTransactionList(5);
        Batch batch = new Batch(BATCH_ID_BATCH_1, transactions, 1000);

        for (Transaction transaction : transactions) {
            when(transactionProcessor.process(transaction))
                .thenReturn(TransactionProcessor.ProcessingResult.success(
                    transaction.asCompleted()
                ));
        }

        BatchProcessingResult result = batchProcessor.executeBatch(batch);

        assertEquals(BATCH_ID_BATCH_1, result.batchId());
        assertEquals(BatchProcessingResult.BatchStatus.SUCCESS, result.status());
        assertEquals(5, result.processedCount());
        assertEquals(0, result.failedCount());
        assertTrue(result.isSuccess());
        assertFalse(result.hasErrors());
        verify(persistencePort, times(1)).saveBatch(any());
    }

    @Test
    @DisplayName("should handle partial batch failure")
    void testExecuteBatchPartialFailure() {
        List<Transaction> transactions = createTransactionList(5);
        Batch batch = new Batch(BATCH_ID_BATCH_1, transactions, 1000);

        Transaction txn1 = transactions.get(0);
        Transaction txn2 = transactions.get(1);
        Transaction txn3 = transactions.get(2);
        Transaction txn4 = transactions.get(3);
        Transaction txn5 = transactions.get(4);

        when(transactionProcessor.process(txn1))
            .thenReturn(TransactionProcessor.ProcessingResult.success(txn1.asCompleted()));
        when(transactionProcessor.process(txn2))
            .thenReturn(TransactionProcessor.ProcessingResult.failure(
                txn2.asFailed(),
                VALIDATION_CODE_INVALID_AMOUNT,
                ERROR_MESSAGE_AMOUNT_MUST_BE_POSITIVE
            ));
        when(transactionProcessor.process(txn3))
            .thenReturn(TransactionProcessor.ProcessingResult.success(txn3.asCompleted()));
        when(transactionProcessor.process(txn4))
            .thenThrow(new RuntimeException(ERROR_MESSAGE_PROCESSING_ERROR));
        when(transactionProcessor.process(txn5))
            .thenReturn(TransactionProcessor.ProcessingResult.success(txn5.asCompleted()));

        BatchProcessingResult result = batchProcessor.executeBatch(batch);

        assertEquals(BATCH_ID_BATCH_1, result.batchId());
        assertEquals(BatchProcessingResult.BatchStatus.PARTIAL_FAILURE, result.status());
        assertEquals(3, result.processedCount());
        assertEquals(1, result.rejectedCount());
        assertEquals(1, result.failedCount());
        assertTrue(result.hasErrors());
        assertEquals(2, result.getErrors().size());
    }

    @Test
    @DisplayName("should detect duplicate transaction IDs in batch")
    void testDetectDuplicateTransactionIds() {
        List<Transaction> transactions = new ArrayList<>();
        Transaction txn1 = new Transaction(
            TRANSACTION_ID_TXN_1,
            ACCOUNT_ID_ACC_1,
            AMOUNT_100,
            VALID_CURRENCY,
            OffsetDateTime.now(),
            SYSTEM_SOURCE,
            Transaction.TransactionStatus.PENDING
        );
        Transaction txn2 = new Transaction(
            TRANSACTION_ID_TXN_1,
            ACCOUNT_ID_ACC_2,
            AMOUNT_200,
            VALID_CURRENCY,
            OffsetDateTime.now(),
            SYSTEM_SOURCE,
            Transaction.TransactionStatus.PENDING
        );
        transactions.add(txn1);
        transactions.add(txn2);

        Batch batch = new Batch(BATCH_ID_BATCH_1, transactions, 1000);

        BatchProcessingResult result = batchProcessor.executeBatch(batch);

        assertEquals(BATCH_ID_BATCH_1, result.batchId());
        assertEquals(BatchProcessingResult.BatchStatus.PARTIAL_FAILURE, result.status());
        assertTrue(result.hasErrors());
        assertEquals(ERROR_CODE_DUPLICATED_TRANSACTION_IN_BATCH, result.getErrors().getFirst().errorCode());
    }

    @Test
    @DisplayName("should execute multiple batches and aggregate results")
    void testExecuteMultipleBatches() {
        List<Batch> batches = new ArrayList<>();
        List<Transaction> batch1Txns = createTransactionList(3);
        List<Transaction> batch2Txns = createTransactionList(3);

        batches.add(new Batch(BATCH_ID_BATCH_1, batch1Txns, 1000));
        batches.add(new Batch(BATCH_ID_BATCH_2, batch2Txns, 1000));

        for (Transaction transaction : batch1Txns) {
            when(transactionProcessor.process(transaction))
                .thenReturn(TransactionProcessor.ProcessingResult.success(
                    transaction.asCompleted()
                ));
        }

        for (Transaction transaction : batch2Txns) {
            when(transactionProcessor.process(transaction))
                .thenReturn(TransactionProcessor.ProcessingResult.success(
                    transaction.asCompleted()
                ));
        }

        List<BatchProcessingResult> results = batchProcessor.executeBatches(batches);

        assertEquals(2, results.size());
        assertEquals(BatchProcessingResult.BatchStatus.SUCCESS, results.get(0).status());
        assertEquals(BatchProcessingResult.BatchStatus.SUCCESS, results.get(1).status());
        assertEquals(3, results.get(0).processedCount());
        assertEquals(3, results.get(1).processedCount());
    }

    @Test
    @DisplayName("should handle empty batch")
    void testExecuteEmptyBatch() {
        Batch emptyBatch = new Batch(BATCH_ID_BATCH_1, new ArrayList<>(), 1000);

        BatchProcessingResult result = batchProcessor.executeBatch(emptyBatch);

        assertEquals(0, result.processedCount());
    }

    @Test
    @DisplayName("should isolate failures per record without stopping processing")
    void testIsolateFailuresPerRecord() {
        List<Transaction> transactions = createTransactionList(5);
        Batch batch = new Batch(BATCH_ID_BATCH_1, transactions, 1000);

        for (int i = 0; i < transactions.size(); i++) {
            Transaction txn = transactions.get(i);
            if (i % 2 == 0) {
                when(transactionProcessor.process(txn))
                    .thenReturn(TransactionProcessor.ProcessingResult.success(txn.asCompleted()));
            } else {
                when(transactionProcessor.process(txn))
                    .thenThrow(new RuntimeException(ERROR_MESSAGE_DATABASE_ERROR));
            }
        }

        BatchProcessingResult result = batchProcessor.executeBatch(batch);

        assertEquals(3, result.processedCount());
        assertEquals(2, result.failedCount());
        assertTrue(result.hasErrors());
    }

    @Test
    @DisplayName("should record execution time")
    void testRecordExecutionTime() {
        List<Transaction> transactions = createTransactionList(2);
        Batch batch = new Batch(BATCH_ID_BATCH_1, transactions, 1000);

        for (Transaction transaction : transactions) {
            when(transactionProcessor.process(transaction))
                .thenReturn(TransactionProcessor.ProcessingResult.success(
                    transaction.asCompleted()
                ));
        }

        BatchProcessingResult result = batchProcessor.executeBatch(batch);

        assertTrue(result.executionTimeMillis() >= 0);
    }

    @Test
    @DisplayName("should aggregate errors correctly")
    void testAggregateErrorsCorrectly() {
        List<Transaction> transactions = createTransactionList(3);
        Batch batch = new Batch(BATCH_ID_BATCH_1, transactions, 1000);

        Transaction txn1 = transactions.get(0);
        Transaction txn2 = transactions.get(1);
        Transaction txn3 = transactions.get(2);

        when(transactionProcessor.process(txn1))
            .thenReturn(TransactionProcessor.ProcessingResult.failure(
                txn1.asFailed(),
                VALIDATION_CODE_INVALID_AMOUNT,
                ERROR_MESSAGE_AMOUNT_MUST_BE_POSITIVE
            ));
        when(transactionProcessor.process(txn2))
            .thenThrow(new RuntimeException(ERROR_MESSAGE_CONNECTION_TIMEOUT));
        when(transactionProcessor.process(txn3))
            .thenReturn(TransactionProcessor.ProcessingResult.success(txn3.asCompleted()));

        BatchProcessingResult result = batchProcessor.executeBatch(batch);

        assertEquals(2, result.getErrors().size());
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> ERROR_CODE_VALIDATION_FAILURE.equals(e.errorCode())));
        assertTrue(result.getErrors().stream()
            .anyMatch(e -> ERROR_CODE_PROCESSING_ERROR.equals(e.errorCode())));
    }

    private List<Transaction> createTransactionList(int count) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transactions.add(
                new Transaction(
                    "TXN-" + (i + 1),
                    "ACC-" + (i + 1),
                    AMOUNT_100.add(BigDecimal.valueOf(i)),
                    VALID_CURRENCY,
                    OffsetDateTime.now(),
                    SYSTEM_SOURCE,
                    Transaction.TransactionStatus.PENDING
                )
            );
        }
        return transactions;
    }
}
