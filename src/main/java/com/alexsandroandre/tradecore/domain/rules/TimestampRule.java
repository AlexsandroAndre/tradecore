package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import java.time.OffsetDateTime;

public final class TimestampRule implements ValidationRule {

    public static final String INVALID_TIMESTAMP = "INVALID_TIMESTAMP";
    public static final String TIMESTAMP_MUST_NOT_BE_NULL = "Timestamp must not be null";
    public static final String TIMESTAMP_CANNOT_BE_IN_THE_FUTURE = "Timestamp cannot be in the future";
    public static final String TIMESTAMP_RULE = "TIMESTAMP_RULE";

    @Override
    public DomainValidationResult validate(Transaction transaction) {
        if (transaction.timestamp() == null) {
            return DomainValidationResult.failure(
                    INVALID_TIMESTAMP,
                    TIMESTAMP_MUST_NOT_BE_NULL,
                getRuleName()
            );
        }

        OffsetDateTime transactionTime = transaction.timestamp();
        OffsetDateTime now = OffsetDateTime.now();

        if (transactionTime.isAfter(now)) {
            return DomainValidationResult.failure(
                    INVALID_TIMESTAMP,
                    TIMESTAMP_CANNOT_BE_IN_THE_FUTURE,
                getRuleName()
            );
        }

        return DomainValidationResult.success();
    }

    @Override
    public String getRuleName() {
        return TIMESTAMP_RULE;
    }
}
