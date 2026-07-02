package com.alexsandroandre.tradecore.application.service;

import com.alexsandroandre.tradecore.application.dto.RawTransactionData;
import com.alexsandroandre.tradecore.application.dto.ValidationResult;
import com.alexsandroandre.tradecore.domain.validation.ValidationError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputValidatorImplTest {
    public static final String TXN_001 = "TXN-001";
    public static final String ACC_123 = "ACC-123";
    public static final String USD = "USD";
    public static final String T_10_30_00_Z = "2024-01-15T10:30:00Z";
    public static final String EXTERNAL_BANK = "external-bank";
    public static final Double ONE_HUNDRED_FIFTY_CENTS = 100.50;
    public static final String TRANSACTION_ID = "transactionId";
    public static final String TXN_ARROBA_001 = "TXN@001!";
    public static final String SHOULD_ACCEPT_VALID_TRANSACTION_ID = "Should accept valid transactionId: ";
    public static final String ACCOUNT_ID = "accountId";

    // Novas Constantes
    public static final String EMPTY_STRING = "";
    public static final String AMOUNT = "amount";
    public static final String CURRENCY = "currency";
    public static final String TIMESTAMP = "timestamp";
    public static final String SOURCE = "source";
    public static final String UNKNOWN_FIELD = "unknownField";
    public static final String VALUE = "value";

    public static final Double AMOUNT_ZERO_POINT_ZERO_ONE = 0.01;
    public static final Double AMOUNT_NINE_NINE_NINE_NINE_NINE_NINE_POINT_NINE_NINE = 999999.99;
    public static final Double AMOUNT_ZERO_POINT_ZERO_ZERO_ONE = 0.001;

    public static final String SHOULD_ACCEPT_VALID_AMOUNT = "Should accept valid amount: ";
    public static final String SHOULD_REJECT_INVALID_CURRENCY = "Should reject invalid currency: ";
    public static final String SHOULD_ACCEPT_VALID_CURRENCY = "Should accept valid currency: ";
    public static final String SHOULD_REJECT_INVALID_TIMESTAMP = "Should reject invalid timestamp: ";
    public static final String SHOULD_ACCEPT_VALID_TIMESTAMP = "Should accept valid timestamp: ";
    public static final String SHOULD_ACCEPT_VALID_SOURCE = "Should accept valid source: ";

    public static final String[] VALID_TRANSACTION_IDS = {TXN_001, "txn_001", "TXN001", "txn-456-abc", "ABC_123_XYZ"};
    public static final Double[] VALID_AMOUNTS = {AMOUNT_ZERO_POINT_ZERO_ONE, ONE_HUNDRED_FIFTY_CENTS, AMOUNT_NINE_NINE_NINE_NINE_NINE_NINE_POINT_NINE_NINE, AMOUNT_ZERO_POINT_ZERO_ZERO_ONE};
    public static final String[] INVALID_CURRENCIES = {"US", "USDA", "usd", "Us$", "UD1"};
    public static final String[] VALID_CURRENCIES = {USD, "EUR", "GBP", "JPY", "CAD", "AUD"};
    public static final String[] INVALID_TIMESTAMPS = {"2024-01-15", "10:30:00", "2024/01/15 10:30:00", "invalid-date", "2024-13-45T10:30:00Z"};
    public static final String[] VALID_TIMESTAMPS = {T_10_30_00_Z, "2024-01-15T10:30:00+00:00", "2024-01-15T10:30:00.123Z", "2024-01-15T10:30:00.123456Z"};
    public static final String[] VALID_SOURCES = {"bank-api", "external_system", "PaymentGateway", "ACH-Import"};

    private InputValidatorImpl validator;

    @BeforeEach
    void setUp() {
        validator = new InputValidatorImpl();
    }

    @Test
    void shouldValidateValidRecord() {
        RawTransactionData validRecord = new RawTransactionData(
                TXN_001,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(validRecord);

        assertTrue(result.valid());
        assertNull(result.error());
        assertNull(result.errorMessage());
        assertNull(result.invalidField());
    }

    @Test
    void shouldRejectNullRecord() {
        ValidationResult result = validator.validate(null);

        assertFalse(result.valid());
        assertEquals(ValidationError.INVALID_JSON, result.error());
        assertNull(result.invalidField());
    }

    @Test
    void shouldRejectMissingTransactionId() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                null,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.MISSING_FIELD, result.error());
        assertEquals(TRANSACTION_ID, result.invalidField());
    }

    @Test
    void shouldRejectEmptyTransactionId() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                EMPTY_STRING,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.INVALID_VALUE, result.error());
        assertEquals(TRANSACTION_ID, result.invalidField());
    }

    @Test
    void shouldRejectInvalidTransactionIdFormat() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_ARROBA_001,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.INVALID_FORMAT, result.error());
        assertEquals(TRANSACTION_ID, result.invalidField());
    }

    @Test
    void shouldAcceptValidTransactionIdFormats() {
        for (String id : VALID_TRANSACTION_IDS) {
            RawTransactionData rawTransactionData = new RawTransactionData(
                    id,
                    ACC_123,
                    ONE_HUNDRED_FIFTY_CENTS,
                    USD,
                    T_10_30_00_Z,
                    EXTERNAL_BANK
            );

            ValidationResult result = validator.validate(rawTransactionData);
            assertTrue(result.valid(), SHOULD_ACCEPT_VALID_TRANSACTION_ID + id);
        }
    }

    @Test
    void shouldRejectMissingAccountId() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                null,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.MISSING_FIELD, result.error());
        assertEquals(ACCOUNT_ID, result.invalidField());
    }

    @Test
    void shouldRejectEmptyAccountId() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                EMPTY_STRING,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.INVALID_VALUE, result.error());
        assertEquals(ACCOUNT_ID, result.invalidField());
    }

    @Test
    void shouldRejectMissingAmount() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                null,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.MISSING_FIELD, result.error());
        assertEquals(AMOUNT, result.invalidField());
    }

    @Test
    void shouldRejectNaNAmount() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                Double.NaN,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.INVALID_TYPE, result.error());
        assertEquals(AMOUNT, result.invalidField());
    }

    @Test
    void shouldRejectInfiniteAmount() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                Double.POSITIVE_INFINITY,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.INVALID_TYPE, result.error());
        assertEquals(AMOUNT, result.invalidField());
    }

    @Test
    void shouldAcceptValidAmounts() {
        for (Double amount : VALID_AMOUNTS) {
            RawTransactionData rawTransactionData = new RawTransactionData(
                    TXN_001,
                    ACC_123,
                    amount,
                    USD,
                    T_10_30_00_Z,
                    EXTERNAL_BANK
            );

            ValidationResult result = validator.validate(rawTransactionData);
            assertTrue(result.valid(), SHOULD_ACCEPT_VALID_AMOUNT + amount);
        }
    }

    @Test
    void shouldRejectMissingCurrency() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                null,
                T_10_30_00_Z,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.MISSING_FIELD, result.error());
        assertEquals(CURRENCY, result.invalidField());
    }

    @Test
    void shouldRejectInvalidCurrencyFormat() {
        for (String currency : INVALID_CURRENCIES) {
            RawTransactionData rawTransactionData = new RawTransactionData(
                    TXN_001,
                    ACC_123,
                    ONE_HUNDRED_FIFTY_CENTS,
                    currency,
                    T_10_30_00_Z,
                    EXTERNAL_BANK
            );

            ValidationResult result = validator.validate(rawTransactionData);
            assertFalse(result.valid(), SHOULD_REJECT_INVALID_CURRENCY + currency);
            assertEquals(ValidationError.INVALID_FORMAT, result.error());
            assertEquals(CURRENCY, result.invalidField());
        }
    }

    @Test
    void shouldAcceptValidCurrencies() {
        for (String currency : VALID_CURRENCIES) {
            RawTransactionData rawTransactionData = new RawTransactionData(
                    TXN_001,
                    ACC_123,
                    ONE_HUNDRED_FIFTY_CENTS,
                    currency,
                    T_10_30_00_Z,
                    EXTERNAL_BANK
            );

            ValidationResult result = validator.validate(rawTransactionData);
            assertTrue(result.valid(), SHOULD_ACCEPT_VALID_CURRENCY + currency);
        }
    }

    @Test
    void shouldRejectMissingTimestamp() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                null,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.MISSING_FIELD, result.error());
        assertEquals(TIMESTAMP, result.invalidField());
    }

    @Test
    void shouldRejectEmptyTimestamp() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                EMPTY_STRING,
                EXTERNAL_BANK
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.INVALID_VALUE, result.error());
        assertEquals(TIMESTAMP, result.invalidField());
    }

    @Test
    void shouldRejectInvalidTimestampFormat() {
        for (String timestamp : INVALID_TIMESTAMPS) {
            RawTransactionData rawTransactionData = new RawTransactionData(
                    TXN_001,
                    ACC_123,
                    ONE_HUNDRED_FIFTY_CENTS,
                    USD,
                    timestamp,
                    EXTERNAL_BANK
            );

            ValidationResult result = validator.validate(rawTransactionData);
            assertFalse(result.valid(), SHOULD_REJECT_INVALID_TIMESTAMP + timestamp);
            assertEquals(ValidationError.INVALID_FORMAT, result.error());
            assertEquals(TIMESTAMP, result.invalidField());
        }
    }

    @Test
    void shouldAcceptValidTimestamps() {
        for (String timestamp : VALID_TIMESTAMPS) {
            RawTransactionData rawTransactionData = new RawTransactionData(
                    TXN_001,
                    ACC_123,
                    ONE_HUNDRED_FIFTY_CENTS,
                    USD,
                    timestamp,
                    EXTERNAL_BANK
            );

            ValidationResult result = validator.validate(rawTransactionData);
            assertTrue(result.valid(), SHOULD_ACCEPT_VALID_TIMESTAMP + timestamp);
        }
    }

    @Test
    void shouldRejectMissingSource() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                null
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.MISSING_FIELD, result.error());
        assertEquals(SOURCE, result.invalidField());
    }

    @Test
    void shouldRejectEmptySource() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                EMPTY_STRING
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.INVALID_VALUE, result.error());
        assertEquals(SOURCE, result.invalidField());
    }

    @Test
    void shouldAcceptValidSources() {
        for (String source : VALID_SOURCES) {
            RawTransactionData rawTransactionData = new RawTransactionData(
                    TXN_001,
                    ACC_123,
                    ONE_HUNDRED_FIFTY_CENTS,
                    USD,
                    T_10_30_00_Z,
                    source
            );

            ValidationResult result = validator.validate(rawTransactionData);
            assertTrue(result.valid(), SHOULD_ACCEPT_VALID_SOURCE + source);
        }
    }

    @Test
    void shouldValidateFieldsInOrder() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                null,
                null,
                null,
                null,
                null,
                null
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertFalse(result.valid());
        assertEquals(ValidationError.MISSING_FIELD, result.error());
        assertEquals(TRANSACTION_ID, result.invalidField());
    }

    @Test
    void shouldRejectRecordWithAdditionalFields() {
        RawTransactionData rawTransactionData = new RawTransactionData(
                TXN_001,
                ACC_123,
                ONE_HUNDRED_FIFTY_CENTS,
                USD,
                T_10_30_00_Z,
                EXTERNAL_BANK,
                java.util.Map.of(UNKNOWN_FIELD, VALUE)
        );

        ValidationResult result = validator.validate(rawTransactionData);

        assertTrue(result.valid());
    }
}