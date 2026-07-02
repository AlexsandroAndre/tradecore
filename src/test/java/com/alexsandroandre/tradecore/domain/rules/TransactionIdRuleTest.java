package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdRuleTest {

    private TransactionIdRule rule;

    @BeforeEach
    void setUp() {
        rule = new TransactionIdRule();
    }

    @Test
    void shouldAcceptValidTransactionId() {
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
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectNullTransactionId() {
        Transaction transaction = new Transaction(
            null,
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TRANSACTION_ID", result.validationCode());
        assertEquals("TRANSACTION_ID_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectEmptyTransactionId() {
        Transaction transaction = new Transaction(
            "",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TRANSACTION_ID", result.validationCode());
    }

    @Test
    void shouldRejectBlankTransactionId() {
        Transaction transaction = new Transaction(
            "   ",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TRANSACTION_ID", result.validationCode());
    }
}
