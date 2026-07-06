package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

class TransactionIdRuleTest {

    private static final BigDecimal VALID_AMOUNT = BigDecimal.valueOf(100.50);
    private static final OffsetDateTime VALID_TIMESTAMP = OffsetDateTime.now().minusHours(1);
    private static final Transaction.TransactionStatus VALID_STATUS = Transaction.TransactionStatus.PENDING;

    private TransactionIdRule rule;

    @BeforeEach
    void setUp() {
        rule = new TransactionIdRule();
    }

    @Test
    void shouldAcceptValidTransactionId() {
        Transaction transaction = new Transaction(
            TRANSACTION_ID_TXN_001,
            ACCOUNT_ID_ACC_123,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            SOURCE_EXTERNAL_BANK,
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
            ACCOUNT_ID_ACC_123,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            SOURCE_EXTERNAL_BANK,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals(VALIDATION_CODE_INVALID_TRANSACTION_ID, result.validationCode());
        assertEquals(REJECTED_RULE_TRANSACTION_ID_RULE, result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldRejectEmptyTransactionId() {
        Transaction transaction = new Transaction(
            TRANSACTION_ID_EMPTY,
            ACCOUNT_ID_ACC_123,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            SOURCE_EXTERNAL_BANK,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_TRANSACTION_ID, result.validationCode());
        assertEquals(REJECTED_RULE_TRANSACTION_ID_RULE, result.rejectedRule());
    }

    @Test
    void shouldRejectBlankTransactionId() {
        Transaction transaction = new Transaction(
            TRANSACTION_ID_BLANK,
            ACCOUNT_ID_ACC_123,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            SOURCE_EXTERNAL_BANK,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_TRANSACTION_ID, result.validationCode());
        assertEquals(REJECTED_RULE_TRANSACTION_ID_RULE, result.rejectedRule());
    }

    @Test
    void shouldAcceptTransactionIdWithSpecialCharacters() {
        Transaction transaction = new Transaction(
            TRANSACTION_ID_WITH_SPECIAL_CHARS,
            ACCOUNT_ID_ACC_123,
            VALID_AMOUNT,
            VALID_CURRENCY,
            VALID_TIMESTAMP,
            SOURCE_EXTERNAL_BANK,
            VALID_STATUS
        );

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
    }
}