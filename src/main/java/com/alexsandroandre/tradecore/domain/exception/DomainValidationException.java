package com.alexsandroandre.tradecore.domain.exception;

public class DomainValidationException extends RuntimeException {
    private final String validationCode;
    private final String validationMessage;
    private final String rejectedRule;

    public DomainValidationException(String code, String message, String rule) {
        super(message);
        this.validationCode = code;
        this.validationMessage = message;
        this.rejectedRule = rule;
    }

    public DomainValidationException(String code, String message, String rule, Throwable cause) {
        super(message, cause);
        this.validationCode = code;
        this.validationMessage = message;
        this.rejectedRule = rule;
    }

    public String getValidationCode() {
        return validationCode;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    public String getRejectedRule() {
        return rejectedRule;
    }
}
