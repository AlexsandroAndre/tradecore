package com.alexsandroandre.tradecore.infrastructure.persistence.batch;

import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import com.alexsandroandre.tradecore.infrastructure.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
public class BatchInsertEngineIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
        .withDatabaseName("financial_processor_test")
        .withUsername("postgres")
        .withPassword("postgres");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("persistence.batch.batch-size", () -> "100");
    }

    @Autowired
    private BatchInsertEngine batchInsertEngine;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("Should successfully insert batch of transactions")
    void testSuccessfulBatchInsert() {
        List<TransactionEntity> transactions = createTransactions(250);

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(250);
        assertThat(result.getSuccessfulRecords()).isEqualTo(250);
        assertThat(result.getFailedRecords()).isEqualTo(0);
        assertThat(result.getDuplicateRecords()).isEqualTo(0);
        assertThat(result.getExecutionTimeMs()).isGreaterThanOrEqualTo(0);

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(250);
    }

    @Test
    @DisplayName("Should handle duplicate transaction identifiers correctly")
    void testDuplicateTransactionHandling() {
        List<TransactionEntity> transactions = new ArrayList<>();

        transactions.add(createTransaction("TXN-001"));
        transactions.add(createTransaction("TXN-001"));
        transactions.add(createTransaction("TXN-002"));

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions);

        assertThat(result.getSuccessfulRecords()).isEqualTo(2);
        assertThat(result.getDuplicateRecords()).isEqualTo(1);
        assertThat(result.getDuplicateTransactionIds()).containsExactly("TXN-001");

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Should respect configurable batch size")
    void testConfigurableBatchSize() {
        List<TransactionEntity> transactions = createTransactions(350);
        BatchInsertConfiguration config = BatchInsertConfiguration.custom(100);

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions, config);

        assertThat(result.getSuccessfulRecords()).isEqualTo(350);
        assertThat(result.isSuccess()).isTrue();

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(350);
    }

    @Test
    @DisplayName("Should flush and clear persistence context after each batch")
    void testFlushStrategy() {
        List<TransactionEntity> transactions = createTransactions(100);
        BatchInsertConfiguration config = new BatchInsertConfiguration(50, true, true, true);

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions, config);

        assertThat(result.getSuccessfulRecords()).isEqualTo(100);
        assertThat(result.isSuccess()).isTrue();

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(100);
    }

    @Test
    @DisplayName("Should process large dataset with multiple batches")
    void testLargeDatasetProcessing() {
        List<TransactionEntity> transactions = createTransactions(1500);

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions);

        assertThat(result.getTotalRecords()).isEqualTo(1500);
        assertThat(result.getSuccessfulRecords()).isEqualTo(1500);
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getThroughput()).isGreaterThan(0);

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(1500);
    }

    @Test
    @DisplayName("Should calculate throughput correctly")
    void testThroughputCalculation() {
        List<TransactionEntity> transactions = createTransactions(100);

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions);

        assertThat(result.getThroughput()).isGreaterThan(0);
        assertThat(result.getExecutionTimeMs()).isGreaterThan(0);

        double expectedThroughput = (result.getSuccessfulRecords() * 1000.0) / result.getExecutionTimeMs();
        assertThat(result.getThroughput()).isCloseTo(expectedThroughput, within(0.1));
    }

    @Test
    @DisplayName("Should handle empty transaction list")
    void testEmptyTransactionList() {
        List<TransactionEntity> transactions = new ArrayList<>();

        BatchInsertResult result = batchInsertEngine.insertBatch(transactions);

        assertThat(result.getTotalRecords()).isEqualTo(0);
        assertThat(result.getSuccessfulRecords()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reject invalid batch size configuration")
    void testInvalidBatchConfiguration() {
        assertThatThrownBy(() -> BatchInsertConfiguration.custom(0))
            .isInstanceOf(com.alexsandroandre.tradecore.infrastructure.persistence.batch.exception.InvalidBatchConfigurationException.class)
            .hasMessageContaining("Batch size must be between");

        assertThatThrownBy(() -> BatchInsertConfiguration.custom(15000))
            .isInstanceOf(com.alexsandroandre.tradecore.infrastructure.persistence.batch.exception.InvalidBatchConfigurationException.class)
            .hasMessageContaining("Batch size must be between");
    }

    private List<TransactionEntity> createTransactions(int count) {
        List<TransactionEntity> transactions = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            transactions.add(createTransaction("TXN-" + System.nanoTime() + "-" + i));
        }
        return transactions;
    }

    private TransactionEntity createTransaction(String transactionId) {
        return new TransactionEntity(
            transactionId,
            "ACC-001",
            new BigDecimal("100.50"),
            "USD",
            "API",
            Instant.now(),
            "PENDING",
            Instant.now()
        );
    }
}
