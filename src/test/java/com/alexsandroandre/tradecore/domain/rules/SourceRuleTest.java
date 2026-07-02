package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SourceRuleTest {

    private SourceRule rule;

    @BeforeEach
    void setUp() {
        rule = new SourceRule();
    }

    @Test
    void shouldAcceptValidSource() {
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
    void shouldAcceptValidSourceWithDashes() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "bank-api",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptValidSourceWithUnderscores() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external_system",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectNullSource() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            null,
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_SOURCE", result.validationCode());
        assertEquals("SOURCE_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectEmptySource() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_SOURCE", result.validationCode());
    }

    @Test
    void shouldRejectBlankSource() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "   ",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_SOURCE", result.validationCode());
    }
}
