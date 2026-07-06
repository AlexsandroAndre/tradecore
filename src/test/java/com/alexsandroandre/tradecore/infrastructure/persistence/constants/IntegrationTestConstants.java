package com.alexsandroandre.tradecore.infrastructure.persistence.constants;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class IntegrationTestConstants {

    public static final String DATABASE_NAME = "transaction_db";
    public static final String DATABASE_USERNAME = "postgres";
    public static final String DATABASE_PASSWORD = "postgres";

    public static final String VALID_TRANSACTION_ID = UUID.randomUUID().toString();
    public static final String VALID_ACCOUNT_ID = "ACC-001";
    public static final BigDecimal VALID_AMOUNT = BigDecimal.valueOf(1000.00);
    public static final String VALID_CURRENCY = "USD";
    public static final String VALID_SOURCE = "IMPORT";
    public static final Instant VALID_TIMESTAMP = Instant.now();
    public static final String VALID_PROCESSING_STATUS = "PENDING";
    public static final Instant VALID_CREATED_AT = Instant.now();

    public static final String ANOTHER_TRANSACTION_ID = UUID.randomUUID().toString();
    public static final String ANOTHER_ACCOUNT_ID = "ACC-002";
    public static final BigDecimal ANOTHER_AMOUNT = BigDecimal.valueOf(500.50);
    public static final String ANOTHER_CURRENCY = "EUR";
    public static final String ANOTHER_SOURCE = "API";
    public static final Instant ANOTHER_TIMESTAMP = Instant.now().minusSeconds(3600);
    public static final String ANOTHER_PROCESSING_STATUS = "COMPLETED";

    public static final String PROCESSING_STATUS_PENDING = "PENDING";
    public static final String PROCESSING_STATUS_COMPLETED = "COMPLETED";
    public static final String PROCESSING_STATUS_FAILED = "FAILED";

    private IntegrationTestConstants() {
    }
}
