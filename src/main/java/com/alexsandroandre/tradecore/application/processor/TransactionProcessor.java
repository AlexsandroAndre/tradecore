package com.alexsandroandre.tradecore.application.processor;

import com.alexsandroandre.tradecore.domain.model.Transaction;

public interface TransactionProcessor {
    ProcessingResult process(Transaction transaction);

    record ProcessingResult(
        boolean success,
        Transaction transaction,
        String errorMessage,
        String errorCode
    ) {
        public static ProcessingResult success(Transaction transaction) {
            return new ProcessingResult(true, transaction, null, null);
        }

        public static ProcessingResult failure(Transaction transaction, String errorCode, String errorMessage) {
            return new ProcessingResult(false, transaction, errorMessage, errorCode);
        }
    }
}
