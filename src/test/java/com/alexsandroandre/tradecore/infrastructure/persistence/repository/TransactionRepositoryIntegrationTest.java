package com.alexsandroandre.tradecore.infrastructure.persistence.repository;

import com.alexsandroandre.tradecore.infrastructure.persistence.BaseIntegrationTest;
import com.alexsandroandre.tradecore.infrastructure.persistence.builder.TransactionEntityTestBuilder;
import com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TransactionRepository Integration Tests")
public class TransactionRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    private TransactionEntity validEntity;

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        validEntity = TransactionEntityTestBuilder.builder().buildValidEntity();
    }

    @Test
    @DisplayName("Should save a valid entity")
    void shouldSaveValidEntity() {
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(IntegrationTestConstants.VALID_TRANSACTION_ID)
                .accountId(IntegrationTestConstants.VALID_ACCOUNT_ID)
                .amount(IntegrationTestConstants.VALID_AMOUNT)
                .currency(IntegrationTestConstants.VALID_CURRENCY)
                .source(IntegrationTestConstants.VALID_SOURCE)
                .timestamp(IntegrationTestConstants.VALID_TIMESTAMP)
                .processingStatus(IntegrationTestConstants.VALID_PROCESSING_STATUS)
                .createdAt(IntegrationTestConstants.VALID_CREATED_AT)
                .build();

        TransactionEntity saved = transactionRepository.save(entity);

        assertNotNull(saved.getId());
        assertEquals(IntegrationTestConstants.VALID_TRANSACTION_ID, saved.getTransactionId());
        assertEquals(IntegrationTestConstants.VALID_ACCOUNT_ID, saved.getAccountId());
        assertEquals(IntegrationTestConstants.VALID_AMOUNT, saved.getAmount());
    }

    @Test
    @DisplayName("Should find entity by transaction ID")
    void shouldFindByTransactionId() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .buildValidEntity();

        transactionRepository.save(entity);

        Optional<TransactionEntity> found = transactionRepository.findByTransactionId(transactionId);

        assertTrue(found.isPresent());
        assertEquals(transactionId, found.get().getTransactionId());
    }

    @Test
    @DisplayName("Should find entities by account ID")
    void shouldFindByAccountId() {
        String accountId = "ACC-TEST-001";
        TransactionEntity entity1 = TransactionEntityTestBuilder.builder()
                .transactionId(UUID.randomUUID().toString())
                .accountId(accountId)
                .buildValidEntity();
        TransactionEntity entity2 = TransactionEntityTestBuilder.builder()
                .transactionId(UUID.randomUUID().toString())
                .accountId(accountId)
                .buildValidEntity();

        transactionRepository.save(entity1);
        transactionRepository.save(entity2);

        List<TransactionEntity> found = transactionRepository.findByAccountId(accountId);

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(t -> t.getAccountId().equals(accountId)));
    }

    @Test
    @DisplayName("Should find entities by processing status")
    void shouldFindByProcessingStatus() {
        String status = IntegrationTestConstants.PROCESSING_STATUS_COMPLETED;
        TransactionEntity entity1 = TransactionEntityTestBuilder.builder()
                .transactionId(UUID.randomUUID().toString())
                .processingStatus(status)
                .buildValidEntity();
        TransactionEntity entity2 = TransactionEntityTestBuilder.builder()
                .transactionId(UUID.randomUUID().toString())
                .processingStatus(status)
                .buildValidEntity();

        transactionRepository.save(entity1);
        transactionRepository.save(entity2);

        List<TransactionEntity> found = transactionRepository.findByProcessingStatus(status);

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(t -> t.getProcessingStatus().equals(status)));
    }

    @Test
    @DisplayName("Should update entity")
    void shouldUpdateEntity() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .processingStatus(IntegrationTestConstants.PROCESSING_STATUS_PENDING)
                .buildValidEntity();

        TransactionEntity saved = transactionRepository.save(entity);
        saved.setProcessingStatus(IntegrationTestConstants.PROCESSING_STATUS_COMPLETED);
        TransactionEntity updated = transactionRepository.save(saved);

        assertEquals(IntegrationTestConstants.PROCESSING_STATUS_COMPLETED, updated.getProcessingStatus());
        assertEquals(saved.getId(), updated.getId());
    }

    @Test
    @DisplayName("Should delete entity")
    void shouldDeleteEntity() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .buildValidEntity();

        TransactionEntity saved = transactionRepository.save(entity);
        transactionRepository.delete(saved);

        Optional<TransactionEntity> found = transactionRepository.findByTransactionId(transactionId);
        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Should check existence by transaction ID")
    void shouldCheckExistenceByTransactionId() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .buildValidEntity();

        transactionRepository.save(entity);

        assertTrue(transactionRepository.existsByTransactionId(transactionId));
        assertFalse(transactionRepository.existsByTransactionId(UUID.randomUUID().toString()));
    }

    @Test
    @DisplayName("Should perform batch insert")
    void shouldPerformBatchInsert() {
        List<TransactionEntity> entities = TransactionEntityTestBuilder.builder()
                .buildBatchEntities(10);

        List<TransactionEntity> saved = transactionRepository.saveAll(entities);

        assertEquals(10, saved.size());
        assertTrue(saved.stream().allMatch(t -> t.getId() != null));
    }

    @Test
    @DisplayName("Should find entities by date range")
    void shouldFindByDateRange() {
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);
        Instant twoHoursAgo = now.minusSeconds(7200);

        TransactionEntity entity1 = TransactionEntityTestBuilder.builder()
                .transactionId(UUID.randomUUID().toString())
                .timestamp(oneHourAgo)
                .createdAt(oneHourAgo)
                .buildValidEntity();
        TransactionEntity entity2 = TransactionEntityTestBuilder.builder()
                .transactionId(UUID.randomUUID().toString())
                .timestamp(twoHoursAgo)
                .createdAt(twoHoursAgo)
                .buildValidEntity();

        transactionRepository.save(entity1);
        transactionRepository.save(entity2);

        List<TransactionEntity> found = transactionRepository.findByDateRange(twoHoursAgo, now);

        assertEquals(2, found.size());
    }

    @Test
    @DisplayName("Should enforce unique transaction ID constraint")
    void shouldEnforceUniqueTransactionIdConstraint() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity1 = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .buildValidEntity();
        TransactionEntity entity2 = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .buildValidEntity();

        transactionRepository.save(entity1);

        assertThrows(Exception.class, () -> {
            transactionRepository.save(entity2);
            transactionRepository.flush();
        });
    }

    @Test
    @DisplayName("Should maintain data consistency after multiple operations")
    void shouldMaintainDataConsistencyAfterMultipleOperations() {
        String transactionId = UUID.randomUUID().toString();
        TransactionEntity entity = TransactionEntityTestBuilder.builder()
                .transactionId(transactionId)
                .amount(IntegrationTestConstants.VALID_AMOUNT)
                .currency(IntegrationTestConstants.VALID_CURRENCY)
                .buildValidEntity();

        transactionRepository.save(entity);

        entity.setAmount(BigDecimal.valueOf(2000.00));
        entity.setCurrency(IntegrationTestConstants.ANOTHER_CURRENCY);
        transactionRepository.save(entity);

        Optional<TransactionEntity> found = transactionRepository.findByTransactionId(transactionId);
        assertTrue(found.isPresent());
        assertEquals(BigDecimal.valueOf(2000.00), found.get().getAmount());
        assertEquals(IntegrationTestConstants.ANOTHER_CURRENCY, found.get().getCurrency());
    }
}
