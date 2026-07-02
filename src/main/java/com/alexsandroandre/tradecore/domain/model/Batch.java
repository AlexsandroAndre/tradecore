package com.alexsandroandre.tradecore.domain.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public record Batch(
    String batchId,
    List<Transaction> transactions,
    int batchSize
) {

    public static final String TRANSACTIONS_CANNOT_BE_NULL = "Transactions cannot be null";
    public static final String BATCH_SIZE_MUST_BE_GREATER_THAN_ZERO = "Batch size must be greater than zero";
    public static final String TRANSACTIONS_EXCEED_BATCH_SIZE = "Transactions exceed batch size";

    public Batch {
        if (transactions == null) {
            throw new IllegalArgumentException(TRANSACTIONS_CANNOT_BE_NULL);
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException(BATCH_SIZE_MUST_BE_GREATER_THAN_ZERO);
        }
        if (transactions.size() > batchSize) {
            throw new IllegalArgumentException(TRANSACTIONS_EXCEED_BATCH_SIZE);
        }
    }

    public boolean hasDuplicates() {
        Set<String> seenIds = new HashSet<>();
        for (Transaction transaction : transactions) {
            if (!seenIds.add(transaction.transactionId())) {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    public int size() {
        return transactions.size();
    }

    public List<Transaction> getTransactions() {
        return Collections.unmodifiableList(transactions);
    }
}
