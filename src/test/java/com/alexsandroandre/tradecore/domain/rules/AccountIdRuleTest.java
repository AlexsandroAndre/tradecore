package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountIdRuleTest {

    private AccountIdRule rule;
    private TransactionTestBuilder transactionBuilder;

    @BeforeEach
    void setUp() {
        rule = new AccountIdRule();
        transactionBuilder = new TransactionTestBuilder();
    }

    @Test
    void shouldAcceptValidAccountId() {
        Transaction transaction = transactionBuilder.build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectNullAccountId() {
        Transaction transaction = transactionBuilder.buildWithNullAccountId();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals("INVALID_ACCOUNT_ID", result.validationCode());
        assertEquals("ACCOUNT_ID_RULE", result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldRejectEmptyAccountId() {
        Transaction transaction = transactionBuilder.buildWithEmptyAccountId();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_ACCOUNT_ID", result.validationCode());
        assertEquals("ACCOUNT_ID_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectBlankAccountId() {
        Transaction transaction = transactionBuilder.buildWithBlankAccountId();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_ACCOUNT_ID", result.validationCode());
        assertEquals("ACCOUNT_ID_RULE", result.rejectedRule());
    }

    @Test
    void shouldAcceptAccountIdWithSpecialCharacters() {
        Transaction transaction = transactionBuilder
            .withAccountId("ACC-123-XYZ")
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldAcceptAccountIdWithNumbers() {
        Transaction transaction = transactionBuilder
            .withAccountId("ACC123")
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }
}
