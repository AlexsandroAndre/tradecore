package com.alexsandroandre.tradecore.application.service;

import com.alexsandroandre.tradecore.application.dto.RawTransactionData;
import com.alexsandroandre.tradecore.application.dto.ValidationResult;
import com.alexsandroandre.tradecore.application.port.InputValidator;
import com.alexsandroandre.tradecore.domain.validation.ValidationError;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

public class InputValidatorImpl implements InputValidator {
    private static final Pattern TRANSACTION_ID_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern ISO_CURRENCY_PATTERN = Pattern.compile("^[A-Z]{3}$");
    private static final int TRANSACTION_ID_MAX_LENGTH = 255;
    private static final int ACCOUNT_ID_MAX_LENGTH = 255;
    private static final int SOURCE_MAX_LENGTH = 255;
    public static final String TRANSACTION_DATA_IS_NULL = "Transaction data is null";
    public static final String ID_IS_REQUIRED = "transactionId is required";
    public static final String TRANSACTION_ID = "transactionId";
    public static final String TRANSACTION_ID_CANNOT_BE_EMPTY = "transactionId cannot be empty";
    public static final String TRANSACTION_ID_EXCEEDS_MAXIMUM_LENGTH_OF = "transactionId exceeds maximum length of ";
    public static final String TRANSACTION_ID_HAS_INVALID_FORMAT = "transactionId has invalid format";
    public static final String ACCOUNT_ID_IS_REQUIRED = "accountId is required";
    public static final String ACCOUNT_ID = "accountId";
    public static final String ACCOUNT_ID_CANNOT_BE_EMPTY = "accountId cannot be empty";
    public static final String ACCOUNT_ID_EXCEEDS_MAXIMUM_LENGTH_OF = "accountId exceeds maximum length of ";
    public static final String AMOUNT_IS_REQUIRED = "amount is required";
    public static final String AMOUNT = "amount";
    public static final String AMOUNT_MUST_BE_A_VALID_NUMBER = "amount must be a valid number";
    public static final String AMOUNT_CANNOT_BE_NA_N_OR_INFINITY = "amount cannot be NaN or Infinity";
    public static final String CURRENCY_IS_REQUIRED = "currency is required";
    public static final String CURRENCY = "currency";
    public static final String CURRENCY_MUST_BE_A_VALID_ISO_4217_CODE_E_G_USD_EUR_GBP = "currency must be a valid ISO 4217 code (e.g., USD, EUR, GBP)";
    public static final String TIMESTAMP_IS_REQUIRED = "timestamp is required";
    public static final String TIMESTAMP = "timestamp";
    public static final String TIMESTAMP_CANNOT_BE_EMPTY = "timestamp cannot be empty";
    public static final String TIMESTAMP_MUST_BE_IN_VALID_ISO_8601_FORMAT = "timestamp must be in valid ISO-8601 format";
    public static final String SOURCE_IS_REQUIRED = "source is required";
    public static final String SOURCE = "source";
    public static final String SOURCE_CANNOT_BE_EMPTY = "source cannot be empty";
    public static final String SOURCE_EXCEEDS_MAXIMUM_LENGTH_OF = "source exceeds maximum length of ";

    @Override
    public ValidationResult validate(RawTransactionData rawTransaction) {
        if (rawTransaction == null) {
            return ValidationResult.failure(
                ValidationError.INVALID_JSON,
                    TRANSACTION_DATA_IS_NULL,
                null
            );
        }

        ValidationResult result = validateTransactionId(rawTransaction.transactionId());
        if (!result.valid()) {
            return result;
        }

        result = validateAccountId(rawTransaction.accountId());
        if (!result.valid()) {
            return result;
        }

        result = validateAmount(rawTransaction.amount());
        if (!result.valid()) {
            return result;
        }

        result = validateCurrency(rawTransaction.currency());
        if (!result.valid()) {
            return result;
        }

        result = validateTimestamp(rawTransaction.timestamp());
        if (!result.valid()) {
            return result;
        }

        result = validateSource(rawTransaction.source());
        if (!result.valid()) {
            return result;
        }

        return ValidationResult.success();
    }

    private ValidationResult validateTransactionId(String transactionId) {
        if (transactionId == null) {
            return ValidationResult.failure(
                ValidationError.MISSING_FIELD,
                    ID_IS_REQUIRED,
                    TRANSACTION_ID
            );
        }

        if (transactionId.isEmpty()) {
            return ValidationResult.failure(
                ValidationError.INVALID_VALUE,
                    TRANSACTION_ID_CANNOT_BE_EMPTY,
                    TRANSACTION_ID
            );
        }

        if (transactionId.length() > TRANSACTION_ID_MAX_LENGTH) {
            return ValidationResult.failure(
                ValidationError.INVALID_VALUE,
                TRANSACTION_ID_EXCEEDS_MAXIMUM_LENGTH_OF + TRANSACTION_ID_MAX_LENGTH,
                    TRANSACTION_ID
            );
        }

        if (!TRANSACTION_ID_PATTERN.matcher(transactionId).matches()) {
            return ValidationResult.failure(
                ValidationError.INVALID_FORMAT,
                    TRANSACTION_ID_HAS_INVALID_FORMAT,
                    TRANSACTION_ID
            );
        }

        return ValidationResult.success();
    }

    private ValidationResult validateAccountId(String accountId) {
        if (accountId == null) {
            return ValidationResult.failure(
                ValidationError.MISSING_FIELD,
                    ACCOUNT_ID_IS_REQUIRED,
                    ACCOUNT_ID
            );
        }

        if (accountId.isEmpty()) {
            return ValidationResult.failure(
                ValidationError.INVALID_VALUE,
                    ACCOUNT_ID_CANNOT_BE_EMPTY,
                    ACCOUNT_ID
            );
        }

        if (accountId.length() > ACCOUNT_ID_MAX_LENGTH) {
            return ValidationResult.failure(
                ValidationError.INVALID_VALUE,
                ACCOUNT_ID_EXCEEDS_MAXIMUM_LENGTH_OF + ACCOUNT_ID_MAX_LENGTH,
                    ACCOUNT_ID
            );
        }

        return ValidationResult.success();
    }

    private ValidationResult validateAmount(Double amount) {
        if (amount == null) {
            return ValidationResult.failure(
                ValidationError.MISSING_FIELD,
                    AMOUNT_IS_REQUIRED,
                    AMOUNT
            );
        }

        if (!isFiniteNumber(amount)) {
            return ValidationResult.failure(
                ValidationError.INVALID_TYPE,
                    AMOUNT_MUST_BE_A_VALID_NUMBER,
                    AMOUNT
            );
        }

        if (amount.isNaN() || amount.isInfinite()) {
            return ValidationResult.failure(
                ValidationError.INVALID_VALUE,
                    AMOUNT_CANNOT_BE_NA_N_OR_INFINITY,
                    AMOUNT
            );
        }

        return ValidationResult.success();
    }

    private ValidationResult validateCurrency(String currency) {
        if (currency == null) {
            return ValidationResult.failure(
                ValidationError.MISSING_FIELD,
                    CURRENCY_IS_REQUIRED,
                    CURRENCY
            );
        }

        if (!ISO_CURRENCY_PATTERN.matcher(currency).matches()) {
            return ValidationResult.failure(
                ValidationError.INVALID_FORMAT,
                    CURRENCY_MUST_BE_A_VALID_ISO_4217_CODE_E_G_USD_EUR_GBP,
                    CURRENCY
            );
        }

        return ValidationResult.success();
    }

    private ValidationResult validateTimestamp(String timestamp) {
        if (timestamp == null) {
            return ValidationResult.failure(
                ValidationError.MISSING_FIELD,
                    TIMESTAMP_IS_REQUIRED,
                    TIMESTAMP
            );
        }

        if (timestamp.isEmpty()) {
            return ValidationResult.failure(
                ValidationError.INVALID_VALUE,
                    TIMESTAMP_CANNOT_BE_EMPTY,
                    TIMESTAMP
            );
        }

        try {
            Instant.parse(timestamp);
        } catch (DateTimeParseException e) {
            return ValidationResult.failure(
                ValidationError.INVALID_FORMAT,
                    TIMESTAMP_MUST_BE_IN_VALID_ISO_8601_FORMAT,
                    TIMESTAMP
            );
        }

        return ValidationResult.success();
    }

    private ValidationResult validateSource(String source) {
        if (source == null) {
            return ValidationResult.failure(
                ValidationError.MISSING_FIELD,
                    SOURCE_IS_REQUIRED,
                    SOURCE
            );
        }

        if (source.isEmpty()) {
            return ValidationResult.failure(
                ValidationError.INVALID_VALUE,
                    SOURCE_CANNOT_BE_EMPTY,
                    SOURCE
            );
        }

        if (source.length() > SOURCE_MAX_LENGTH) {
            return ValidationResult.failure(
                ValidationError.INVALID_VALUE,
                SOURCE_EXCEEDS_MAXIMUM_LENGTH_OF + SOURCE_MAX_LENGTH,
                    SOURCE
            );
        }

        return ValidationResult.success();
    }

    private boolean isFiniteNumber(Double number) {
        return number != null && !number.isNaN() && !number.isInfinite();
    }
}
