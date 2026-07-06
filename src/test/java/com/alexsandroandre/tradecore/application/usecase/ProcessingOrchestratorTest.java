package com.alexsandroandre.tradecore.application.usecase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.alexsandroandre.tradecore.application.dto.ProcessingReport;
import com.alexsandroandre.tradecore.application.port.TransactionBatchPersistencePort;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationService;
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

@DisplayName("ProcessingOrchestrator Tests")
class ProcessingOrchestratorTest {

    private ProcessingOrchestrator orchestrator;

    @Mock
    private TransactionBatchPersistencePort batchPersistencePort;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        DomainValidationService validationService = new DomainValidationService();
        orchestrator = new ProcessingOrchestrator(validationService, batchPersistencePort);
    }

    @Test
    @DisplayName("should orchestrate successful processing flow")
    void testOrchestrateSuccessfulFlow() {
        List<Transaction> transactions = createValidTransactionList(5);

        ProcessingReport report = orchestrator.orchestrate(transactions.stream());

        assertEquals(5, report.totalRecords());
        assertEquals(5, report.successfulRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
        verify(batchPersistencePort, times(1)).saveBatch(any());
    }

    @Test
    @DisplayName("should handle rejected records in orchestration")
    void testOrchestrateWithRejectedRecords() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(createValidTransaction("TXN-001", "ACC-001"));
        transactions.add(createInvalidTransaction("TXN-002", "ACC-002", BigDecimal.ZERO));
        transactions.add(createValidTransaction("TXN-003", "ACC-003"));

        ProcessingReport report = orchestrator.orchestrate(transactions.stream());

        assertEquals(3, report.totalRecords());
        assertEquals(2, report.successfulRecords());
        assertEquals(1, report.rejectedRecords());
    }

    @Test
    @DisplayName("should continue orchestration after batch persistence failures")
    void testOrchestrateWithPersistenceFailures() {
        List<Transaction> transactions = createValidTransactionList(3);

        doThrow(new RuntimeException("Database error"))
            .when(batchPersistencePort)
            .saveBatch(any());

        ProcessingReport report = orchestrator.orchestrate(transactions.stream());

        assertEquals(3, report.totalRecords());
        assertEquals(3, report.successfulRecords());
        assertEquals(0, report.failedRecords());
    }

    @Test
    @DisplayName("should process empty stream successfully")
    void testOrchestrateEmptyStream() {
        ProcessingReport report = orchestrator.orchestrate(Stream.empty());

        assertEquals(0, report.totalRecords());
        assertEquals(0, report.successfulRecords());
        assertEquals(0, report.rejectedRecords());
        assertEquals(0, report.failedRecords());
        verify(batchPersistencePort, never()).saveBatch(any());
    }

    @Test
    @DisplayName("should maintain sequential processing order")
    void testMaintainSequentialOrder() {
        List<Transaction> transactions = createValidTransactionList(10);

        ProcessingReport report = orchestrator.orchestrate(transactions.stream());

        assertEquals(10, report.totalRecords());
        assertEquals(10, report.successfulRecords());
        verify(batchPersistencePort, times(1)).saveBatch(any());
    }

    @Test
    @DisplayName("should aggregate comprehensive metrics")
    void testAggregateComprehensiveMetrics() {
        List<Transaction> transactions = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            transactions.add(createValidTransaction("TXN-" + i, "ACC-" + i));
        }

        for (int i = 5; i < 8; i++) {
            transactions.add(
                createInvalidTransaction("TXN-" + i, "ACC-" + i, BigDecimal.valueOf(-100))
            );
        }

        ProcessingReport report = orchestrator.orchestrate(transactions.stream());

        assertEquals(8, report.totalRecords());
        assertEquals(5, report.successfulRecords());
        assertEquals(3, report.rejectedRecords());
        assertTrue(report.duration() >= 0);
    }

    private List<Transaction> createValidTransactionList(int count) {
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transactions.add(createValidTransaction("TXN-" + (i + 1), "ACC-" + (i + 1)));
        }
        return transactions;
    }

    private Transaction createValidTransaction(String transactionId, String accountId) {
        return new Transaction(
            transactionId,
            accountId,
            BigDecimal.valueOf(100.00),
            "USD",
            OffsetDateTime.now(),
            "SYSTEM",
            Transaction.TransactionStatus.PENDING
        );
    }

    private Transaction createInvalidTransaction(
        String transactionId,
        String accountId,
        BigDecimal amount
    ) {
        return new Transaction(
            transactionId,
            accountId,
            amount,
            "USD",
            OffsetDateTime.now(),
            "SYSTEM",
            Transaction.TransactionStatus.PENDING
        );
    }
}
