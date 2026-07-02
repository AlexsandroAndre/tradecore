package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;

public interface ValidationRule {
    DomainValidationResult validate(Transaction transaction);

    String getRuleName();
}
