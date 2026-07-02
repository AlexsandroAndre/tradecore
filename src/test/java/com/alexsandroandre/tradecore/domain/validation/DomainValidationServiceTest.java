package com.alexsandroandre.tradecore.domain.validation;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainValidationServiceTest {

    private DomainValidationService service;

    @BeforeEach
    void setUp() {
        service = new DomainValidationService();
    }

    @Test
    void shouldAcceptValidTransaction() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectTransactionWithNullTransactionId() {
        Transaction transaction = new Transaction(
            null,
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TRANSACTION_ID", result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithNullAccountId() {
        Transaction transaction = new Transaction(
            "TXN-001",
            null,
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_ACCOUNT_ID", result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithNegativeAmount() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("-100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_AMOUNT", result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithZeroAmount() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            BigDecimal.ZERO,
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_AMOUNT", result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithUnsupportedCurrency() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "XYZ",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("UNSUPPORTED_CURRENCY", result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithFutureTimestamp() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().plusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TIMESTAMP", result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithNullSource() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            null,
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_SOURCE", result.validationCode());
    }

    @Test
    void shouldDetectDuplicateTransactions() {
        Transaction transaction1 = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result1 = service.validate(transaction1);
        assertTrue(result1.isSuccess());

        Transaction transaction2 = new Transaction(
            "TXN-001",
            "ACC-456",
            new BigDecimal("200.00"),
            "EUR",
            OffsetDateTime.now().minusHours(2),
            "another-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result2 = service.validate(transaction2);
        assertTrue(result2.isFailure());
        assertEquals("DUPLICATED_TRANSACTION", result2.validationCode());
    }

    @Test
    void shouldValidateBatchOfTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        ));
        transactions.add(new Transaction(
            "TXN-002",
            "ACC-456",
            new BigDecimal("200.00"),
            "EUR",
            OffsetDateTime.now().minusHours(2),
            "another-bank",
            Transaction.TransactionStatus.PENDING
        ));
        transactions.add(new Transaction(
            "TXN-003",
            "ACC-789",
            new BigDecimal("300.00"),
            "GBP",
            OffsetDateTime.now().minusHours(3),
            "third-bank",
            Transaction.TransactionStatus.PENDING
        ));

        List<DomainValidationResult> results = service.validateBatch(transactions);

        assertEquals(3, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertTrue(results.get(2).isSuccess());
    }

    @Test
    void shouldValidateBatchWithSomeInvalidTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        ));
        transactions.add(new Transaction(
            "TXN-002",
            "ACC-456",
            BigDecimal.ZERO,
            "EUR",
            OffsetDateTime.now().minusHours(2),
            "another-bank",
            Transaction.TransactionStatus.PENDING
        ));
        transactions.add(new Transaction(
            "TXN-003",
            "ACC-789",
            new BigDecimal("300.00"),
            "XYZ",
            OffsetDateTime.now().minusHours(3),
            "third-bank",
            Transaction.TransactionStatus.PENDING
        ));

        List<DomainValidationResult> results = service.validateBatch(transactions);

        assertEquals(3, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isFailure());
        assertEquals("INVALID_AMOUNT", results.get(1).validationCode());
        assertTrue(results.get(2).isFailure());
        assertEquals("UNSUPPORTED_CURRENCY", results.get(2).validationCode());
    }

    @Test
    void shouldAllowMultipleValidTransactionsWithDifferentIds() {
        Transaction transaction1 = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result1 = service.validate(transaction1);
        assertTrue(result1.isSuccess());

        Transaction transaction2 = new Transaction(
            "TXN-002",
            "ACC-456",
            new BigDecimal("200.00"),
            "EUR",
            OffsetDateTime.now().minusHours(2),
            "another-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result2 = service.validate(transaction2);
        assertTrue(result2.isSuccess());

        Transaction transaction3 = new Transaction(
            "TXN-003",
            "ACC-789",
            new BigDecimal("300.00"),
            "GBP",
            OffsetDateTime.now().minusHours(3),
            "third-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result3 = service.validate(transaction3);
        assertTrue(result3.isSuccess());
    }
}
