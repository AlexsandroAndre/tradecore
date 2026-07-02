package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;

public final class SourceRule implements ValidationRule {

    public static final String INVALID_SOURCE = "INVALID_SOURCE";
    public static final String SOURCE_MUST_NOT_BE_NULL = "Source must not be null";
    public static final String SOURCE_MUST_NOT_BE_EMPTY = "Source must not be empty";
    public static final String SOURCE_RULE = "SOURCE_RULE";

    @Override
    public DomainValidationResult validate(Transaction transaction) {
        if (transaction.source() == null) {
            return DomainValidationResult.failure(
                    INVALID_SOURCE,
                    SOURCE_MUST_NOT_BE_NULL,
                getRuleName()
            );
        }

        if (transaction.source().isEmpty() || transaction.source().isBlank()) {
            return DomainValidationResult.failure(
                    INVALID_SOURCE,
                    SOURCE_MUST_NOT_BE_EMPTY,
                getRuleName()
            );
        }

        return DomainValidationResult.success();
    }

    @Override
    public String getRuleName() {
        return SOURCE_RULE;
    }
}
