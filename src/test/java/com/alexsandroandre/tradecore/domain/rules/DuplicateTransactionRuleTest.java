package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DuplicateTransactionRuleTest {

    private static final String EXISTING_TRANSACTION_ID = "TXN-001";
    private static final String NEW_TRANSACTION_ID = "TXN-NEW";

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
            .withTransactionId(NEW_TRANSACTION_ID)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isSuccess());
        assertEquals(DomainValidationResult.ValidationStatus.SUCCESS, result.status());
    }

    @Test
    void shouldRejectDuplicateTransaction() {
        processedTransactionIds.add(EXISTING_TRANSACTION_ID);
        Transaction transaction = transactionBuilder
            .withTransactionId(EXISTING_TRANSACTION_ID)
            .build();

        DomainValidationResult result = rule.validate(transaction);

        assertTrue(result.isFailure());
        assertEquals(DomainValidationResult.ValidationStatus.FAILURE, result.status());
        assertEquals("DUPLICATED_TRANSACTION", result.validationCode());
        assertEquals("DUPLICATE_TRANSACTION_RULE", result.rejectedRule());
        assertNotNull(result.validationMessage());
    }

    @Test
    void shouldMarkProcessedTransaction() {
        Transaction transaction = transactionBuilder
            .withTransactionId("TXN-MARK")
            .build();

        rule.validate(transaction);
        rule.markAsProcessed(transaction.transactionId());

        assertTrue(processedTransactionIds.contains("TXN-MARK"));
    }

    @Test
    void shouldRejectMultipleDuplicates() {
        processedTransactionIds.add("TXN-A");
        processedTransactionIds.add("TXN-B");
        processedTransactionIds.add("TXN-C");

        Transaction transactionA = transactionBuilder
            .withTransactionId("TXN-A")
            .build();
        Transaction transactionB = transactionBuilder
            .withTransactionId("TXN-B")
            .build();
        Transaction transactionC = transactionBuilder
            .withTransactionId("TXN-C")
            .build();

        assertTrue(rule.validate(transactionA).isFailure());
        assertTrue(rule.validate(transactionB).isFailure());
        assertTrue(rule.validate(transactionC).isFailure());
    }

    @Test
    void shouldAcceptNewTransactionAfterMultipleDuplicates() {
        processedTransactionIds.add("TXN-OLD-1");
        processedTransactionIds.add("TXN-OLD-2");

        Transaction newTransaction = transactionBuilder
            .withTransactionId("TXN-FRESH")
            .build();

        DomainValidationResult result = rule.validate(newTransaction);

        assertTrue(result.isSuccess());
    }

    @Test
    void shouldRejectExactDuplicate() {
        String duplicateId = "DUP-001";
        processedTransactionIds.add(duplicateId);

        Transaction transaction1 = transactionBuilder
            .withTransactionId(duplicateId)
            .build();
        Transaction transaction2 = transactionBuilder
            .withTransactionId(duplicateId)
            .build();

        assertTrue(rule.validate(transaction1).isFailure());
        assertTrue(rule.validate(transaction2).isFailure());
    }
}
