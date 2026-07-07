package com.alexsandroandre.tradecore.interfaces.api.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProcessingResponse(
    UUID processingId,
    long totalRecordsProcessed,
    long successfulRecords,
    long failedRecords,
    long duplicateRecords,
    long totalDurationMillis,
    BigDecimal throughput,
    long validationErrors,
    long processingErrors,
    long systemErrors,
    long duplicateErrors,
    String status,
    String message
) {
}
