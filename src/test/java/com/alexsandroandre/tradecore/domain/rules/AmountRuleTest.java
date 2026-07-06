package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AmountRuleTest {

    private AmountRule rule;
    private TransactionTestBuilder transactionBuilder;

    @BeforeEach
    void setUp() {
        rule = new AmountRule();
        transactionBuilder = new TransactionTestBuilder();
    }

    @Test
    void shouldAcceptValidPositiveAmount() {
        Transaction transaction = transactionBuilder.buildWithPositiveAmount();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectNullAmount() {
        Transaction transaction = transactionBuilder.buildWithNullAmount();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals("INVALID_AMOUNT", result.validationCode());
        assertEquals("AMOUNT_RULE", result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldRejectZeroAmount() {
        Transaction transaction = transactionBuilder.buildWithZeroAmount();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_AMOUNT", result.validationCode());
        assertEquals("AMOUNT_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectNegativeAmount() {
        Transaction transaction = transactionBuilder.buildWithNegativeAmount();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_AMOUNT", result.validationCode());
        assertEquals("AMOUNT_RULE", result.rejectedRule());
    }

    @Test
    void shouldAcceptSmallPositiveAmount() {
        Transaction transaction = transactionBuilder
            .withAmount(new BigDecimal("0.01"))
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptLargePositiveAmount() {
        Transaction transaction = transactionBuilder
            .withAmount(new BigDecimal("999999999.99"))
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }
}
