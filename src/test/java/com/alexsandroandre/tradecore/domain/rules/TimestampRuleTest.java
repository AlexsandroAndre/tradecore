package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimestampRuleTest {

    private TimestampRule rule;

    @BeforeEach
    void setUp() {
        rule = new TimestampRule();
    }

    @Test
    void shouldAcceptValidPastTimestamp() {
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
    void shouldAcceptValidPastTimestampDaysAgo() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusDays(30),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptValidCurrentTimestamp() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now(),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectNullTimestamp() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            null,
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TIMESTAMP", result.validationCode());
        assertEquals("TIMESTAMP_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectFutureTimestamp() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().plusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TIMESTAMP", result.validationCode());
    }

    @Test
    void shouldRejectFutureTimestampInTheFar() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "ACC-123",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().plusDays(100),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TIMESTAMP", result.validationCode());
    }
}
