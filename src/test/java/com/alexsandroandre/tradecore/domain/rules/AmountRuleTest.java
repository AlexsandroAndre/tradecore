package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AmountRuleTest {

    private AmountRule rule;

    @BeforeEach
    void setUp() {
        rule = new AmountRule();
    }

    @Test
    void shouldAcceptValidAmount() {
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
    void shouldAcceptLargeAmount() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("999999.99"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptSmallAmount() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("0.01"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectNullAmount() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            null,
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_AMOUNT", result.validationCode());
        assertEquals("AMOUNT_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectZeroAmount() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            BigDecimal.ZERO,
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_AMOUNT", result.validationCode());
    }

    @Test
    void shouldRejectNegativeAmount() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("-50.00"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_AMOUNT", result.validationCode());
    }
}
