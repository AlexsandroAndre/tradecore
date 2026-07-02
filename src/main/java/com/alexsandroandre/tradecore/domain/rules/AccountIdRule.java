package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;

public final class AccountIdRule implements ValidationRule {

    public static final String INVALID_ACCOUNT_ID = "INVALID_ACCOUNT_ID";
    public static final String ACCOUNT_ID_MUST_NOT_BE_NULL = "Account ID must not be null";
    public static final String ACCOUNT_ID_MUST_NOT_BE_EMPTY = "Account ID must not be empty";
    public static final String ACCOUNT_ID_RULE = "ACCOUNT_ID_RULE";

    @Override
    public DomainValidationResult validate(Transaction transaction) {
        if (transaction.accountId() == null) {
            return DomainValidationResult.failure(
                    INVALID_ACCOUNT_ID,
                    ACCOUNT_ID_MUST_NOT_BE_NULL,
                getRuleName()
            );
        }

        if (transaction.accountId().isEmpty() || transaction.accountId().isBlank()) {
            return DomainValidationResult.failure(
                    INVALID_ACCOUNT_ID,
                    ACCOUNT_ID_MUST_NOT_BE_EMPTY,
                getRuleName()
            );
        }

        return DomainValidationResult.success();
    }

    @Override
    public String getRuleName() {
        return ACCOUNT_ID_RULE;
    }
}
