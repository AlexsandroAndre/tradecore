package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateTransactionRuleTest {

    private DuplicateTransactionRule rule;
    private Set<String> processedIds;

    @BeforeEach
    void setUp() {
        processedIds = new HashSet<>();
        rule = new DuplicateTransactionRule(processedIds);
    }

    @Test
    void shouldAcceptFirstOccurrenceOfTransaction() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectDuplicateTransaction() {
        String transactionId = "TXN-001";
        processedIds.add(transactionId);

        Transaction transaction = new Transaction(
            transactionId,
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("DUPLICATED_TRANSACTION", result.validationCode());
        assertEquals("DUPLICATE_TRANSACTION_RULE", result.rejectedRule());
    }

    @Test
    void shouldTrackProcessedTransactionIds() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        rule.markAsProcessed(transaction.transactionId());

        assertTrue(processedIds.contains("TXN-001"));
    }

    @Test
    void shouldRejectMultipleDuplicates() {
        String transactionId = "TXN-001";
        processedIds.add(transactionId);

        Transaction transaction1 = new Transaction(
            transactionId,
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result1 = rule.validate(transaction1);
        assertTrue(result1.isFailure());

        Transaction transaction2 = new Transaction(
            transactionId,
            "ACC-456",
            new BigDecimal("200.00"),
            "EUR",
            OffsetDateTime.now().minusHours(2),
            "another-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result2 = rule.validate(transaction2);
        assertTrue(result2.isFailure());
    }
}
