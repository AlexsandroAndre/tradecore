package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

class DuplicateTransactionRuleTest {

    private DuplicateTransactionRule rule;
    private Set<String> processedTransactionIds;
    private TransactionTestBuilder transactionBuilder;

    @BeforeEach
    void setUp() {
        processedTransactionIds = new HashSet<>();
        rule = new DuplicateTransactionRule(processedTransactionIds);
        transactionBuilder = new TransactionTestBuilder();
    }

    @Test
    void shouldAcceptNewTransaction() {
        Transaction transaction = transactionBuilder
            .withTransactionId(TRANSACTION_ID_NEW)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectDuplicateTransaction() {
        processedTransactionIds.add(TRANSACTION_ID_TXN_001);
        Transaction transaction = transactionBuilder
            .withTransactionId(TRANSACTION_ID_TXN_001)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals(VALIDATION_CODE_DUPLICATED_TRANSACTION, result.validationCode());
        assertEquals(REJECTED_RULE_DUPLICATE_TRANSACTION_RULE, result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldMarkProcessedTransaction() {
        Transaction transaction = transactionBuilder
            .withTransactionId(TRANSACTION_ID_MARK)
            .build();

        rule.validate(transaction);
        rule.markAsProcessed(transaction.transactionId());

        assertTrue(processedTransactionIds.contains(TRANSACTION_ID_MARK));
    }

    @Test
    void shouldRejectMultipleDuplicates() {
        processedTransactionIds.add(TRANSACTION_ID_A);
        processedTransactionIds.add(TRANSACTION_ID_B);
        processedTransactionIds.add(TRANSACTION_ID_C);

        Transaction transactionA = transactionBuilder
            .withTransactionId(TRANSACTION_ID_A)
            .build();
        Transaction transactionB = transactionBuilder
            .withTransactionId(TRANSACTION_ID_B)
            .build();
        Transaction transactionC = transactionBuilder
            .withTransactionId(TRANSACTION_ID_C)
            .build();

        assertTrue(rule.validate(transactionA).isFailure());
        assertTrue(rule.validate(transactionB).isFailure());
        assertTrue(rule.validate(transactionC).isFailure());
    }

    @Test
    void shouldAcceptNewTransactionAfterMultipleDuplicates() {
        processedTransactionIds.add(TRANSACTION_ID_OLD_1);
        processedTransactionIds.add(TRANSACTION_ID_OLD_2);

        Transaction newTransaction = transactionBuilder
            .withTransactionId(TRANSACTION_ID_FRESH)
            .build();

        DomainValidationResult result = rule.validate(newTransaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectExactDuplicate() {
        processedTransactionIds.add(TRANSACTION_ID_DUP_001);

        Transaction transaction1 = transactionBuilder
            .withTransactionId(TRANSACTION_ID_DUP_001)
            .build();
        Transaction transaction2 = transactionBuilder
            .withTransactionId(TRANSACTION_ID_DUP_001)
            .build();

        assertTrue(rule.validate(transaction1).isFailure());
        assertTrue(rule.validate(transaction2).isFailure());
    }
}