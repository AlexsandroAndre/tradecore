package com.alexsandroandre.tradecore.infrastructure.persistence.batch.exception;

public class InvalidBatchConfigurationException extends RuntimeException {
    private final String code;

    public InvalidBatchConfigurationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
