package com.alexsandroandre.tradecore.infrastructure.persistence.mapper;

import com.alexsandroandre.tradecore.domain.exception.DomainValidationException;
import com.alexsandroandre.tradecore.domain.model.Batch;
import com.alexsandroandre.tradecore.domain.model.Transaction;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.TransactionEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static com.alexsandroandre.tradecore.infrastructure.persistence.constants.IntegrationTestConstants.*;

@DisplayName("BatchMapper Tests")
class BatchMapperTest {

    private BatchMapper batchMapper;
    private TransactionMapper transactionMapper;

    @BeforeEach
    void setUp() {
        transactionMapper = new TransactionMapperImpl();
        batchMapper = new BatchMapperImpl(transactionMapper);
    }

    @Nested
    @DisplayName("toEntityList - Batch to Entity List Mapping")
    class ToEntityListTests {

        @Test
        @DisplayName("should convert Batch domain to TransactionEntity list")
        void shouldConvertBatchToEntityList() {
            List<Transaction> transactions = List.of(
                createTransaction("TRX001", Transaction.TransactionStatus.PENDING),
                createTransaction("TRX002", Transaction.TransactionStatus.PROCESSING),
                createTransaction("TRX003", Transaction.TransactionStatus.COMPLETED)
            );

            Batch batch = new Batch("BATCH001", transactions, 1000);
            List<TransactionEntity> entities = batchMapper.toEntityList(batch);

            assertNotNull(entities);
            assertEquals(3, entities.size());
            assertEquals("TRX001", entities.get(0).getTransactionId());
            assertEquals("TRX002", entities.get(1).getTransactionId());
            assertEquals("TRX003", entities.get(2).getTransactionId());
        }

        @Test
        @DisplayName("should preserve transaction order in batch conversion")
        void shouldPreserveTransactionOrder() {
            List<Transaction> transactions = List.of(
                createTransaction("FIRST", Transaction.TransactionStatus.PENDING),
                createTransaction("SECOND", Transaction.TransactionStatus.PENDING),
                createTransaction("THIRD", Transaction.TransactionStatus.PENDING)
            );

            Batch batch = new Batch("BATCH001", transactions, 1000);
            List<TransactionEntity> entities = batchMapper.toEntityList(batch);

            assertEquals("FIRST", entities.get(0).getTransactionId());
            assertEquals("SECOND", entities.get(1).getTransactionId());
            assertEquals("THIRD", entities.get(2).getTransactionId());
        }

        @Test
        @DisplayName("should handle batch with single transaction")
        void shouldHandleSingleTransaction() {
            List<Transaction> transactions = List.of(
                createTransaction("TRX001", Transaction.TransactionStatus.PENDING)
            );

            Batch batch = new Batch("BATCH001", transactions, 1000);
            List<TransactionEntity> entities = batchMapper.toEntityList(batch);

            assertEquals(1, entities.size());
            assertEquals("TRX001", entities.get(0).getTransactionId());
        }

        @Test
        @DisplayName("should handle large batches efficiently")
        void shouldHandleLargeBatches() {
            List<Transaction> transactions = new java.util.ArrayList<>();
            for (int i = 0; i < 5000; i++) {
                transactions.add(createTransaction("TRX-" + i, Transaction.TransactionStatus.PENDING));
            }

            Batch batch = new Batch("BATCH001", transactions, 10000);

            long startTime = System.currentTimeMillis();
            List<TransactionEntity> entities = batchMapper.toEntityList(batch);
            long duration = System.currentTimeMillis() - startTime;

            assertEquals(5000, entities.size());
            assertTrue(duration < 5000, "Batch conversion took too long: " + duration + "ms");
        }

        @Test
        @DisplayName("should throw exception when batch is null")
        void shouldThrowExceptionWhenBatchIsNull() {
            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> batchMapper.toEntityList(null)
            );

            assertEquals("NULL_MAPPING", exception.getValidationCode());
        }

        @Test
        @DisplayName("should preserve all transaction details in batch conversion")
        void shouldPreserveTransactionDetails() {
            OffsetDateTime timestamp = OffsetDateTime.of(2024, 5, 20, 12, 0, 0, 0, ZoneOffset.UTC);

            Transaction transaction = new Transaction(
                "TRX-DETAIL",
                "ACC-XYZ",
                new BigDecimal("5000.75"),
                "EUR",
                timestamp,
                "BATCH_SOURCE",
                Transaction.TransactionStatus.PROCESSING
            );

            Batch batch = new Batch("BATCH001", List.of(transaction), 1000);
            List<TransactionEntity> entities = batchMapper.toEntityList(batch);

            TransactionEntity entity = entities.get(0);
            assertEquals("TRX-DETAIL", entity.getTransactionId());
            assertEquals("ACC-XYZ", entity.getAccountId());
            assertEquals(new BigDecimal("5000.75"), entity.getAmount());
            assertEquals("EUR", entity.getCurrency());
            assertEquals("BATCH_SOURCE", entity.getSource());
            assertEquals("PROCESSING", entity.getProcessingStatus());
        }
    }

    @Nested
    @DisplayName("toDomain - Entity List to Batch Domain Mapping")
    class ToDomainTests {

        @Test
        @DisplayName("should convert entity list to Batch domain")
        void shouldConvertEntitiesToBatch() {
            List<TransactionEntity> entities = List.of(
                createEntity("TRX001", "PENDING"),
                createEntity("TRX002", "PROCESSING"),
                createEntity("TRX003", "COMPLETED")
            );

            Batch batch = batchMapper.toDomain(entities, "BATCH001", 1000);

            assertNotNull(batch);
            assertEquals("BATCH001", batch.batchId());
            assertEquals(3, batch.size());
            assertEquals(1000, batch.batchSize());
        }

        @Test
        @DisplayName("should recreate transactions in correct order")
        void shouldRecreateTransactionsInOrder() {
            List<TransactionEntity> entities = List.of(
                createEntity("FIRST", "PENDING"),
                createEntity("SECOND", "PENDING"),
                createEntity("THIRD", "PENDING")
            );

            Batch batch = batchMapper.toDomain(entities, "BATCH001", 1000);
            List<Transaction> transactions = batch.transactions();

            assertEquals("FIRST", transactions.get(0).transactionId());
            assertEquals("SECOND", transactions.get(1).transactionId());
            assertEquals("THIRD", transactions.get(2).transactionId());
        }

        @Test
        @DisplayName("should handle batch with single entity")
        void shouldHandleSingleEntity() {
            List<TransactionEntity> entities = List.of(
                createEntity("TRX001", "PENDING")
            );

            Batch batch = batchMapper.toDomain(entities, "BATCH001", 1000);

            assertEquals(1, batch.size());
            assertEquals("TRX001", batch.transactions().get(0).transactionId());
        }

        @Test
        @DisplayName("should throw exception when entities list is null")
        void shouldThrowExceptionWhenEntitiesIsNull() {
            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> batchMapper.toDomain(null, "BATCH001", 1000)
            );

            assertEquals("NULL_MAPPING", exception.getValidationCode());
        }

        @Test
        @DisplayName("should throw exception when batchId is null")
        void shouldThrowExceptionWhenBatchIdIsNull() {
            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> batchMapper.toDomain(List.of(), null, 1000)
            );

            assertEquals("INVALID_BATCH_MAPPING", exception.getValidationCode());
            assertTrue(exception.getMessage().contains("Batch ID"));
        }

        @Test
        @DisplayName("should throw exception when batchId is empty")
        void shouldThrowExceptionWhenBatchIdIsEmpty() {
            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> batchMapper.toDomain(List.of(), "", 1000)
            );

            assertEquals("INVALID_BATCH_MAPPING", exception.getValidationCode());
        }

        @Test
        @DisplayName("should throw exception when batchSize is zero or negative")
        void shouldThrowExceptionWhenBatchSizeIsInvalid() {
            List<TransactionEntity> entities = List.of(createEntity("TRX001", "PENDING"));

            DomainValidationException exception1 = assertThrows(
                DomainValidationException.class,
                () -> batchMapper.toDomain(entities, "BATCH001", 0)
            );

            DomainValidationException exception2 = assertThrows(
                DomainValidationException.class,
                () -> batchMapper.toDomain(entities, "BATCH001", -1)
            );

            assertEquals("INVALID_BATCH_MAPPING", exception1.getValidationCode());
            assertEquals("INVALID_BATCH_MAPPING", exception2.getValidationCode());
        }

        @Test
        @DisplayName("should preserve all transaction details when converting to batch")
        void shouldPreserveTransactionDetails() {
            Instant timestamp = Instant.parse("2024-05-20T12:00:00Z");

            TransactionEntity entity = new TransactionEntity(
                "TRX-DETAIL",
                "ACC-ABC",
                new BigDecimal("2500.50"),
                "GBP",
                "SOURCE_SYSTEM",
                timestamp,
                "COMPLETED",
                Instant.now()
            );

            Batch batch = batchMapper.toDomain(List.of(entity), "BATCH001", 5000);
            Transaction transaction = batch.transactions().get(0);

            assertEquals("TRX-DETAIL", transaction.transactionId());
            assertEquals("ACC-ABC", transaction.accountId());
            assertEquals(new BigDecimal("2500.50"), transaction.amount());
            assertEquals("GBP", transaction.currency());
            assertEquals("SOURCE_SYSTEM", transaction.source());
            assertEquals(Transaction.TransactionStatus.COMPLETED, transaction.status());
        }

        @Test
        @DisplayName("should handle large entity lists efficiently")
        void shouldHandleLargeEntityListsEfficiently() {
            List<TransactionEntity> entities = new java.util.ArrayList<>();
            for (int i = 0; i < 5000; i++) {
                entities.add(createEntity("TRX-" + i, "PENDING"));
            }

            long startTime = System.currentTimeMillis();
            Batch batch = batchMapper.toDomain(entities, "BATCH001", 10000);
            long duration = System.currentTimeMillis() - startTime;

            assertEquals(5000, batch.size());
            assertTrue(duration < 5000, "Batch domain creation took too long: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("Batch Mapping Consistency")
    class BatchMappingConsistencyTests {

        @Test
        @DisplayName("should maintain data consistency in round-trip batch mapping")
        void shouldMaintainConsistencyInRoundTrip() {
            List<Transaction> originalTransactions = List.of(
                createTransaction("TRX001", Transaction.TransactionStatus.PENDING),
                createTransaction("TRX002", Transaction.TransactionStatus.PROCESSING),
                createTransaction("TRX003", Transaction.TransactionStatus.COMPLETED)
            );

            Batch originalBatch = new Batch("BATCH001", originalTransactions, 10000);

            List<TransactionEntity> entities = batchMapper.toEntityList(originalBatch);
            Batch recoveredBatch = batchMapper.toDomain(entities, originalBatch.batchId(), originalBatch.batchSize());

            assertEquals(originalBatch.batchId(), recoveredBatch.batchId());
            assertEquals(originalBatch.size(), recoveredBatch.size());
            assertEquals(originalBatch.batchSize(), recoveredBatch.batchSize());

            for (int i = 0; i < originalBatch.size(); i++) {
                Transaction original = originalBatch.transactions().get(i);
                Transaction recovered = recoveredBatch.transactions().get(i);

                assertEquals(original.transactionId(), recovered.transactionId());
                assertEquals(original.accountId(), recovered.accountId());
                assertEquals(original.amount(), recovered.amount());
                assertEquals(original.currency(), recovered.currency());
                assertEquals(original.status(), recovered.status());
            }
        }

        @Test
        @DisplayName("should handle empty batch correctly")
        void shouldHandleEmptyBatch() {
            Batch emptyBatch = new Batch("BATCH001", List.of(), 1000);
            List<TransactionEntity> entities = batchMapper.toEntityList(emptyBatch);

            assertNotNull(entities);
            assertTrue(entities.isEmpty());
        }

        @Test
        @DisplayName("should handle batch with mixed transaction statuses")
        void shouldHandleMixedStatuses() {
            List<Transaction> transactions = List.of(
                createTransaction("TRX001", Transaction.TransactionStatus.PENDING),
                createTransaction("TRX002", Transaction.TransactionStatus.PROCESSING),
                createTransaction("TRX003", Transaction.TransactionStatus.COMPLETED),
                createTransaction("TRX004", Transaction.TransactionStatus.FAILED)
            );

            Batch batch = new Batch("BATCH001", transactions, 10000);
            List<TransactionEntity> entities = batchMapper.toEntityList(batch);

            assertEquals("PENDING", entities.get(0).getProcessingStatus());
            assertEquals("PROCESSING", entities.get(1).getProcessingStatus());
            assertEquals("COMPLETED", entities.get(2).getProcessingStatus());
            assertEquals("FAILED", entities.get(3).getProcessingStatus());
        }
    }

    private Transaction createTransaction(String transactionId, Transaction.TransactionStatus status) {
        return new Transaction(
            transactionId,
            "ACC-123",
            new BigDecimal("100.00"),
            "USD",
            OffsetDateTime.now(ZoneOffset.UTC),
            "TEST_SOURCE",
            status
        );
    }

    private TransactionEntity createEntity(String transactionId, String status) {
        return new TransactionEntity(
            transactionId,
            "ACC-123",
            new BigDecimal("100.00"),
            "USD",
            "TEST_SOURCE",
            Instant.now(),
            status,
            Instant.now()
        );
    }
}