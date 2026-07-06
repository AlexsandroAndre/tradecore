package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TimestampRuleTest {

    private TimestampRule rule;
    private TransactionTestBuilder transactionBuilder;

    @BeforeEach
    void setUp() {
        rule = new TimestampRule();
        transactionBuilder = new TransactionTestBuilder();
    }

    @Test
    void shouldAcceptValidPastTimestamp() {
        Transaction transaction = transactionBuilder.build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectNullTimestamp() {
        Transaction transaction = transactionBuilder.buildWithNullTimestamp();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals("INVALID_TIMESTAMP", result.validationCode());
        assertEquals("TIMESTAMP_RULE", result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldRejectFutureTimestamp() {
        Transaction transaction = transactionBuilder.buildWithFutureTimestamp();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TIMESTAMP", result.validationCode());
        assertEquals("TIMESTAMP_RULE", result.rejectedRule());
    }

    @Test
    void shouldAcceptTimestampFromOneHourAgo() {
        Transaction transaction = transactionBuilder
            .withTimestamp(OffsetDateTime.now().minusHours(1))
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptTimestampFromYesterday() {
        Transaction transaction = transactionBuilder.buildWithPastTimestamp();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptVeryOldTimestamp() {
        Transaction transaction = transactionBuilder
            .withTimestamp(OffsetDateTime.now().minusDays(365))
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptCurrentTimestamp() {
        Transaction transaction = transactionBuilder
            .withTimestamp(OffsetDateTime.now())
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }
}
