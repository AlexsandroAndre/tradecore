package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionIdRuleTest {

    private static final String VALID_TRANSACTION_ID = "TXN-001";
    private static final String VALID_ACCOUNT_ID = "ACC-123";
    private static final BigDecimal VALID_AMOUNT = new BigDecimal("100.50");
    private static final String VALID_CURRENCY = "USD";
    private static final OffsetDateTime VALID_TIMESTAMP = OffsetDateTime.now().minusHours(1);
    private static final String VALID_SOURCE = "external-bank";
    private static final Transaction.TransactionStatus VALID_STATUS = Transaction.TransactionStatus.PENDING;

    private TransactionIdRule rule;

    @BeforeEach
    void setUp() {
        rule = new TransactionIdRule();
    }

    @Test
    void shouldAcceptValidTransactionId() {
        Transaction transaction = new Transaction(
            VALID_TRANSACTION_ID,
            VALID_ACCOUNT_ID,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            VALID_SOURCE,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectNullTransactionId() {
        Transaction transaction = new Transaction(
            null,
            VALID_ACCOUNT_ID,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            VALID_SOURCE,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals("INVALID_TRANSACTION_ID", result.validationCode());
        assertEquals("TRANSACTION_ID_RULE", result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldRejectEmptyTransactionId() {
        Transaction transaction = new Transaction(
            "",
            VALID_ACCOUNT_ID,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            VALID_SOURCE,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TRANSACTION_ID", result.validationCode());
        assertEquals("TRANSACTION_ID_RULE", result.rejectedRule());
    }

    @Test
    void shouldRejectBlankTransactionId() {
        Transaction transaction = new Transaction(
            "   ",
            VALID_ACCOUNT_ID,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            VALID_SOURCE,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals("INVALID_TRANSACTION_ID", result.validationCode());
        assertEquals("TRANSACTION_ID_RULE", result.rejectedRule());
    }

    @Test
    void shouldAcceptTransactionIdWithSpecialCharacters() {
        Transaction transaction = new Transaction(
            "TXN-001-ABC-XYZ",
            VALID_ACCOUNT_ID,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            VALID_SOURCE,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }
}