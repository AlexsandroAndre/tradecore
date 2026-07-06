package com.alexsandroandre.tradecore.infrastructure.persistence.batch;

import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import com.alexsandroandre.tradecore.infrastructure.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
public class BatchInsertServiceIntegrationTest {

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
        registry.add("persistence.batch.batch-size", () -> "250");
    }

    @Autowired
    private BatchInsertService batchInsertService;

    @Autowired
    private TransactionRepository transactionRepository;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("Should persist transactions using default batch size")
    void testPersistTransactionsWithDefaultBatchSize() {
        List<TransactionEntity> transactions = createTransactions(500);

        BatchInsertResult result = batchInsertService.persistTransactions(transactions);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(500);
        assertThat(result.getSuccessfulRecords()).isEqualTo(500);

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(500);
    }

    @Test
    @DisplayName("Should persist transactions using custom batch size")
    void testPersistTransactionsWithCustomBatchSize() {
        List<TransactionEntity> transactions = createTransactions(300);

        BatchInsertResult result = batchInsertService.persistTransactions(transactions, 75);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getSuccessfulRecords()).isEqualTo(300);

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(300);
    }

    @Test
    @DisplayName("Should handle empty transaction list gracefully")
    void testPersistEmptyTransactionList() {
        List<TransactionEntity> transactions = new ArrayList<>();

        BatchInsertResult result = batchInsertService.persistTransactions(transactions);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(0);

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle null transaction list gracefully")
    void testPersistNullTransactionList() {
        BatchInsertResult result = batchInsertService.persistTransactions(null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should reject invalid custom batch size")
    void testRejectInvalidCustomBatchSize() {
        List<TransactionEntity> transactions = createTransactions(100);

        assertThatThrownBy(() -> batchInsertService.persistTransactions(transactions, 0))
            .isInstanceOf(com.alexsandroandre.tradecore.infrastructure.persistence.batch.exception.InvalidBatchConfigurationException.class);

        assertThatThrownBy(() -> batchInsertService.persistTransactions(transactions, 15000))
            .isInstanceOf(com.alexsandroandre.tradecore.infrastructure.persistence.batch.exception.InvalidBatchConfigurationException.class);
    }

    @Test
    @DisplayName("Should provide default configuration")
    void testGetDefaultConfiguration() {
        BatchInsertConfiguration config = batchInsertService.getDefaultConfiguration();

        assertThat(config).isNotNull();
        assertThat(config.batchSize()).isGreaterThan(0);
        assertThat(config.flushAfterBatch()).isTrue();
        assertThat(config.clearContextAfterFlush()).isTrue();
    }

    @Test
    @DisplayName("Should process large dataset efficiently")
    void testProcessLargeDataset() {
        List<TransactionEntity> transactions = createTransactions(2000);

        BatchInsertResult result = batchInsertService.persistTransactions(transactions);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getTotalRecords()).isEqualTo(2000);
        assertThat(result.getSuccessfulRecords()).isEqualTo(2000);
        assertThat(result.getThroughput()).isGreaterThan(0);

        long persistedCount = transactionRepository.count();
        assertThat(persistedCount).isEqualTo(2000);
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
            "ACC-002",
            new BigDecimal("250.75"),
            "USD",
            "API",
            Instant.now(),
            "PENDING",
            Instant.now()
        );
    }
}
