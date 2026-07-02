package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountIdRuleTest {

    private AccountIdRule rule;

    @BeforeEach
    void setUp() {
        rule = new AccountIdRule();
    }

    @Test
    void shouldAcceptValidAccountId() {
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
    void shouldRejectNullAccountId() {
        Transaction transaction = new Transaction(
            "TXN-001",
            null,
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_ACCOUNT_ID", result.validationCode());
        assertEquals("ACCOUNT_ID_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectEmptyAccountId() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_ACCOUNT_ID", result.validationCode());
    }

    @Test
    void shouldRejectBlankAccountId() {
        Transaction transaction = new Transaction(
            "TXN-001",
            "   ",
            new BigDecimal("100.50"),
            "USD",
            OffsetDateTime.now().minusHours(1),
            "external-bank",
            Transaction.TransactionStatus.PENDING
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_ACCOUNT_ID", result.validationCode());
    }
}
