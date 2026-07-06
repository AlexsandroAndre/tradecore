package com.alexsandroandre.tradecore.application.processor;

import static org.junit.jupiter.api.Assertions.*;
import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

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
            TRANSACTION_ID_TXN_001,
            VALID_ACCOUNT_ID,
            AMOUNT_100,
            VALID_CURRENCY,
            OffsetDateTime.now(),
            SOURCE_SYSTEM_A,
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
            TRANSACTION_ID_TXN_002,
            ANOTHER_ACCOUNT_ID,
            AMOUNT_NEGATIVE_100,
            VALID_CURRENCY,
            OffsetDateTime.now(),
            SOURCE_SYSTEM_A,
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
            TRANSACTION_ID_TXN_003,
            ANOTHER_ACCOUNT_ID_ACC_003,
            AMOUNT_100,
            CURRENCY_INVALID,
            OffsetDateTime.now(),
            SOURCE_SYSTEM_A,
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
            TRANSACTION_ID_TXN_004,
            ANOTHER_ACCOUNT_ID_ACC_004,
            AMOUNT_250_50,
            CURRENCY_EUR,
            OffsetDateTime.now(),
            SOURCE_SYSTEM_B,
            Transaction.TransactionStatus.PENDING
        );

        TransactionProcessor.ProcessingResult result = processor.process(transaction);

        assertEquals(transaction.transactionId(), result.transaction().transactionId());
        assertEquals(transaction.accountId(), result.transaction().accountId());
        assertEquals(transaction.amount(), result.transaction().amount());
        assertEquals(transaction.currency(), result.transaction().currency());
    }
}