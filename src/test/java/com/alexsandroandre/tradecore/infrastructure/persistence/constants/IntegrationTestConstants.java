package com.alexsandroandre.tradecore.infrastructure.persistence.constants;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class IntegrationTestConstants {

    // Database Configuration
    public static final String DATABASE_NAME = "transaction_db";
    public static final String DATABASE_USERNAME = "postgres";
    public static final String DATABASE_PASSWORD = "postgres";

    // Transaction IDs and Account IDs
    public static final String VALID_TRANSACTION_ID = UUID.randomUUID().toString();
    public static final String VALID_ACCOUNT_ID = "ACC-001";
    public static final String ACCOUNT_ID_WITH_SPECIAL_CHARS = "ACC-123-XYZ";
    public static final String ACCOUNT_ID_WITH_NUMBERS = "ACC123";
    public static final String ACCOUNT_ID_ACC_123 = "ACC-123";
    public static final String ANOTHER_ACCOUNT_ID = "ACC-002";
    public static final String ANOTHER_ACCOUNT_ID_ACC_003 = "ACC-003";
    public static final String ANOTHER_ACCOUNT_ID_ACC_004 = "ACC-004";

    // Transaction Amounts
    public static final BigDecimal VALID_AMOUNT = BigDecimal.valueOf(1000.00);
    public static final BigDecimal AMOUNT_100 = BigDecimal.valueOf(100.00);
    public static final BigDecimal AMOUNT_NEGATIVE_100 = BigDecimal.valueOf(-100.00);
    public static final BigDecimal AMOUNT_250_50 = BigDecimal.valueOf(250.50);
    public static final BigDecimal AMOUNT_SMALL_POSITIVE = BigDecimal.valueOf(0.01);
    public static final BigDecimal AMOUNT_LARGE_POSITIVE = BigDecimal.valueOf(999999999.99);
    public static final BigDecimal AMOUNT_ZERO = BigDecimal.ZERO;
    public static final BigDecimal ANOTHER_AMOUNT = BigDecimal.valueOf(500.50);

    // Currencies
    public static final String VALID_CURRENCY = "USD";
    public static final String CURRENCY_EUR = "EUR";
    public static final String CURRENCY_INVALID = "INVALID";
    public static final String CURRENCY_GBP = "GBP";
    public static final String CURRENCY_JPY = "JPY";
    public static final String CURRENCY_TWO_LETTER = "US";
    public static final String CURRENCY_EMPTY = "";
    public static final String ANOTHER_CURRENCY = "EUR";

    // Transaction Sources
    public static final String VALID_SOURCE = "IMPORT";
    public static final String SOURCE_SYSTEM_A = "SYSTEM-A";
    public static final String SOURCE_SYSTEM_B = "SYSTEM-B";
    public static final String SOURCE_EXTERNAL_BANK = "external-bank";
    public static final String SOURCE_EXTERNAL_BANK_001 = "external-bank-001";
    public static final String SOURCE_123 = "SOURCE123";
    public static final String SOURCE_IMPORT_SYSTEM = "IMPORT_SYSTEM";
    public static final String ANOTHER_SOURCE = "API";

    // Timestamps
    public static final Instant VALID_TIMESTAMP = Instant.now();
    public static final Instant ANOTHER_TIMESTAMP = Instant.now().minusSeconds(3600);
    public static final Instant VALID_CREATED_AT = Instant.now();

    // Processing Report Constants
    public static final String EXECUTION_ID_EXEC_1 = "exec-1";
    public static final String EXECUTION_ID_EXEC_123 = "exec-123";
    public static final String EXECUTION_ID_TEST_EXEC = "test-exec";

    // Time values for processing reports
    public static final long START_TIME_1000 = 1000L;
    public static final long END_TIME_2000 = 2000L;
    public static final long END_TIME_5000 = 5000L;
    public static final long END_TIME_6000 = 6000L;
    public static final long DURATION_4000 = 4000L;
    public static final long DURATION_5000 = 5000L;

    // Record counts
    public static final int TOTAL_RECORDS_100 = 100;
    public static final int TOTAL_RECORDS_50 = 50;
    public static final int TOTAL_RECORDS_10 = 10;
    public static final int TOTAL_RECORDS_0 = 0;

    public static final int SUCCESSFUL_RECORDS_90 = 90;
    public static final int SUCCESSFUL_RECORDS_80 = 80;
    public static final int SUCCESSFUL_RECORDS_100 = 100;
    public static final int SUCCESSFUL_RECORDS_45 = 45;
    public static final int SUCCESSFUL_RECORDS_0 = 0;

    public static final int REJECTED_RECORDS_5 = 5;
    public static final int REJECTED_RECORDS_10 = 10;
    public static final int REJECTED_RECORDS_20 = 20;
    public static final int REJECTED_RECORDS_3 = 3;
    public static final int REJECTED_RECORDS_0 = 0;

    public static final int FAILED_RECORDS_5 = 5;
    public static final int FAILED_RECORDS_2 = 2;
    public static final int FAILED_RECORDS_10 = 10;
    public static final int FAILED_RECORDS_0 = 0;

    public static final int PERSISTED_RECORDS_90 = 90;
    public static final int PERSISTED_RECORDS_80 = 80;
    public static final int PERSISTED_RECORDS_95 = 95;
    public static final int PERSISTED_RECORDS_100 = 100;
    public static final int PERSISTED_RECORDS_45 = 45;
    public static final int PERSISTED_RECORDS_0 = 0;

    // Throughput values
    public static final long THROUGHPUT_20000 = 20000L;
    public static final long THROUGHPUT_50000 = 50000L;
    public static final long THROUGHPUT_100000 = 100000L;
    public static final long THROUGHPUT_0 = 0L;

    // Processing Status
    public static final String PROCESSING_STATUS_PENDING = "PENDING";
    public static final String PROCESSING_STATUS_COMPLETED = "COMPLETED";
    public static final String PROCESSING_STATUS_FAILED = "FAILED";
    public static final String VALID_PROCESSING_STATUS = "PENDING";
    public static final String ANOTHER_PROCESSING_STATUS = "COMPLETED";

    // Expected Rate Values
    public static final double RATE_0_8 = 0.8;
    public static final double RATE_0_2 = 0.2;
    public static final double RATE_0_95 = 0.95;
    public static final double RATE_0_0 = 0.0;
    public static final double RATE_50_0 = 50.0;
    public static final double RATE_DELTA = 0.0001;

    // Validation Codes and Rules
    public static final String VALIDATION_CODE_INVALID_ACCOUNT_ID = "INVALID_ACCOUNT_ID";
    public static final String VALIDATION_CODE_INVALID_AMOUNT = "INVALID_AMOUNT";
    public static final String VALIDATION_CODE_UNSUPPORTED_CURRENCY = "UNSUPPORTED_CURRENCY";
    public static final String VALIDATION_CODE_INVALID_TRANSACTION_ID = "INVALID_TRANSACTION_ID";
    public static final String VALIDATION_CODE_INVALID_TIMESTAMP = "INVALID_TIMESTAMP";
    public static final String VALIDATION_CODE_INVALID_SOURCE = "INVALID_SOURCE";
    public static final String VALIDATION_CODE_DUPLICATE_TRANSACTION = "DUPLICATE_TRANSACTION";

    public static final String REJECTED_RULE_ACCOUNT_ID_RULE = "ACCOUNT_ID_RULE";
    public static final String REJECTED_RULE_AMOUNT_RULE = "AMOUNT_RULE";
    public static final String REJECTED_RULE_CURRENCY_RULE = "CURRENCY_RULE";
    public static final String REJECTED_RULE_TRANSACTION_ID_RULE = "TRANSACTION_ID_RULE";
    public static final String REJECTED_RULE_TIMESTAMP_RULE = "TIMESTAMP_RULE";
    public static final String REJECTED_RULE_SOURCE_RULE = "SOURCE_RULE";
    public static final String REJECTED_RULE_DUPLICATE_TRANSACTION_RULE = "DUPLICATE_TRANSACTION_RULE";

    public static final String VALIDATION_STATUS_SUCCESS = "SUCCESS";
    public static final String VALIDATION_STATUS_FAILURE = "FAILURE";

    // Transaction ID values for rules tests
    public static final String TRANSACTION_ID_EMPTY = "";
    public static final String TRANSACTION_ID_BLANK = "   ";
    public static final String TRANSACTION_ID_TXN_001 = "TXN-001";
    public static final String TRANSACTION_ID_TXN_002 = "TXN-002";
    public static final String TRANSACTION_ID_TXN_003 = "TXN-003";
    public static final String TRANSACTION_ID_TXN_004 = "TXN-004";
    public static final String TRANSACTION_ID_WITH_SPECIAL_CHARS = "TXN-001-ABC-XYZ";
    public static final String TRANSACTION_ID_NEW = "TXN-NEW";
    public static final String TRANSACTION_ID_MARK = "TXN-MARK";
    public static final String TRANSACTION_ID_A = "TXN-A";
    public static final String TRANSACTION_ID_B = "TXN-B";
    public static final String TRANSACTION_ID_C = "TXN-C";
    public static final String TRANSACTION_ID_OLD_1 = "TXN-OLD-1";
    public static final String TRANSACTION_ID_OLD_2 = "TXN-OLD-2";
    public static final String TRANSACTION_ID_FRESH = "TXN-FRESH";
    public static final String TRANSACTION_ID_DUP_001 = "DUP-001";

    // Duplicate transaction validation code
    public static final String VALIDATION_CODE_DUPLICATED_TRANSACTION = "DUPLICATED_TRANSACTION";

    // Mapper test values
    public static final String TRANSACTION_ID_TRX001 = "TRX001";
    public static final String TRANSACTION_ID_TRX002 = "TRX002";
    public static final String TRANSACTION_ID_TRX003 = "TRX003";
    public static final String TRANSACTION_ID_TRX004 = "TRX004";
    public static final String TRANSACTION_ID_TRX_ABC_123 = "TRX-ABC-123";
    public static final String TRANSACTION_ID_TRX_Z = "TRX-Z";
    public static final String TRANSACTION_ID_TRX_A = "TRX-A";
    public static final String TRANSACTION_ID_TRX_M = "TRX-M";
    public static final String TRANSACTION_ID_TRX_XYZ_789 = "TRX-XYZ-789";
    public static final String ACCOUNT_ID_ACC_456 = "ACC-456";
    public static final String ACCOUNT_ID_ACCOUNT_ABC = "ACCOUNT-ABC";
    public static final String ACCOUNT_ID_ACCOUNT_XYZ = "ACCOUNT-XYZ";
    public static final String SOURCE_EXTERNAL_API = "EXTERNAL_API";
    public static final String SOURCE_INTERNAL_SYSTEM = "INTERNAL_SYSTEM";
    public static final String SOURCE_SYSTEM_A_MAPPER = "SYSTEM_A";
    public static final String SOURCE_TEST_SOURCE = "TEST_SOURCE";
    public static final String SOURCE_GENERIC = "SOURCE";
    public static final String PROCESSING_STATUS_INVALID = "INVALID_STATUS";

    // Batch processing constants
    public static final String BATCH_ID_BATCH_1 = "batch-1";
    public static final String BATCH_ID_BATCH_2 = "batch-2";
    public static final String BATCH_ID_BATCH001 = "BATCH001";
    public static final String TRANSACTION_ID_TXN_1 = "TXN-1";
    public static final String ACCOUNT_ID_ACC_1 = "ACC-1";
    public static final String ACCOUNT_ID_ACC_2 = "ACC-2";
    public static final String TRANSACTION_ID_FIRST = "FIRST";
    public static final String TRANSACTION_ID_SECOND = "SECOND";
    public static final String TRANSACTION_ID_THIRD = "THIRD";
    public static final String TRANSACTION_ID_TRX_DETAIL = "TRX-DETAIL";
    public static final String SOURCE_BATCH_SOURCE = "BATCH_SOURCE";
    public static final String SOURCE_SYSTEM_SOURCE = "SOURCE_SYSTEM";
    public static final String ERROR_CODE_DUPLICATED_TRANSACTION_IN_BATCH = "DUPLICATED_TRANSACTION_IN_BATCH";
    public static final String ERROR_CODE_INVALID_BATCH_MAPPING = "INVALID_BATCH_MAPPING";
    public static final String ERROR_CODE_VALIDATION_FAILURE = "VALIDATION_FAILURE";
    public static final String ERROR_CODE_PROCESSING_ERROR = "PROCESSING_ERROR";
    public static final String SYSTEM_SOURCE = "SYSTEM";
    public static final String ERROR_MESSAGE_AMOUNT_MUST_BE_POSITIVE = "Amount must be positive";
    public static final String ERROR_MESSAGE_CONNECTION_TIMEOUT = "Connection timeout";
    public static final String ERROR_MESSAGE_PROCESSING_ERROR = "Processing error";
    public static final String ERROR_MESSAGE_DATABASE_ERROR = "Database error";
    public static final BigDecimal AMOUNT_2500_50 = BigDecimal.valueOf(2500.50);

    // Amount values for mapper tests
    public static final BigDecimal AMOUNT_100_50 = BigDecimal.valueOf(100.50);
    public static final BigDecimal AMOUNT_1000_99 = BigDecimal.valueOf(1000.99);
    public static final BigDecimal AMOUNT_5000_75 = BigDecimal.valueOf(5000.75);
    public static final BigDecimal AMOUNT_200 = BigDecimal.valueOf(200.00);
    public static final BigDecimal AMOUNT_999_99 = BigDecimal.valueOf(999.99);

    // Processing status for mapper tests
    public static final String PROCESSING_STATUS_PROCESSING = "PROCESSING";

    // Exception validation codes
    public static final String EXCEPTION_CODE_NULL_MAPPING = "NULL_MAPPING";
    public static final String EXCEPTION_CODE_INVALID_ENTITY_MAPPING = "INVALID_ENTITY_MAPPING";

    // List size constants
    public static final int LIST_SIZE_3 = 3;
    public static final int LIST_SIZE_10000 = 10000;
    public static final int PERFORMANCE_TIME_THRESHOLD = 5000;

    private IntegrationTestConstants() {
    }
}