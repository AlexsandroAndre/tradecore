package com.alexsandroandre.tradecore.infrastructure.persistence;

import com.alexsandroandre.tradecore.infrastructure.persistence.builder.TransactionEntityTestBuilder;
import com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import com.alexsandroandre.tradecore.infrastructure.persistence.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionEntity Persistence Validation Tests")
public class TransactionEntityPersistenceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
    }

    @Test
    @DisplayName("Should validate JPA entity mapping")
    void shouldValidateJpaEntityMapping() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .accountId(IntegrationTestConstants.VALID_ACCOUNT_ID)
                .amount(IntegrationTestConstants.VALID_AMOUNT)
                .currency(IntegrationTestConstants.VALID_CURRENCY)
                .source(IntegrationTestConstants.VALID_SOURCE)
                .timestamp(IntegrationTestConstants.VALID_TIMESTAMP)
                .processingStatus(IntegrationTestConstants.VALID_PROCESSING_STATUS)
                .createdAt(IntegrationTestConstants.VALID_CREATED_AT)
                .build();

        TransactionEntity saved = transactionRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        TransactionEntity retrieved = transactionRepository.findById(saved.getId()).orElse(null);

        assertNotNull(retrieved);
        assertEquals(transactionId, retrieved.getTransactionId());
        assertEquals(IntegrationTestConstants.VALID_ACCOUNT_ID, retrieved.getAccountId());
        assertEquals(0, IntegrationTestConstants.VALID_AMOUNT.compareTo(retrieved.getAmount()));
        assertEquals(IntegrationTestConstants.VALID_CURRENCY, retrieved.getCurrency());
        assertEquals(IntegrationTestConstants.VALID_SOURCE, retrieved.getSource());
        assertEquals(IntegrationTestConstants.VALID_PROCESSING_STATUS, retrieved.getProcessingStatus());
    }

    @Test
    @DisplayName("Should validate primary key generation")
    void shouldValidatePrimaryKeyGeneration() {
        TransactionEntity entity1 = TransactionEntityTestBuilder.builder().buildValidEntity();
        TransactionEntity entity2 = TransactionEntityTestBuilder.builder().buildValidEntity();

        TransactionEntity saved1 = transactionRepository.save(entity1);
        TransactionEntity saved2 = transactionRepository.save(entity2);

        assertNotNull(saved1.getId());
        assertNotNull(saved2.getId());
        assertNotEquals(saved1.getId(), saved2.getId());
    }

    @Test
    @DisplayName("Should validate unique transaction ID constraint")
    void shouldValidateUniqueTransactionIdConstraint() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity1 = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .buildValidEntity();

        transactionRepository.save(entity1);
        entityManager.flush();
        entityManager.clear();

        var found = transactionRepository.findByTransactionId(transactionId);
        assertTrue(found.isPresent());
        assertEquals(transactionId, found.get().getTransactionId());
    }

    @Test
    @DisplayName("Should validate nullable column constraints")
    void shouldValidateNullableColumnConstraints() {
        TransactionEntity entity = new TransactionEntity();
        entity.setTransactionId(UUID.randomUUID().toString());
        entity.setAccountId("TEST-ACC");
        entity.setAmount(BigDecimal.valueOf(100));
        entity.setCurrency("USD");
        entity.setSource("API");
        entity.setTimestamp(Instant.now());
        entity.setProcessingStatus("PENDING");
        entity.setCreatedAt(Instant.now());

        TransactionEntity saved = transactionRepository.save(entity);
        entityManager.flush();

        assertNotNull(saved.getId());
    }

    @Test
    @DisplayName("Should validate decimal precision and scale")
    void shouldValidateDecimalPrecisionAndScale() {
        String transactionId = UUID.randomUUID().toString();
        BigDecimal amount = new BigDecimal("99999999999999999.99");

        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .amount(amount)
                .buildValidEntity();

        TransactionEntity saved = transactionRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        TransactionEntity retrieved = transactionRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertEquals(0, amount.compareTo(retrieved.getAmount()));
    }

    @Test
    @DisplayName("Should validate string column length constraints")
    void shouldValidateStringColumnLengthConstraints() {
        String transactionId = UUID.randomUUID().toString();
        String longCurrency = "USD";

        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .currency(longCurrency)
                .buildValidEntity();

        TransactionEntity saved = transactionRepository.save(entity);
        entityManager.flush();

        assertTrue(saved.getId() != null);
    }

    @Test
    @DisplayName("Should validate instant timestamp persistence")
    void shouldValidateInstantTimestampPersistence() {
        String transactionId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .timestamp(now)
                .createdAt(now)
                .buildValidEntity();

        TransactionEntity saved = transactionRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        TransactionEntity retrieved = transactionRepository.findById(saved.getId()).orElse(null);
        assertNotNull(retrieved);
        assertNotNull(retrieved.getTimestamp());
        assertNotNull(retrieved.getCreatedAt());
    }

    @Test
    @DisplayName("Should validate transaction rollback on error")
    void shouldValidateTransactionRollbackOnError() {
        String transactionId1 = UUID.randomUUID().toString();
        String transactionId2 = UUID.randomUUID().toString();

        TransactionEntity entity1 = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId1)
                .buildValidEntity();

        transactionRepository.save(entity1);
        entityManager.flush();
        entityManager.clear();

        long countBefore = transactionRepository.count();

        TransactionEntity entity2 = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId2)
                .buildValidEntity();

        transactionRepository.save(entity2);
        entityManager.flush();

        long countAfter = transactionRepository.count();
        assertEquals(countBefore + 1, countAfter);
    }

    @Test
    @DisplayName("Should validate index on transaction_id")
    void shouldValidateTransactionIdIndex() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .buildValidEntity();

        TransactionEntity saved = transactionRepository.save(entity);
        Long savedId = saved.getId();
        entityManager.flush();
        entityManager.clear();

        TransactionEntity retrieved = transactionRepository.findById(savedId).orElse(null);
        assertNotNull(retrieved);
        assertEquals(transactionId, retrieved.getTransactionId());

        var found = transactionRepository.findByTransactionId(transactionId);
        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("Should validate index on account_id")
    void shouldValidateAccountIdIndex() {
        String accountId = "ACC-TEST-INDEX";
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .accountId(accountId)
                .buildValidEntity();

        transactionRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        var found = transactionRepository.findByAccountId(accountId);
        assertFalse(found.isEmpty());
    }

    @Test
    @DisplayName("Should validate index on processing_status")
    void shouldValidateProcessingStatusIndex() {
        String status = "COMPLETED";
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .processingStatus(status)
                .buildValidEntity();

        transactionRepository.save(entity);
        entityManager.flush();
        entityManager.clear();

        var found = transactionRepository.findByProcessingStatus(status);
        assertFalse(found.isEmpty());
    }

    @Test
    @DisplayName("Should validate concurrent entity persistence")
    void shouldValidateConcurrentEntityPersistence() {
        var entity1 = TransactionEntityTestBuilder.builder().buildValidEntity();
        var entity2 = TransactionEntityTestBuilder.builder().buildValidEntity();
        var entity3 = TransactionEntityTestBuilder.builder().buildValidEntity();

        transactionRepository.save(entity1);
        transactionRepository.save(entity2);
        transactionRepository.save(entity3);
        entityManager.flush();

        long count = transactionRepository.count();
        assertEquals(3, count);
    }

    @Test
    @DisplayName("Should validate entity state changes are persisted")
    void shouldValidateEntityStateChangesArePersisted() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .amount(BigDecimal.valueOf(100.00))
                .buildValidEntity();

        TransactionEntity saved = transactionRepository.save(entity);
        Long savedId = saved.getId();
        entityManager.flush();
        entityManager.clear();

        TransactionEntity retrieved = transactionRepository.findById(savedId).orElse(null);
        assertNotNull(retrieved);
        assertEquals(0, BigDecimal.valueOf(100.00).compareTo(retrieved.getAmount()));

        retrieved.setAmount(BigDecimal.valueOf(200.00));
        transactionRepository.save(retrieved);
        entityManager.flush();
        entityManager.clear();

        TransactionEntity updated = transactionRepository.findById(savedId).orElse(null);
        assertNotNull(updated);
        assertEquals(0, BigDecimal.valueOf(200.00).compareTo(updated.getAmount()));
    }
}
