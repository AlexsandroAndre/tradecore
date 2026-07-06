package com.alexsandroandre.tradecore.infrastructure.persistence.batch;

import com.alexsandroandre.tradecore.infrastructure.persistence.batch.exception.InvalidBatchConfigurationException;

public record BatchInsertConfiguration(
    int batchSize,
    boolean flushAfterBatch,
    boolean clearContextAfterFlush,
    boolean orderInserts
) {
    private static final int MIN_BATCH_SIZE = 1;
    private static final int MAX_BATCH_SIZE = 10000;
    private static final int DEFAULT_BATCH_SIZE = 500;

    public BatchInsertConfiguration {
        if (batchSize < MIN_BATCH_SIZE || batchSize > MAX_BATCH_SIZE) {
            throw new InvalidBatchConfigurationException(
                "INVALID_BATCH_CONFIGURATION",
                "Batch size must be between " + MIN_BATCH_SIZE + " and " + MAX_BATCH_SIZE + ", got: " + batchSize
            );
        }
    }

    public static BatchInsertConfiguration withDefaults() {
        return new BatchInsertConfiguration(DEFAULT_BATCH_SIZE, true, true, true);
    }

    public static BatchInsertConfiguration custom(int batchSize) {
        return new BatchInsertConfiguration(batchSize, true, true, true);
    }
}
