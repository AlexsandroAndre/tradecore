# TASK-018 — Integration Tests Docker Environment

## Phase
V1 — Testing

## Module
testing

---

# Objective

Implement a complete integration testing environment using Docker and Testcontainers to validate the interaction between the application and its infrastructure components.

The goal is to guarantee production-like integration tests while maintaining:

- isolated test execution
- deterministic results
- infrastructure reproducibility
- high reliability
- environment independence

---

# Scope

This task includes:

- Testcontainers configuration
- PostgreSQL container
- Docker integration
- Spring Boot integration tests
- repository integration tests
- persistence validation
- reusable integration test infrastructure

---

# Problem Context

Unit tests validate business rules in isolation.

However, they cannot verify:

- database connectivity
- SQL execution
- transaction management
- JPA mappings
- repository behavior

Integration tests execute against a real PostgreSQL instance running inside Docker, providing confidence that the Infrastructure layer behaves correctly before deployment.

---

# Architecture Placement

The integration tests belong to the test source set.

Structure:

src

└── test

&nbsp;&nbsp;&nbsp;&nbsp;└── java

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── ...

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── integration

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── configuration

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── repository

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── persistence

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── builder

The integration test layer may depend on:

- Spring Boot Test
- Testcontainers
- PostgreSQL
- JUnit 5

The integration test layer must NOT depend on:

- Kafka
- Redis
- AWS
- External APIs

---

# Integration Flow

Start Docker Container

↓

Initialize PostgreSQL

↓

Start Spring Context

↓

Execute Repository

↓

Validate Database State

↓

Destroy Container

---

# Docker Environment

The integration environment must start automatically.

Infrastructure:

- PostgreSQL Container
- Spring Boot Test Context

The container lifecycle must be managed entirely by Testcontainers.

---

# Testcontainers Configuration

The project must use:

- PostgreSQLContainer
- @Testcontainers
- @Container

Container configuration should be centralized to maximize reuse.

---

# Test Constants

Every input value used by integration tests MUST be declared as constants.

Avoid:

```java
repository.findById(UUID.randomUUID());

entity.setCurrency("USD");

entity.setAmount(BigDecimal.valueOf(100));
```

Preferred:

```java
private static final UUID VALID_TRANSACTION_ID = UUID.randomUUID();
private static final String VALID_ACCOUNT_ID = "ACC-001";
private static final BigDecimal VALID_AMOUNT = BigDecimal.valueOf(100.00);
private static final String VALID_CURRENCY = "USD";
private static final String VALID_SOURCE = "IMPORT";
private static final String DATABASE_NAME = "transaction_db";
private static final String DATABASE_USERNAME = "postgres";
private static final String DATABASE_PASSWORD = "postgres";
```

Rules:

- no magic values
- descriptive constant names
- constants declared once
- reusable across integration tests

---

# Integration Test Builder

Complex entities should be created using reusable builders.

Example:

TransactionEntityTestBuilder

Capabilities:

- buildValidEntity()
- buildPersistedEntity()
- buildBatchEntities()
- buildInvalidEntity()

Builders must eliminate duplicated setup logic.

---

# Repository Validation

Repository integration tests must validate:

- save entity
- update entity
- find by identifier
- delete entity
- batch insert
- transaction rollback

---

# Persistence Validation

The persistence layer must validate:

- JPA mappings
- primary keys
- constraints
- indexes
- generated identifiers
- entity relationships (when applicable)

---

# Database Validation

Integration tests must verify:

- inserted records
- updated records
- deleted records
- rollback behavior
- transaction consistency

---

# Assertion Rules

Assertions must validate:

- persisted values
- retrieved entities
- database consistency
- transaction integrity
- exception types

Avoid generic assertions.

Preferred:

- assertEquals()
- assertNotNull()
- assertTrue()
- assertFalse()
- assertThrows()

---

# Performance Requirements

Integration tests must:

- start containers automatically
- reuse containers when possible
- minimize startup time
- execute independently

Target:

- complete execution within a few minutes

---

# Memory Rules

Integration tests MUST NOT:

- duplicate container configuration
- create unnecessary application contexts
- persist unnecessary data

Preferred:

- shared container configuration
- reusable builders
- reusable constants
- isolated datasets

---

# Coding Requirements

Follow:

- SOLID principles
- Clean Code
- Arrange Act Assert pattern

Preferred:

- one responsibility per test
- descriptive test names
- reusable configuration
- centralized constants

---

# Testing Requirements

## Integration Tests

Must validate:

- Docker startup
- PostgreSQL availability
- Spring Boot context initialization
- repository persistence
- transaction rollback
- batch persistence
- entity retrieval
- entity deletion

---

# Acceptance Criteria

This task is complete when:

- Docker integration environment is configured
- Testcontainers starts PostgreSQL automatically
- integration tests execute successfully
- repository behavior is validated
- persistence layer is fully tested
- all input parameters use constants
- reusable builders are implemented
- tests execute independently

---

# Out of Scope

This task does NOT include:

- Kafka integration tests
- Redis integration tests
- end-to-end tests
- performance benchmarking
- cloud infrastructure validation

---

# Output

A fully automated Docker-based integration testing environment capable of validating the complete persistence layer against a real PostgreSQL instance using Testcontainers.

---

# Notes

Integration tests represent the second quality gate of the project.

Unlike unit tests, they validate the interaction between application components and the infrastructure layer under production-like conditions.

All future integration tests must follow the same standards established by this task, including mandatory use of constants for every input parameter, reusable builders, isolated execution, and deterministic behavior.