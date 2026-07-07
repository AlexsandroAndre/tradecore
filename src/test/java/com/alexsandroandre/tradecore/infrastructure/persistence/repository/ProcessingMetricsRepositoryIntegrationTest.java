package com.alexsandroandre.tradecore.infrastructure.persistence.repository;

import com.alexsandroandre.tradecore.infrastructure.persistence.BaseIntegrationTest;
import com.alexsandroandre.tradecore.infrastructure.persistence.builder.ProcessingMetricsEntityTestBuilder;
import com.alexsandroandre.tradecore.infrastructure.persistence.entity.ProcessingMetricsEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ProcessingMetricsRepository Integration Tests")
class ProcessingMetricsRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ProcessingMetricsRepository repository;

    private UUID validId;
    private ProcessingMetricsEntity validEntity;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
        validId = UUID.randomUUID();
        validEntity = ProcessingMetricsEntityTestBuilder.builder()
            .id(validId)
            .buildValidEntity();
    }

    @Nested
    @DisplayName("Save Entity Tests")
    class SaveEntityTests {

        @Test
        @DisplayName("should save valid entity")
        void shouldSaveValidEntity() {
            ProcessingMetricsEntity saved = repository.save(validEntity);

            assertNotNull(saved.getId());
            assertEquals(validId, saved.getId());
            assertEquals(1000L, saved.getTotalRecordsProcessed());
        }

        @Test
        @DisplayName("should persist entity to database")
        void shouldPersistEntityToDatabase() {
            repository.save(validEntity);
            Optional<ProcessingMetricsEntity> retrieved = repository.findById(validId);

            assertTrue(retrieved.isPresent());
            assertEquals(validEntity.getTotalRecordsProcessed(), retrieved.get().getTotalRecordsProcessed());
        }
    }

    @Nested
    @DisplayName("Find By ID Tests")
    class FindByIdTests {

        @Test
        @DisplayName("should find entity by ID")
        void shouldFindEntityById() {
            repository.save(validEntity);
            Optional<ProcessingMetricsEntity> found = repository.findById(validId);

            assertTrue(found.isPresent());
            assertEquals(validId, found.get().getId());
        }

        @Test
        @DisplayName("should return empty optional when ID not found")
        void shouldReturnEmptyOptionalWhenIdNotFound() {
            Optional<ProcessingMetricsEntity> found = repository.findById(UUID.randomUUID());

            assertFalse(found.isPresent());
        }
    }

    @Nested
    @DisplayName("Delete Entity Tests")
    class DeleteEntityTests {

        @Test
        @DisplayName("should delete entity by ID")
        void shouldDeleteEntityById() {
            repository.save(validEntity);
            repository.deleteById(validId);

            Optional<ProcessingMetricsEntity> found = repository.findById(validId);
            assertFalse(found.isPresent());
        }

        @Test
        @DisplayName("should delete all entities")
        void shouldDeleteAllEntities() {
            repository.save(validEntity);
            repository.save(ProcessingMetricsEntityTestBuilder.builder()
                .id(UUID.randomUUID())
                .buildValidEntity());

            repository.deleteAll();

            assertTrue(repository.findAll().isEmpty());
        }
    }

    @Nested
    @DisplayName("Date Range Query Tests")
    class DateRangeQueryTests {

        @Test
        @DisplayName("should find entities within date range")
        void shouldFindEntitiesWithinDateRange() {
            LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 1, 9, 0, 0);
            LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 1, 11, 0, 0);

            repository.save(validEntity);

            List<ProcessingMetricsEntity> found = repository.findByDateRange(rangeStart, rangeEnd);

            assertEquals(1, found.size());
            assertEquals(validId, found.get(0).getId());
        }

        @Test
        @DisplayName("should return empty list when no entities in range")
        void shouldReturnEmptyListWhenNoEntitiesInRange() {
            LocalDateTime rangeStart = LocalDateTime.of(2025, 2, 1, 0, 0, 0);
            LocalDateTime rangeEnd = LocalDateTime.of(2025, 2, 2, 0, 0, 0);

            repository.save(validEntity);

            List<ProcessingMetricsEntity> found = repository.findByDateRange(rangeStart, rangeEnd);

            assertTrue(found.isEmpty());
        }

        @Test
        @DisplayName("should return sorted results from date range query")
        void shouldReturnSortedResultsFromDateRangeQuery() {
            LocalDateTime time1 = LocalDateTime.of(2025, 1, 1, 10, 0, 0);
            LocalDateTime time2 = LocalDateTime.of(2025, 1, 1, 11, 0, 0);
            LocalDateTime time3 = LocalDateTime.of(2025, 1, 1, 12, 0, 0);

            ProcessingMetricsEntity entity1 = ProcessingMetricsEntityTestBuilder.builder()
                .id(UUID.randomUUID())
                .startTime(time1)
                .endTime(time1.plusMinutes(5))
                .buildValidEntity();

            ProcessingMetricsEntity entity2 = ProcessingMetricsEntityTestBuilder.builder()
                .id(UUID.randomUUID())
                .startTime(time2)
                .endTime(time2.plusMinutes(5))
                .buildValidEntity();

            ProcessingMetricsEntity entity3 = ProcessingMetricsEntityTestBuilder.builder()
                .id(UUID.randomUUID())
                .startTime(time3)
                .endTime(time3.plusMinutes(5))
                .buildValidEntity();

            repository.save(entity1);
            repository.save(entity2);
            repository.save(entity3);

            LocalDateTime rangeStart = LocalDateTime.of(2025, 1, 1, 9, 0, 0);
            LocalDateTime rangeEnd = LocalDateTime.of(2025, 1, 1, 13, 0, 0);

            List<ProcessingMetricsEntity> found = repository.findByDateRange(rangeStart, rangeEnd);

            assertEquals(3, found.size());
            assertEquals(time3, found.get(0).getStartTime());
            assertEquals(time2, found.get(1).getStartTime());
            assertEquals(time1, found.get(2).getStartTime());
        }
    }

    @Nested
    @DisplayName("Find All Tests")
    class FindAllTests {

        @Test
        @DisplayName("should find all saved entities")
        void shouldFindAllSavedEntities() {
            ProcessingMetricsEntity entity1 = validEntity;
            ProcessingMetricsEntity entity2 = ProcessingMetricsEntityTestBuilder.builder()
                .id(UUID.randomUUID())
                .buildValidEntity();

            repository.save(entity1);
            repository.save(entity2);

            List<ProcessingMetricsEntity> found = repository.findAll();

            assertEquals(2, found.size());
        }

        @Test
        @DisplayName("should return empty list when no entities exist")
        void shouldReturnEmptyListWhenNoEntitiesExist() {
            List<ProcessingMetricsEntity> found = repository.findAll();

            assertTrue(found.isEmpty());
        }
    }

    @Nested
    @DisplayName("Data Persistence Tests")
    class DataPersistenceTests {

        @Test
        @DisplayName("should persist all metrics fields correctly")
        void shouldPersistAllMetricsFieldsCorrectly() {
            repository.save(validEntity);
            ProcessingMetricsEntity retrieved = repository.findById(validId).get();

            assertEquals(validEntity.getTotalRecordsProcessed(), retrieved.getTotalRecordsProcessed());
            assertEquals(validEntity.getSuccessfulRecords(), retrieved.getSuccessfulRecords());
            assertEquals(validEntity.getFailedRecords(), retrieved.getFailedRecords());
            assertEquals(validEntity.getDuplicateRecords(), retrieved.getDuplicateRecords());
            assertEquals(validEntity.getThroughput(), retrieved.getThroughput());
            assertEquals(validEntity.getAverageLatencyMillis(), retrieved.getAverageLatencyMillis());
        }

        @Test
        @DisplayName("should update entity")
        void shouldUpdateEntity() {
            repository.save(validEntity);

            ProcessingMetricsEntity toUpdate = repository.findById(validId).get();
            toUpdate.setSuccessfulRecords(999L);
            repository.save(toUpdate);

            ProcessingMetricsEntity updated = repository.findById(validId).get();
            assertEquals(999L, updated.getSuccessfulRecords());
        }
    }
}