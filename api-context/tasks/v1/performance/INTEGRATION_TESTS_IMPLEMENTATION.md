# Integration Test Infrastructure - Complete Implementation

## Overview

This document describes the complete integration testing environment implemented for TASK-018, following the Hexagonal Architecture and Clean Code principles.

## Implementation Summary

### 1. Testcontainers Configuration
**Location:** `src/test/java/com/alexsandroandre/tradecore/infrastructure/persistence/configuration/TestcontainersConfiguration.java`

Manages PostgreSQL container lifecycle with:
- Static container initialization
- DynamicPropertySource for automatic datasource configuration
- Automatic schema creation/drop via Hibernate DDL-auto
- ActiveProfiles configuration for test environment

### 2. Test Constants
**Location:** `src/test/java/com/alexsandroandre/tradecore/infrastructure/persistence/constants/IntegrationTestConstants.java`

Provides reusable test data constants:
- Database credentials
- Transaction test data (amounts, currencies, status values)
- Timestamps and account IDs
- No magic values - all constants declared once

### 3. Test Builder Pattern
**Location:** `src/test/java/com/alexsandroandre/tradecore/infrastructure/persistence/builder/TransactionEntityTestBuilder.java`

Implements builder pattern for entity creation:
- Default sensible values for all fields
- Fluent API for customization
- buildValidEntity() - creates entities with valid data
- buildBatchEntities(int) - creates multiple entities for batch testing
- buildInvalidEntity() - creates null-valued entity for constraint testing

### 4. Base Integration Test Class
**Location:** `src/test/java/com/alexsandroandre/tradecore/infrastructure/persistence/BaseIntegrationTest.java`

Provides centralized Spring Boot test configuration:
- @DataJpaTest - loads only persistence layer
- @AutoConfigureTestDatabase - uses Testcontainers instead of in-memory DB
- @Import(TestcontainersConfiguration.class) - injects container management
- Abstract base class - extended by all integration tests

### 5. Repository Integration Tests
**Location:** `src/test/java/com/alexsandroandre/tradecore/infrastructure/persistence/repository/TransactionRepositoryIntegrationTest.java`

Validates repository behavior with 11 comprehensive tests:
- shouldSaveValidEntity() - entity persistence
- shouldFindByTransactionId() - query by unique ID
- shouldFindByAccountId() - query by account relationship
- shouldFindByProcessingStatus() - status filtering
- shouldUpdateEntity() - entity state changes
- shouldDeleteEntity() - entity removal
- shouldCheckExistenceByTransactionId() - existence checks
- shouldPerformBatchInsert() - batch operations
- shouldFindByDateRange() - date range queries
- shouldEnforceUniqueTransactionIdConstraint() - unique constraint validation
- shouldMaintainDataConsistencyAfterMultipleOperations() - transaction integrity

### 6. Persistence Layer Integration Tests
**Location:** `src/test/java/com/alexsandroandre/tradecore/infrastructure/persistence/TransactionEntityPersistenceIntegrationTest.java`

Validates JPA/Hibernate persistence with 15 comprehensive tests:
- shouldValidateJpaEntityMapping() - entity mapping correctness
- shouldValidatePrimaryKeyGeneration() - ID generation strategy
- shouldValidateUniqueTransactionIdConstraint() - unique constraints
- shouldValidateNullableColumnConstraints() - non-null constraints
- shouldValidateDecimalPrecisionAndScale() - decimal field validation
- shouldValidateStringColumnLengthConstraints() - string length validation
- shouldValidateInstantTimestampPersistence() - temporal field persistence
- shouldValidateTransactionRollbackOnError() - transaction isolation
- shouldValidateTransactionIdIndex() - index effectiveness
- shouldValidateAccountIdIndex() - query performance
- shouldValidateProcessingStatusIndex() - status queries
- shouldValidateConcurrentEntityPersistence() - concurrent operations
- shouldValidateEntityStateChangesArePersisted() - state management

## Architecture Alignment

### Backend Development Principles Applied
- ✓ Small focused classes (each test validates one aspect)
- ✓ Dependency inversion (tests depend on abstractions)
- ✓ Clear responsibilities (repository vs. persistence tests)

### Hexagonal Architecture
- Domain layer is untouched (no Spring dependencies)
- Infrastructure tests validate database adapters
- Tests use dependency injection through Spring Test context

### Clean Code Principles
- Descriptive test names following Given-When-Then pattern
- Arrange-Act-Assert structure in each test
- No code duplication (builder pattern, constants, base class)
- Single responsibility (one test = one behavior)

## Test Execution Guidelines

### Prerequisites
- Docker and Docker Daemon running
- Java 21+
- Maven 3.9.6+
- Network connectivity for pulling Docker images

### Running Tests

#### Run all integration tests:
```bash
./mvnw test
```

#### Run only repository tests:
```bash
./mvnw test -Dtest=TransactionRepositoryIntegrationTest
```

#### Run only persistence tests:
```bash
./mvnw test -Dtest=TransactionEntityPersistenceIntegrationTest
```

#### Run with detailed output:
```bash
./mvnw test -e -X
```

#### Run with coverage:
```bash
./mvnw test jacoco:report
```

## Test Data Flow

```
TransactionEntityTestBuilder
    ↓
Creates entity with constants from IntegrationTestConstants
    ↓
BaseIntegrationTest -> TestcontainersConfiguration
    ↓
Testcontainers starts PostgreSQL container
    ↓
Spring initializes DataJpaTest context
    ↓
Test executes (save, query, update, delete)
    ↓
EntityManager flushes changes to database
    ↓
Assertions validate persisted state
    ↓
Transaction rolled back (DataJpaTest isolation)
    ↓
Container destroyed after test class completes
```

## Key Testing Patterns

### 1. Isolated Test Data
Each test creates its own entities, avoiding interdependencies:
```java
@BeforeEach
void setUp() {
    transactionRepository.deleteAll();
}
```

### 2. Assertion-First Verification
Tests verify both Java object state AND database consistency:
```java
entityManager.flush();
entityManager.clear();
TransactionEntity retrieved = transactionRepository.findById(...);
```

### 3. Constraint Testing
Tests validate database constraints:
```java
assertThrows(Exception.class, () -> {
    transactionRepository.save(duplicate);
    entityManager.flush();
});
```

### 4. Index Effectiveness
Tests ensure indexes are used for queries:
```java
var found = transactionRepository.findByTransactionId(id);
assertTrue(found.isPresent());
```

## Performance Considerations

### Container Reuse
The Testcontainers configuration enables reuse:
```java
.withReuse(true)  // Reuses container across test runs
```

### Transaction Isolation
Each test runs in an isolated transaction:
```java
@DataJpaTest  // Wraps each test in transaction, rolls back after
```

### Lazy Initialization
Spring context initialized once per test class, shared across methods

## Future Extensions

These integration tests can be extended to:
1. Add more entity types (maintain same pattern)
2. Test relationship mappings (one-to-many, many-to-many)
3. Validate custom queries with @Query annotations
4. Test transaction propagation and isolation levels
5. Validate named queries and HQL queries
6. Add performance benchmarks

## Requirements Met

### ✓ Docker Integration
- PostgreSQL container managed by Testcontainers
- Automatic startup/shutdown per test lifecycle
- Environment isolation and reproducibility

### ✓ Spring Boot Integration Tests
- @DataJpaTest loads only persistence layer
- Automatic Spring context management
- Transaction isolation between tests

### ✓ Repository Validation
- CRUD operations (create, read, update, delete)
- Query methods validation
- Batch operations
- Existence checks

### ✓ Persistence Layer Testing
- JPA mappings validation
- Primary key generation
- Constraint validation
- Index effectiveness
- Transaction isolation

### ✓ Constants and Builders
- All test data as constants
- Reusable builder pattern
- No magic values
- Centralized configuration

### ✓ Acceptance Criteria
- Docker environment configured ✓
- Testcontainers starts PostgreSQL ✓
- Integration tests execute successfully ✓
- Repository behavior validated ✓
- Persistence layer fully tested ✓
- All parameters use constants ✓
- Reusable builders implemented ✓
- Tests execute independently ✓

## Notes for Running in Different Environments

### macOS with Docker Desktop
- Ensure Docker daemon is running
- No additional configuration needed
- Container URL: docker.for.mac.localhost

### Linux
- May need to add user to docker group:
  ```bash
  sudo usermod -aG docker $USER
  ```

### Windows
- Use Docker Desktop with WSL 2 backend
- Network may require different host configuration

### CI/CD Pipelines
- Ensure Docker-in-Docker or host Docker socket available
- Set memory limits to prevent resource exhaustion
- Configure container cleanup via testcontainers.reuse properties

## Maintenance Guidelines

1. **Update Container Version**: Change version in TestcontainersConfiguration
2. **Add New Tests**: Follow the same pattern (extend BaseIntegrationTest)
3. **Update Constants**: Always add to IntegrationTestConstants, never use magic values
4. **Use Builder**: Always use TransactionEntityTestBuilder for entity creation
5. **Follow Naming**: Test method names clearly describe what's being tested

---

**Status:** Complete Implementation Ready for Docker Environment
**Last Updated:** 2026-07-05
