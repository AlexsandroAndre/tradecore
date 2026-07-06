CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(255) NOT NULL UNIQUE,
    account_id VARCHAR(255) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    source VARCHAR(255) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    processing_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_transaction_id ON transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_account_id ON transactions(account_id);
CREATE INDEX IF NOT EXISTS idx_processing_status ON transactions(processing_status);
CREATE INDEX IF NOT EXISTS idx_created_at ON transactions(created_at);