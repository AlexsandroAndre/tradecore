package com.alexsandroandre.tradecore.interfaces.api.request;

import java.util.List;

public record ProcessTransactionRequest(
    List<TransactionInput> transactions
) {
    public record TransactionInput(
        String transactionId,
        String accountId,
        Double amount,
        String currency,
        String timestamp,
        String source
    ) {
    }
}
