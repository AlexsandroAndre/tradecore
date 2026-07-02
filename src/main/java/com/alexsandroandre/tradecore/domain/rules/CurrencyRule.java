package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import java.util.Set;

public final class CurrencyRule implements ValidationRule {

    private static final Set<String> SUPPORTED_CURRENCIES = Set.of(
        "USD", "EUR", "GBP", "JPY", "CHF", "CAD", "AUD", "NZD", "CNY", "INR",
        "MXN", "SGD", "HKD", "SEK", "NOK", "DKK", "ZAR", "RUB", "BRL", "KRW"
    );
    public static final String UNSUPPORTED_CURRENCY = "UNSUPPORTED_CURRENCY";
    public static final String CURRENCY_MUST_NOT_BE_NULL = "Currency must not be null";
    public static final String CURRENCY_MUST_FOLLOW_ISO_4217_FORMAT_3_UPPERCASE_LETTERS = "Currency must follow ISO 4217 format (3 uppercase letters)";
    public static final String CURRENCY = "Currency ";
    public static final String IS_NOT_SUPPORTED = " is not supported";
    public static final String CURRENCY_RULE = "CURRENCY_RULE";

    @Override
    public DomainValidationResult validate(Transaction transaction) {
        if (transaction.currency() == null) {
            return DomainValidationResult.failure(
                    UNSUPPORTED_CURRENCY,
                    CURRENCY_MUST_NOT_BE_NULL,
                getRuleName()
            );
        }

        String currency = transaction.currency().toUpperCase();

        if (!currency.matches("^[A-Z]{3}$")) {
            return DomainValidationResult.failure(
                    UNSUPPORTED_CURRENCY,
                    CURRENCY_MUST_FOLLOW_ISO_4217_FORMAT_3_UPPERCASE_LETTERS,
                getRuleName()
            );
        }

        if (!SUPPORTED_CURRENCIES.contains(currency)) {
            return DomainValidationResult.failure(
                    UNSUPPORTED_CURRENCY,
                CURRENCY + currency + IS_NOT_SUPPORTED,
                getRuleName()
            );
        }

        return DomainValidationResult.success();
    }

    @Override
    public String getRuleName() {
        return CURRENCY_RULE;
    }
}
