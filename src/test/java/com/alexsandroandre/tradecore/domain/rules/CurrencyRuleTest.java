package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

class CurrencyRuleTest {

    private CurrencyRule rule;
    private TransactionTestBuilder transactionBuilder;

    @BeforeEach
    void setUp() {
        rule = new CurrencyRule();
        transactionBuilder = new TransactionTestBuilder();
    }

    @Test
    void shouldAcceptValidSupportedCurrency() {
        Transaction transaction = transactionBuilder.build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectNullCurrency() {
        Transaction transaction = transactionBuilder.buildWithNullCurrency();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals(VALIDATION_CODE_UNSUPPORTED_CURRENCY, result.validationCode());
        assertEquals(REJECTED_RULE_CURRENCY_RULE, result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldRejectUnsupportedCurrency() {
        Transaction transaction = transactionBuilder.buildWithUnsupportedCurrency();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_UNSUPPORTED_CURRENCY, result.validationCode());
        assertEquals(REJECTED_RULE_CURRENCY_RULE, result.rejectedRule());
    }

    @Test
    void shouldRejectInvalidCurrencyFormat() {
        Transaction transaction = transactionBuilder.buildWithInvalidCurrencyFormat();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_UNSUPPORTED_CURRENCY, result.validationCode());
        assertEquals(REJECTED_RULE_CURRENCY_RULE, result.rejectedRule());
    }

    @Test
    void shouldAcceptLowercaseCurrencyAndConvertToUppercase() {
        Transaction transaction = transactionBuilder.buildWithLowercaseCurrency();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptEuroCurrency() {
        Transaction transaction = transactionBuilder
            .withCurrency(CURRENCY_EUR)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptGBPCurrency() {
        Transaction transaction = transactionBuilder
            .withCurrency(CURRENCY_GBP)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptJPYCurrency() {
        Transaction transaction = transactionBuilder
            .withCurrency(CURRENCY_JPY)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectTwoLetterCurrency() {
        Transaction transaction = transactionBuilder
            .withCurrency(CURRENCY_TWO_LETTER)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_UNSUPPORTED_CURRENCY, result.validationCode());
    }

    @Test
    void shouldRejectEmptyCurrency() {
        Transaction transaction = transactionBuilder
            .withCurrency(CURRENCY_EMPTY)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_UNSUPPORTED_CURRENCY, result.validationCode());
    }
}