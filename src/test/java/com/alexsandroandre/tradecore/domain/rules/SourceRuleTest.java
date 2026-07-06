package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SourceRuleTest {

    private SourceRule rule;
    private TransactionTestBuilder transactionBuilder;

    @BeforeEach
    void setUp() {
        rule = new SourceRule();
        transactionBuilder = new TransactionTestBuilder();
    }

    @Test
    void shouldAcceptValidSource() {
        Transaction transaction = transactionBuilder.build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectNullSource() {
        Transaction transaction = transactionBuilder.buildWithNullSource();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals("INVALID_SOURCE", result.validationCode());
        assertEquals("SOURCE_RULE", result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldRejectEmptySource() {
        Transaction transaction = transactionBuilder.buildWithEmptySource();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_SOURCE", result.validationCode());
        assertEquals("SOURCE_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectBlankSource() {
        Transaction transaction = transactionBuilder.buildWithBlankSource();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_SOURCE", result.validationCode());
        assertEquals("SOURCE_RULE", result.rejectedRule());
    }

    @Test
    void shouldAcceptSourceWithSpecialCharacters() {
        Transaction transaction = transactionBuilder
            .withSource("external-bank-001")
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptSourceWithNumbers() {
        Transaction transaction = transactionBuilder
            .withSource("SOURCE123")
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptSourceWithUnderscores() {
        Transaction transaction = transactionBuilder
            .withSource("IMPORT_SYSTEM")
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }
}
