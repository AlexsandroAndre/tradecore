package com.alexsandroandre.tradecore.domain.validation;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.rules.TransactionTestBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

class DomainValidationServiceTest {

    private DomainValidationService service;
    private TransactionTestBuilder transactionBuilder;

    @BeforeEach
    void setUp() {
        service = new DomainValidationService();
        transactionBuilder = new TransactionTestBuilder();
    }

    @Test
    void shouldAcceptValidTransaction() {
        Transaction transaction = transactionBuilder.build();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectTransactionWithNullTransactionId() {
        Transaction transaction = transactionBuilder.buildWithNullTransactionId();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_TRANSACTION_ID, result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithNullAccountId() {
        Transaction transaction = transactionBuilder.buildWithNullAccountId();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_ACCOUNT_ID, result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithNegativeAmount() {
        Transaction transaction = transactionBuilder.buildWithNegativeAmount();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_AMOUNT, result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithZeroAmount() {
        Transaction transaction = transactionBuilder.buildWithZeroAmount();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_AMOUNT, result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithUnsupportedCurrency() {
        Transaction transaction = transactionBuilder.buildWithUnsupportedCurrency();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_UNSUPPORTED_CURRENCY, result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithFutureTimestamp() {
        Transaction transaction = transactionBuilder.buildWithFutureTimestamp();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_TIMESTAMP, result.validationCode());
    }

    @Test
    void shouldRejectTransactionWithNullSource() {
        Transaction transaction = transactionBuilder.buildWithNullSource();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_SOURCE, result.validationCode());
    }

    @Test
    void shouldDetectDuplicateTransactions() {
        Transaction transaction1 = transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_001)
            .build();

        DomainValidationResult result1 = service.validate(transaction1);
        assertTrue(result1.isSuccess());

        Transaction transaction2 = transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_001)
            .withAccountId(ACCOUNT_ID_ACC_456)
            .build();

        DomainValidationResult result2 = service.validate(transaction2);
        assertTrue(result2.isFailure());
        assertEquals(VALIDATION_CODE_DUPLICATED_TRANSACTION, result2.validationCode());
    }

    @Test
    void shouldValidateBatchOfValidTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_001)
            .build());
        transactions.add(transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_002)
            .withCurrency(CURRENCY_EUR)
            .build());
        transactions.add(transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_003)
            .withCurrency(CURRENCY_GBP)
            .build());

        List<DomainValidationResult> results = service.validateBatch(transactions);

        assertEquals(LIST_SIZE_3, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertTrue(results.get(2).isSuccess());
    }

    @Test
    void shouldValidateBatchWithSomeInvalidTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_001)
            .build());
        transactions.add(transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_002)
            .buildWithZeroAmount());
        transactions.add(transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_003)
            .buildWithUnsupportedCurrency());

        List<DomainValidationResult> results = service.validateBatch(transactions);

        assertEquals(LIST_SIZE_3, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isFailure());
        assertEquals(VALIDATION_CODE_INVALID_AMOUNT, results.get(1).validationCode());
        assertTrue(results.get(2).isFailure());
        assertEquals(VALIDATION_CODE_UNSUPPORTED_CURRENCY, results.get(2).validationCode());
    }

    @Test
    void shouldAllowMultipleValidTransactionsWithDifferentIds() {
        Transaction transaction1 = transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_001)
            .build();
        DomainValidationResult result1 = service.validate(transaction1);
        assertTrue(result1.isSuccess());

        Transaction transaction2 = transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_002)
            .withCurrency(CURRENCY_EUR)
            .build();
        DomainValidationResult result2 = service.validate(transaction2);
        assertTrue(result2.isSuccess());

        Transaction transaction3 = transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_003)
            .withCurrency(CURRENCY_GBP)
            .build();
        DomainValidationResult result3 = service.validate(transaction3);
        assertTrue(result3.isSuccess());
    }

    @Test
    void shouldStopValidationOnFirstFailure() {
        Transaction transaction = transactionBuilder
            .buildWithNullTransactionId();

        DomainValidationResult result = service.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(VALIDATION_CODE_INVALID_TRANSACTION_ID, result.validationCode());
        assertEquals(REJECTED_RULE_TRANSACTION_ID_RULE, result.rejectedRule());
    }

    @Test
    void shouldValidateAllFieldsInCorrectOrder() {
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transactionBuilder.buildWithNullTransactionId());
        transactions.add(transactionBuilder.buildWithNullAccountId());
        transactions.add(transactionBuilder.buildWithNullAmount());
        transactions.add(transactionBuilder.buildWithNullCurrency());
        transactions.add(transactionBuilder.buildWithNullTimestamp());
        transactions.add(transactionBuilder.buildWithNullSource());

        List<DomainValidationResult> results = service.validateBatch(transactions);

        assertEquals(6, results.size());
        for (DomainValidationResult result : results) {
            assertTrue(result.isFailure());
            assertNotNull(result.validationCode());
            assertNotNull(result.rejectedRule());
        }
    }
}
