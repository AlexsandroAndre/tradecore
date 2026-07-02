package com.alexsandroandre.tradecore.application.dto;

import com.alexsandroandre.tradecore.domain.validation.ValidationError;

public record ValidationResult(
    boolean valid,
    ValidationError error,
    String errorMessage,
    String invalidField
) {
    public static ValidationResult success() {
        return new ValidationResult(true, null, null, null);
    }

    public static ValidationResult failure(ValidationError error, String message, String field) {
        return new ValidationResult(false, error, message, field);
    }
}
