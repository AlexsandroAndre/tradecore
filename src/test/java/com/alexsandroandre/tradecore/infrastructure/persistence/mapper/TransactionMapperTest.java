package com.alexsandroandre.tradecore.infrastructure.persistence.mapper;

import com.alexsandroandre.tradecore.domain.exception.DomainValidationException;
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

@DisplayName("TransactionMapper Tests")
class TransactionMapperTest {

    private TransactionMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapperImpl();
    }

    @Nested
    @DisplayName("toEntity - Domain to Entity Mapping")
    class ToEntityTests {

        @Test
        @DisplayName("should map Transaction domain to TransactionEntity successfully")
        void shouldMapDomainToEntity() {
            OffsetDateTime timestamp = OffsetDateTime.of(
                2024, 1, 15, 10, 30, 0, 0,
                ZoneOffset.UTC
            );

            Transaction domain = new Transaction(
                TRANSACTION_ID_TRX001,
                ACCOUNT_ID_ACC_123,
                AMOUNT_100_50,
                VALID_CURRENCY,
                timestamp,
                SOURCE_EXTERNAL_API,
                Transaction.TransactionStatus.PENDING
            );

            TransactionEntity entity = mapper.toEntity(domain);

            assertNotNull(entity);
            assertEquals(TRANSACTION_ID_TRX001, entity.getTransactionId());
            assertEquals(ACCOUNT_ID_ACC_123, entity.getAccountId());
            assertEquals(AMOUNT_100_50, entity.getAmount());
            assertEquals(VALID_CURRENCY, entity.getCurrency());
            assertEquals(SOURCE_EXTERNAL_API, entity.getSource());
            assertEquals(PROCESSING_STATUS_PENDING, entity.getProcessingStatus());
            assertNotNull(entity.getTimestamp());
            assertNotNull(entity.getCreatedAt());
        }

        @Test
        @DisplayName("should preserve all transaction fields during mapping")
        void shouldPreserveAllFields() {
            OffsetDateTime timestamp = OffsetDateTime.of(
                2024, 3, 20, 15, 45, 30, 500000000,
                ZoneOffset.UTC
            );

            Transaction domain = new Transaction(
                TRANSACTION_ID_TRX_ABC_123,
                ACCOUNT_ID_ACCOUNT_XYZ,
                AMOUNT_1000_99,
                CURRENCY_EUR,
                timestamp,
                SOURCE_INTERNAL_SYSTEM,
                Transaction.TransactionStatus.PROCESSING
            );

            TransactionEntity entity = mapper.toEntity(domain);

            assertEquals(domain.transactionId(), entity.getTransactionId());
            assertEquals(domain.accountId(), entity.getAccountId());
            assertEquals(domain.amount(), entity.getAmount());
            assertEquals(domain.currency(), entity.getCurrency());
            assertEquals(domain.source(), entity.getSource());
            assertEquals(domain.status().name(), entity.getProcessingStatus());
        }

        @Test
        @DisplayName("should handle different transaction statuses")
        void shouldHandleDifferentStatuses() {
            Transaction[] transactions = new Transaction[]{
                createTransaction(TRANSACTION_ID_TRX001, Transaction.TransactionStatus.PENDING),
                createTransaction(TRANSACTION_ID_TRX002, Transaction.TransactionStatus.PROCESSING),
                createTransaction(TRANSACTION_ID_TRX003, Transaction.TransactionStatus.COMPLETED),
                createTransaction(TRANSACTION_ID_TRX004, Transaction.TransactionStatus.FAILED)
            };

            for (Transaction transaction : transactions) {
                TransactionEntity entity = mapper.toEntity(transaction);
                assertEquals(transaction.status().name(), entity.getProcessingStatus());
            }
        }

        @Test
        @DisplayName("should throw exception when domain is null")
        void shouldThrowExceptionWhenDomainIsNull() {
            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> mapper.toEntity(null)
            );

            assertEquals(EXCEPTION_CODE_NULL_MAPPING, exception.getValidationCode());
            assertTrue(exception.getMessage().contains("null"));
        }

        @Test
        @DisplayName("should handle null status gracefully")
        void shouldHandleNullStatus() {
            Transaction domain = new Transaction(
                TRANSACTION_ID_TRX001,
                ACCOUNT_ID_ACC_123,
                AMOUNT_100_50,
                VALID_CURRENCY,
                OffsetDateTime.now(ZoneOffset.UTC),
                SOURCE_GENERIC,
                null
            );

            TransactionEntity entity = mapper.toEntity(domain);
            assertNotNull(entity);
            assertNull(entity.getProcessingStatus());
        }
    }

    @Nested
    @DisplayName("toDomain - Entity to Domain Mapping")
    class ToDomainTests {

        @Test
        @DisplayName("should map TransactionEntity to Transaction domain successfully")
        void shouldMapEntityToDomain() {
            Instant timestamp = Instant.parse("2024-01-15T10:30:00Z");
            Instant createdAt = Instant.now();

            TransactionEntity entity = new TransactionEntity(
                TRANSACTION_ID_TRX001,
                ACCOUNT_ID_ACC_123,
                AMOUNT_100_50,
                VALID_CURRENCY,
                SOURCE_EXTERNAL_API,
                timestamp,
                PROCESSING_STATUS_PENDING,
                createdAt
            );

            Transaction domain = mapper.toDomain(entity);

            assertNotNull(domain);
            assertEquals(TRANSACTION_ID_TRX001, domain.transactionId());
            assertEquals(ACCOUNT_ID_ACC_123, domain.accountId());
            assertEquals(AMOUNT_100_50, domain.amount());
            assertEquals(VALID_CURRENCY, domain.currency());
            assertEquals(SOURCE_EXTERNAL_API, domain.source());
            assertEquals(Transaction.TransactionStatus.PENDING, domain.status());
            assertNotNull(domain.timestamp());
        }

        @Test
        @DisplayName("should preserve all entity fields during mapping")
        void shouldPreserveAllFields() {
            Instant timestamp = Instant.parse("2024-03-20T15:45:30.500Z");

            TransactionEntity entity = new TransactionEntity(
                TRANSACTION_ID_TRX_XYZ_789,
                ACCOUNT_ID_ACCOUNT_ABC,
                AMOUNT_5000_75,
                CURRENCY_GBP,
                SOURCE_SYSTEM_A_MAPPER,
                timestamp,
                PROCESSING_STATUS_COMPLETED,
                Instant.now()
            );

            Transaction domain = mapper.toDomain(entity);

            assertEquals(entity.getTransactionId(), domain.transactionId());
            assertEquals(entity.getAccountId(), domain.accountId());
            assertEquals(entity.getAmount(), domain.amount());
            assertEquals(entity.getCurrency(), domain.currency());
            assertEquals(entity.getSource(), domain.source());
        }

        @Test
        @DisplayName("should handle different transaction statuses from entity")
        void shouldHandleDifferentStatuses() {
            String[] statuses = {PROCESSING_STATUS_PENDING, PROCESSING_STATUS_PROCESSING, PROCESSING_STATUS_COMPLETED, PROCESSING_STATUS_FAILED};

            for (String status : statuses) {
                TransactionEntity entity = createEntity(TRANSACTION_ID_TRX001, status);
                Transaction domain = mapper.toDomain(entity);
                assertEquals(status, domain.status().name());
            }
        }

        @Test
        @DisplayName("should throw exception when entity is null")
        void shouldThrowExceptionWhenEntityIsNull() {
            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> mapper.toDomain(null)
            );

            assertEquals(EXCEPTION_CODE_NULL_MAPPING, exception.getValidationCode());
        }

        @Test
        @DisplayName("should throw exception for invalid status")
        void shouldThrowExceptionForInvalidStatus() {
            TransactionEntity entity = new TransactionEntity(
                TRANSACTION_ID_TRX001,
                ACCOUNT_ID_ACC_123,
                AMOUNT_100_50,
                VALID_CURRENCY,
                SOURCE_GENERIC,
                Instant.now(),
                PROCESSING_STATUS_INVALID,
                Instant.now()
            );

            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> mapper.toDomain(entity)
            );

            assertEquals(EXCEPTION_CODE_INVALID_ENTITY_MAPPING, exception.getValidationCode());
        }
    }

    @Nested
    @DisplayName("toEntityList - Collection Mapping")
    class ToEntityListTests {

        @Test
        @DisplayName("should map list of domain transactions to entities")
        void shouldMapListOfDomains() {
            List<Transaction> domains = List.of(
                createTransaction(TRANSACTION_ID_TRX001, Transaction.TransactionStatus.PENDING),
                createTransaction(TRANSACTION_ID_TRX002, Transaction.TransactionStatus.PROCESSING),
                createTransaction(TRANSACTION_ID_TRX003, Transaction.TransactionStatus.COMPLETED)
            );

            List<TransactionEntity> entities = mapper.toEntityList(domains);

            assertEquals(LIST_SIZE_3, entities.size());
            assertEquals(TRANSACTION_ID_TRX001, entities.get(0).getTransactionId());
            assertEquals(TRANSACTION_ID_TRX002, entities.get(1).getTransactionId());
            assertEquals(TRANSACTION_ID_TRX003, entities.get(2).getTransactionId());
        }

        @Test
        @DisplayName("should preserve order during list mapping")
        void shouldPreserveOrder() {
            List<Transaction> domains = List.of(
                createTransaction(TRANSACTION_ID_TRX_Z, Transaction.TransactionStatus.PENDING),
                createTransaction(TRANSACTION_ID_TRX_A, Transaction.TransactionStatus.PENDING),
                createTransaction(TRANSACTION_ID_TRX_M, Transaction.TransactionStatus.PENDING)
            );

            List<TransactionEntity> entities = mapper.toEntityList(domains);

            assertEquals(TRANSACTION_ID_TRX_Z, entities.get(0).getTransactionId());
            assertEquals(TRANSACTION_ID_TRX_A, entities.get(1).getTransactionId());
            assertEquals(TRANSACTION_ID_TRX_M, entities.get(2).getTransactionId());
        }

        @Test
        @DisplayName("should handle empty list")
        void shouldHandleEmptyList() {
            List<TransactionEntity> entities = mapper.toEntityList(List.of());

            assertNotNull(entities);
            assertTrue(entities.isEmpty());
        }

        @Test
        @DisplayName("should throw exception when list is null")
        void shouldThrowExceptionWhenListIsNull() {
            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> mapper.toEntityList(null)
            );

            assertEquals(EXCEPTION_CODE_NULL_MAPPING, exception.getValidationCode());
        }

        @Test
        @DisplayName("should handle large collections efficiently")
        void shouldHandleLargeCollections() {
            List<Transaction> domains = new java.util.ArrayList<>();
            for (int i = 0; i < LIST_SIZE_10000; i++) {
                domains.add(createTransaction("TRX-" + i, Transaction.TransactionStatus.PENDING));
            }

            long startTime = System.currentTimeMillis();
            List<TransactionEntity> entities = mapper.toEntityList(domains);
            long duration = System.currentTimeMillis() - startTime;

            assertEquals(LIST_SIZE_10000, entities.size());
            assertTrue(duration < PERFORMANCE_TIME_THRESHOLD, "Mapping 10000 transactions took too long: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("toDomainList - Collection Mapping")
    class ToDomainListTests {

        @Test
        @DisplayName("should map list of entities to domain transactions")
        void shouldMapListOfEntities() {
            List<TransactionEntity> entities = List.of(
                createEntity(TRANSACTION_ID_TRX001, PROCESSING_STATUS_PENDING),
                createEntity(TRANSACTION_ID_TRX002, PROCESSING_STATUS_PROCESSING),
                createEntity(TRANSACTION_ID_TRX003, PROCESSING_STATUS_COMPLETED)
            );

            List<Transaction> domains = mapper.toDomainList(entities);

            assertEquals(LIST_SIZE_3, domains.size());
            assertEquals(TRANSACTION_ID_TRX001, domains.get(0).transactionId());
            assertEquals(TRANSACTION_ID_TRX002, domains.get(1).transactionId());
            assertEquals(TRANSACTION_ID_TRX003, domains.get(2).transactionId());
        }

        @Test
        @DisplayName("should preserve order during list mapping")
        void shouldPreserveOrder() {
            List<TransactionEntity> entities = List.of(
                createEntity(TRANSACTION_ID_TRX_Z, PROCESSING_STATUS_PENDING),
                createEntity(TRANSACTION_ID_TRX_A, PROCESSING_STATUS_PENDING),
                createEntity(TRANSACTION_ID_TRX_M, PROCESSING_STATUS_PENDING)
            );

            List<Transaction> domains = mapper.toDomainList(entities);

            assertEquals(TRANSACTION_ID_TRX_Z, domains.get(0).transactionId());
            assertEquals(TRANSACTION_ID_TRX_A, domains.get(1).transactionId());
            assertEquals(TRANSACTION_ID_TRX_M, domains.get(2).transactionId());
        }

        @Test
        @DisplayName("should handle empty list")
        void shouldHandleEmptyList() {
            List<Transaction> domains = mapper.toDomainList(List.of());

            assertNotNull(domains);
            assertTrue(domains.isEmpty());
        }

        @Test
        @DisplayName("should throw exception when list is null")
        void shouldThrowExceptionWhenListIsNull() {
            DomainValidationException exception = assertThrows(
                DomainValidationException.class,
                () -> mapper.toDomainList(null)
            );

            assertEquals(EXCEPTION_CODE_NULL_MAPPING, exception.getValidationCode());
        }

        @Test
        @DisplayName("should handle large collections efficiently")
        void shouldHandleLargeCollections() {
            List<TransactionEntity> entities = new java.util.ArrayList<>();
            for (int i = 0; i < LIST_SIZE_10000; i++) {
                entities.add(createEntity("TRX-" + i, PROCESSING_STATUS_PENDING));
            }

            long startTime = System.currentTimeMillis();
            List<Transaction> domains = mapper.toDomainList(entities);
            long duration = System.currentTimeMillis() - startTime;

            assertEquals(LIST_SIZE_10000, domains.size());
            assertTrue(duration < PERFORMANCE_TIME_THRESHOLD, "Mapping 10000 entities took too long: " + duration + "ms");
        }
    }

    @Nested
    @DisplayName("Bidirectional Mapping Consistency")
    class BidirectionalMappingTests {

        @Test
        @DisplayName("should maintain data consistency in round-trip mapping")
        void shouldMaintainConsistencyInRoundTrip() {
            OffsetDateTime originalTimestamp = OffsetDateTime.of(
                2024, 6, 15, 14, 30, 0, 0,
                ZoneOffset.UTC
            );

            Transaction original = new Transaction(
                TRANSACTION_ID_TRX001,
                ACCOUNT_ID_ACC_123,
                AMOUNT_999_99,
                VALID_CURRENCY,
                originalTimestamp,
                SOURCE_GENERIC,
                Transaction.TransactionStatus.PENDING
            );

            TransactionEntity entity = mapper.toEntity(original);
            Transaction recovered = mapper.toDomain(entity);

            assertEquals(original.transactionId(), recovered.transactionId());
            assertEquals(original.accountId(), recovered.accountId());
            assertEquals(original.amount(), recovered.amount());
            assertEquals(original.currency(), recovered.currency());
            assertEquals(original.source(), recovered.source());
            assertEquals(original.status(), recovered.status());
        }

        @Test
        @DisplayName("should handle null values correctly")
        void shouldHandleNullInputsCorrectly() {
            assertThrows(DomainValidationException.class, () -> mapper.toEntity(null));
            assertThrows(DomainValidationException.class, () -> mapper.toDomain(null));
            assertThrows(DomainValidationException.class, () -> mapper.toEntityList(null));
            assertThrows(DomainValidationException.class, () -> mapper.toDomainList(null));
        }
    }

    private Transaction createTransaction(String transactionId, Transaction.TransactionStatus status) {
        return new Transaction(
            transactionId,
            ACCOUNT_ID_ACC_456,
            AMOUNT_100,
            VALID_CURRENCY,
            OffsetDateTime.now(ZoneOffset.UTC),
            SOURCE_TEST_SOURCE,
            status
        );
    }

    private TransactionEntity createEntity(String transactionId, String status) {
        return new TransactionEntity(
            transactionId,
            ACCOUNT_ID_ACC_456,
            AMOUNT_100,
            VALID_CURRENCY,
            SOURCE_TEST_SOURCE,
            Instant.now(),
            status,
            Instant.now()
        );
    }
}