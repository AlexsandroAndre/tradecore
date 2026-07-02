package com.alexsandroandre.tradecore.domain.validation;

public record DomainValidationResult(
    ValidationStatus status,
    String validationCode,
    String validationMessage,
    String rejectedRule
) {

    public enum ValidationStatus {
        SUCCESS,
        FAILURE
    }

    public static DomainValidationResult success() {
        return new DomainValidationResult(
            ValidationStatus.SUCCESS,
            null,
            null,
            null
        );
    }

    public static DomainValidationResult failure(String code, String message, String rule) {
        return new DomainValidationResult(
            ValidationStatus.FAILURE,
            code,
            message,
            rule
        );
    }

    public boolean isSuccess() {
        return status == ValidationStatus.SUCCESS;
    }

    public boolean isFailure() {
        return status == ValidationStatus.FAILURE;
    }
}
