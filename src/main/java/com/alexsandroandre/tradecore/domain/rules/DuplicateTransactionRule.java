package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import java.util.Set;

public final class DuplicateTransactionRule implements ValidationRule {

    public static final String DUPLICATED_TRANSACTION = "DUPLICATED_TRANSACTION";
    public static final String TRANSACTION_WITH_ID = "Transaction with ID ";
    public static final String HAS_ALREADY_BEEN_PROCESSED = " has already been processed";
    public static final String DUPLICATE_TRANSACTION_RULE = "DUPLICATE_TRANSACTION_RULE";
    private final Set<String> processedTransactionIds;

    public DuplicateTransactionRule(Set<String> processedTransactionIds) {
        this.processedTransactionIds = processedTransactionIds;
    }

    @Override
    public DomainValidationResult validate(Transaction transaction) {
        if (processedTransactionIds.contains(transaction.transactionId())) {
            return DomainValidationResult.failure(
                    DUPLICATED_TRANSACTION,
                TRANSACTION_WITH_ID + transaction.transactionId() + HAS_ALREADY_BEEN_PROCESSED,
                getRuleName()
            );
        }

        return DomainValidationResult.success();
    }

    @Override
    public String getRuleName() {
        return DUPLICATE_TRANSACTION_RULE;
    }

    public void markAsProcessed(String transactionId) {
        processedTransactionIds.add(transactionId);
    }

    public void clearProcessedIds() {
        processedTransactionIds.clear();
    }
}