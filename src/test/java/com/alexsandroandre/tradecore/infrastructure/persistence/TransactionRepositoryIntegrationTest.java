package com.alexsandroandre.tradecore.infrastructure.persistence;

import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import com.alexsandroandre.tradecore.infrastructure.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
public class TransactionRepositoryIntegrationTest {

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
    }

    @Autowired
    private TransactionRepository transactionRepository;

    private TransactionEntity testTransaction;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        testTransaction = new TransactionEntity(
            "TXN-" + System.currentTimeMillis(),
            "ACC-123",
            new BigDecimal("1000.00"),
            "USD",
            "API",
            Instant.now(),
            "PENDING",
            Instant.now()
        );
    }

    @Test
    void testPostgresqlConnection() {
        assertThat(postgres.isRunning()).isTrue();
    }

    @Test
    void testInsertTransaction() {
        TransactionEntity savedTransaction = transactionRepository.save(testTransaction);

        assertThat(savedTransaction.getId()).isNotNull();
        assertThat(savedTransaction.getTransactionId()).startsWith("TXN-");
        assertThat(savedTransaction.getAccountId()).isEqualTo("ACC-123");
    }

    @Test
    void testFindByTransactionId() {
        String txnId = testTransaction.getTransactionId();
        transactionRepository.save(testTransaction);

        Optional<TransactionEntity> found = transactionRepository.findByTransactionId(txnId);

        assertThat(found).isPresent();
        assertThat(found.get().getAccountId()).isEqualTo("ACC-123");
        assertThat(found.get().getAmount()).isEqualByComparingTo(new BigDecimal("1000.00"));
    }

    @Test
    void testUniqueConstraintOnTransactionId() {
        String txnId = testTransaction.getTransactionId();
        transactionRepository.save(testTransaction);

        TransactionEntity duplicate = new TransactionEntity(
            txnId,
            "ACC-456",
            new BigDecimal("2000.00"),
            "USD",
            "API",
            Instant.now(),
            "PENDING",
            Instant.now()
        );

        assertThatThrownBy(() -> {
            transactionRepository.save(duplicate);
            transactionRepository.flush();
        })
            .isNotNull();
    }

    @Test
    void testFindByAccountId() {
        transactionRepository.save(testTransaction);

        var transactions = transactionRepository.findByAccountId("ACC-123");

        assertThat(transactions).hasSize(1);
        assertThat(transactions.get(0).getTransactionId()).isEqualTo(testTransaction.getTransactionId());
    }

    @Test
    void testFindByProcessingStatus() {
        transactionRepository.save(testTransaction);

        var transactions = transactionRepository.findByProcessingStatus("PENDING");

        assertThat(transactions).isNotEmpty();
        assertThat(transactions.get(0).getProcessingStatus()).isEqualTo("PENDING");
    }

    @Test
    void testExistsByTransactionId() {
        String txnId = testTransaction.getTransactionId();
        transactionRepository.save(testTransaction);

        boolean exists = transactionRepository.existsByTransactionId(txnId);

        assertThat(exists).isTrue();
    }

    @Test
    void testBatchInsert() {
        TransactionEntity txn1 = new TransactionEntity(
            "TXN-B-" + System.currentTimeMillis() + "-1",
            "ACC-B001",
            new BigDecimal("5000.00"),
            "USD",
            "BATCH",
            Instant.now(),
            "PENDING",
            Instant.now()
        );
        TransactionEntity txn2 = new TransactionEntity(
            "TXN-B-" + System.currentTimeMillis() + "-2",
            "ACC-B001",
            new BigDecimal("7000.00"),
            "USD",
            "BATCH",
            Instant.now(),
            "PENDING",
            Instant.now()
        );

        var saved = transactionRepository.saveAll(java.util.List.of(txn1, txn2));

        assertThat(saved).hasSize(2);
        assertThat(transactionRepository.count()).isGreaterThanOrEqualTo(2);
        assertThat(transactionRepository.count()).isGreaterThanOrEqualTo(2);
    }
}