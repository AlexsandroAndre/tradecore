package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

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
        assertEquals(VALIDATION_CODE_INVALID_SOURCE, result.validationCode());
        assertEquals(REJECTED_RULE_SOURCE_RULE, result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldRejectEmptySource() {
        Transaction transaction = transactionBuilder.buildWithEmptySource();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_SOURCE, result.validationCode());
        assertEquals(REJECTED_RULE_SOURCE_RULE, result.rejectedRule());
    }

    @Test
    void shouldRejectBlankSource() {
        Transaction transaction = transactionBuilder.buildWithBlankSource();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_SOURCE, result.validationCode());
        assertEquals(REJECTED_RULE_SOURCE_RULE, result.rejectedRule());
    }

    @Test
    void shouldAcceptSourceWithSpecialCharacters() {
        Transaction transaction = transactionBuilder
            .withSource(SOURCE_EXTERNAL_BANK_001)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptSourceWithNumbers() {
        Transaction transaction = transactionBuilder
            .withSource(SOURCE_123)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptSourceWithUnderscores() {
        Transaction transaction = transactionBuilder
            .withSource(SOURCE_IMPORT_SYSTEM)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }
}