package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;

public final class TransactionIdRule implements ValidationRule {

    public static final String INVALID_TRANSACTION_ID = "INVALID_TRANSACTION_ID";
    public static final String TRANSACTION_ID_MUST_NOT_BE_NULL = "Transaction ID must not be null";
    public static final String TRANSACTION_ID_MUST_NOT_BE_EMPTY = "Transaction ID must not be empty";
    public static final String TRANSACTION_ID_RULE = "TRANSACTION_ID_RULE";

    @Override
    public DomainValidationResult validate(Transaction transaction) {
        if (transaction.transactionId() == null) {
            return DomainValidationResult.failure(
                    INVALID_TRANSACTION_ID,
                    TRANSACTION_ID_MUST_NOT_BE_NULL,
                getRuleName()
            );
        }

        if (transaction.transactionId().isEmpty() || transaction.transactionId().isBlank()) {
            return DomainValidationResult.failure(
                    INVALID_TRANSACTION_ID,
                    TRANSACTION_ID_MUST_NOT_BE_EMPTY,
                getRuleName()
            );
        }

        return DomainValidationResult.success();
    }

    @Override
    public String getRuleName() {
        return TRANSACTION_ID_RULE;
    }
}
