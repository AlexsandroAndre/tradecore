package com.alexsandroandre.tradecore.domain.exception;

public class MalformedRecordException extends RuntimeException {
    private final int recordIndex;

    public MalformedRecordException(String message, int recordIndex) {
        super(message);
        this.recordIndex = recordIndex;
    }

    public MalformedRecordException(String message, int recordIndex, Throwable cause) {
        super(message, cause);
        this.recordIndex = recordIndex;
    }

    public int getRecordIndex() {
        return recordIndex;
    }
}