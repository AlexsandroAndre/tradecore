package com.alexsandroandre.tradecore.application.port;

import com.alexsandroandre.tradecore.domain.model.Transaction;

public interface TransactionPersistencePort {
    void save(Transaction transaction);
}
