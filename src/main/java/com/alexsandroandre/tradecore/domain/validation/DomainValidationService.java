package com.alexsandroandre.tradecore.domain.validation;

import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.domain.rules.AccountIdRule;
import com.alexsandroandre.tradecore.domain.rules.AmountRule;
import com.alexsandroandre.tradecore.domain.rules.CurrencyRule;
import com.alexsandroandre.tradecore.domain.rules.DuplicateTransactionRule;
import com.alexsandroandre.tradecore.domain.rules.SourceRule;
import com.alexsandroandre.tradecore.domain.rules.TimestampRule;
import com.alexsandroandre.tradecore.domain.rules.TransactionIdRule;
import com.alexsandroandre.tradecore.domain.rules.ValidationRule;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class DomainValidationService {

    private final List<ValidationRule> rules;
    private final DuplicateTransactionRule duplicateRule;

    public DomainValidationService() {
        Set<String> processedIds = new HashSet<>();
        this.duplicateRule = new DuplicateTransactionRule(processedIds);
        this.rules = new ArrayList<>();
        initializeRules();
    }

    private void initializeRules() {
        rules.add(new TransactionIdRule());
        rules.add(new AccountIdRule());
        rules.add(new AmountRule());
        rules.add(new CurrencyRule());
        rules.add(new TimestampRule());
        rules.add(new SourceRule());
        rules.add(duplicateRule);
    }

    public DomainValidationResult validate(Transaction transaction) {
        for (ValidationRule rule : rules) {
            DomainValidationResult result = rule.validate(transaction);
            if (result.isFailure()) {
                return result;
            }
        }

        duplicateRule.markAsProcessed(transaction.transactionId());
        return DomainValidationResult.success();
    }

    public List<DomainValidationResult> validateBatch(List<Transaction> transactions) {
        List<DomainValidationResult> results = new ArrayList<>();
        for (Transaction transaction : transactions) {
            results.add(validate(transaction));
        }
        return results;
    }

    public void resetProcessedIds() {
        duplicateRule.clearProcessedIds();
    }
}