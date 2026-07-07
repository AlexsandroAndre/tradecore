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

CREATE TABLE IF NOT EXISTS processing_metrics (
    id UUID PRIMARY KEY,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    total_records_processed BIGINT NOT NULL,
    successful_records BIGINT NOT NULL,
    failed_records BIGINT NOT NULL,
    duplicate_records BIGINT NOT NULL,
    total_duration_millis BIGINT NOT NULL,
    throughput NUMERIC(19, 2) NOT NULL,
    average_latency_millis NUMERIC(19, 2) NOT NULL,
    peak_memory_usage_bytes BIGINT NOT NULL,
    average_memory_usage_bytes BIGINT NOT NULL,
    validation_errors BIGINT NOT NULL,
    processing_errors BIGINT NOT NULL,
    system_errors BIGINT NOT NULL,
    duplicate_errors BIGINT NOT NULL,
    batch_size INT NOT NULL,
    batch_count BIGINT NOT NULL,
    slowest_batch_millis BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_metrics_start_time ON processing_metrics(start_time);
CREATE INDEX IF NOT EXISTS idx_metrics_created_at ON processing_metrics(created_at);