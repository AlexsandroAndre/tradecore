package com.alexsandroandre.tradecore.application.dto;

import java.time.Instant;
import java.util.Map;

public record RawTransactionData(
    String transactionId,
    String accountId,
    Double amount,
    String currency,
    String timestamp,
    String source,
    Map<String, Object> additionalFields
) {
    public RawTransactionData(
        String transactionId,
        String accountId,
        Double amount,
        String currency,
        String timestamp,
        String source
    ) {
        this(transactionId, accountId, amount, currency, timestamp, source, Map.of());
    }
}