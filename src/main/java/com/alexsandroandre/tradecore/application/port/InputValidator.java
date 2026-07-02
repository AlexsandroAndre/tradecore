package com.alexsandroandre.tradecore.application.port;

import com.alexsandroandre.tradecore.application.dto.RawTransactionData;
import com.alexsandroandre.tradecore.application.dto.ValidationResult;

public interface InputValidator {
    ValidationResult validate(RawTransactionData rawTransaction);
}
