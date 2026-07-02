package com.alexsandroandre.tradecore.application.processor;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationResult;
import com.alexsandroandre.tradecore.domain.validation.DomainValidationService;

public final class RecordProcessor implements TransactionProcessor {

    private final DomainValidationService validationService;

    public RecordProcessor(DomainValidationService validationService) {
        this.validationService = validationService;
    }

    @Override
    public ProcessingResult process(Transaction transaction) {
        DomainValidationResult validationResult = validationService.validate(transaction);

        if (validationResult.isFailure()) {
            return ProcessingResult.failure(
                transaction.withStatus(Transaction.TransactionStatus.FAILED),
                validationResult.validationCode(),
                validationResult.validationMessage()
            );
        }

        Transaction processedTransaction = transaction.withStatus(Transaction.TransactionStatus.COMPLETED);
        return ProcessingResult.success(processedTransaction);
    }
}
