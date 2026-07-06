package com.alexsandroandre.tradecore.domain.rules;

import com.alexsandroandre.tradecore.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TransactionTestBuilder {

    private String transactionId = "TXN-001";
    private String accountId = "ACC-123";
    private BigDecimal amount = new BigDecimal("100.50");
    private String currency = "USD";
    private OffsetDateTime timestamp = OffsetDateTime.now().minusHours(1);
    private String source = "external-bank";
    private Transaction.TransactionStatus status = Transaction.TransactionStatus.PENDING;

    public TransactionTestBuilder withTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public TransactionTestBuilder withAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }

    public TransactionTestBuilder withAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionTestBuilder withCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public TransactionTestBuilder withTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public TransactionTestBuilder withSource(String source) {
        this.source = source;
        return this;
    }

    public TransactionTestBuilder withStatus(Transaction.TransactionStatus status) {
        this.status = status;
        return this;
    }

    public Transaction build() {
        return new Transaction(
            transactionId,
            accountId,
            amount,
            currency,
            timestamp,
            source,
            status
        );
    }

    public Transaction buildValidTransaction() {
        return new TransactionTestBuilder().build();
    }

    public Transaction buildWithNullTransactionId() {
        return new TransactionTestBuilder()
            .withTransactionId(null)
            .build();
    }

    public Transaction buildWithEmptyTransactionId() {
        return new TransactionTestBuilder()
            .withTransactionId("")
            .build();
    }

    public Transaction buildWithBlankTransactionId() {
        return new TransactionTestBuilder()
            .withTransactionId("   ")
            .build();
    }

    public Transaction buildWithNullAccountId() {
        return new TransactionTestBuilder()
            .withAccountId(null)
            .build();
    }

    public Transaction buildWithEmptyAccountId() {
        return new TransactionTestBuilder()
            .withAccountId("")
            .build();
    }

    public Transaction buildWithBlankAccountId() {
        return new TransactionTestBuilder()
            .withAccountId("   ")
            .build();
    }

    public Transaction buildWithNullAmount() {
        return new TransactionTestBuilder()
            .withAmount(null)
            .build();
    }

    public Transaction buildWithZeroAmount() {
        return new TransactionTestBuilder()
            .withAmount(BigDecimal.ZERO)
            .build();
    }

    public Transaction buildWithNegativeAmount() {
        return new TransactionTestBuilder()
            .withAmount(new BigDecimal("-50.00"))
            .build();
    }

    public Transaction buildWithPositiveAmount() {
        return new TransactionTestBuilder()
            .withAmount(new BigDecimal("1000.99"))
            .build();
    }

    public Transaction buildWithNullCurrency() {
        return new TransactionTestBuilder()
            .withCurrency(null)
            .build();
    }

    public Transaction buildWithUnsupportedCurrency() {
        return new TransactionTestBuilder()
            .withCurrency("XXX")
            .build();
    }

    public Transaction buildWithInvalidCurrencyFormat() {
        return new TransactionTestBuilder()
            .withCurrency("USDA")
            .build();
    }

    public Transaction buildWithLowercaseCurrency() {
        return new TransactionTestBuilder()
            .withCurrency("usd")
            .build();
    }

    public Transaction buildWithNullTimestamp() {
        return new TransactionTestBuilder()
            .withTimestamp(null)
            .build();
    }

    public Transaction buildWithFutureTimestamp() {
        return new TransactionTestBuilder()
            .withTimestamp(OffsetDateTime.now().plusHours(1))
            .build();
    }

    public Transaction buildWithPastTimestamp() {
        return new TransactionTestBuilder()
            .withTimestamp(OffsetDateTime.now().minusDays(10))
            .build();
    }

    public Transaction buildWithNullSource() {
        return new TransactionTestBuilder()
            .withSource(null)
            .build();
    }

    public Transaction buildWithEmptySource() {
        return new TransactionTestBuilder()
            .withSource("")
            .build();
    }

    public Transaction buildWithBlankSource() {
        return new TransactionTestBuilder()
            .withSource("   ")
            .build();
    }
}
