package com.alexsandroandre.tradecore.application.processor;

import static org.junit.jupiter.api.Assertions.*;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationService;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecordProcessor Tests")
class RecordProcessorTest {

    private RecordProcessor processor;
    private DomainValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new DomainValidationService();
        processor = new RecordProcessor(validationService);
    }

    @Test
    @DisplayName("should process valid transaction successfully")
    void testProcessValidTransaction() {
        Transaction validTransaction = new Transaction(
            "TXN-001",
            "ACC-001",
            BigDecimal.valueOf(100.00),
            "USD",
            OffsetDateTime.now(),
            "SYSTEM-A",
            Transaction.TransactionStatus.PENDING
        );

        TransactionProcessor.ProcessingResult result = processor.process(validTransaction);

        assertTrue(result.success());
        assertEquals(Transaction.TransactionStatus.COMPLETED, result.transaction().status());
        assertNull(result.errorMessage());
        assertNull(result.errorCode());
    }

    @Test
    @DisplayName("should fail processing invalid amount transaction")
    void testProcessInvalidAmountTransaction() {
        Transaction invalidTransaction = new Transaction(
            "TXN-002",
            "ACC-002",
            BigDecimal.valueOf(-100.00),
            "USD",
            OffsetDateTime.now(),
            "SYSTEM-A",
            Transaction.TransactionStatus.PENDING
        );

        TransactionProcessor.ProcessingResult result = processor.process(invalidTransaction);

        assertFalse(result.success());
        assertEquals(Transaction.TransactionStatus.FAILED, result.transaction().status());
        assertNotNull(result.errorMessage());
        assertNotNull(result.errorCode());
    }

    @Test
    @DisplayName("should fail processing invalid currency transaction")
    void testProcessInvalidCurrencyTransaction() {
        Transaction invalidTransaction = new Transaction(
            "TXN-003",
            "ACC-003",
            BigDecimal.valueOf(100.00),
            "INVALID",
            OffsetDateTime.now(),
            "SYSTEM-A",
            Transaction.TransactionStatus.PENDING
        );

        TransactionProcessor.ProcessingResult result = processor.process(invalidTransaction);

        assertFalse(result.success());
        assertEquals(Transaction.TransactionStatus.FAILED, result.transaction().status());
    }

    @Test
    @DisplayName("should preserve transaction data in result")
    void testPreserveTransactionData() {
        Transaction transaction = new Transaction(
            "TXN-004",
            "ACC-004",
            BigDecimal.valueOf(250.50),
            "EUR",
            OffsetDateTime.now(),
            "SYSTEM-B",
            Transaction.TransactionStatus.PENDING
        );

        TransactionProcessor.ProcessingResult result = processor.process(transaction);

        assertEquals(transaction.transactionId(), result.transaction().transactionId());
        assertEquals(transaction.accountId(), result.transaction().accountId());
        assertEquals(transaction.amount(), result.transaction().amount());
        assertEquals(transaction.currency(), result.transaction().currency());
    }
}
