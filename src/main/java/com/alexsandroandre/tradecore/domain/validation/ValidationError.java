package com.alexsandroandre.tradecore.domain.validation;

public enum ValidationError {
    INVALID_JSON("INVALID_JSON", "JSON structure is invalid"),
    MISSING_FIELD("MISSING_FIELD", "Required field is missing"),
    INVALID_FORMAT("INVALID_FORMAT", "Field format is invalid"),
    INVALID_TYPE("INVALID_TYPE", "Field type is invalid"),
    INVALID_VALUE("INVALID_VALUE", "Field value is invalid");

    private final String code;
    private final String description;

    ValidationError(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
