package com.alexsandroandre.tradecore.infrastructure.persistence.batch.exception;

public class BatchTransactionException extends RuntimeException {
    private final String code;
    private final int batchNumber;

    public BatchTransactionException(String code, int batchNumber, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.batchNumber = batchNumber;
    }

    public String getCode() {
        return code;
    }

    public int getBatchNumber() {
        return batchNumber;
    }
}
