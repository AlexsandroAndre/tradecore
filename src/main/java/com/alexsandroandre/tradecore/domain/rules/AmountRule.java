package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import java.math.BigDecimal;

public final class AmountRule implements ValidationRule {

    public static final String INVALID_AMOUNT = "INVALID_AMOUNT";
    public static final String AMOUNT_MUST_NOT_BE_NULL = "Amount must not be null";
    public static final String AMOUNT_MUST_BE_GREATER_THAN_ZERO = "Amount must be greater than zero";
    public static final String AMOUNT_RULE = "AMOUNT_RULE";

    @Override
    public DomainValidationResult validate(Transaction transaction) {
        if (transaction.amount() == null) {
            return DomainValidationResult.failure(
                    INVALID_AMOUNT,
                    AMOUNT_MUST_NOT_BE_NULL,
                getRuleName()
            );
        }

        if (transaction.amount().compareTo(BigDecimal.ZERO) <= 0) {
            return DomainValidationResult.failure(
                    INVALID_AMOUNT,
                    AMOUNT_MUST_BE_GREATER_THAN_ZERO,
                getRuleName()
            );
        }

        return DomainValidationResult.success();
    }

    @Override
    public String getRuleName() {
        return AMOUNT_RULE;
    }
}
