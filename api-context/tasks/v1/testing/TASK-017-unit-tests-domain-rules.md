# TASK-017 — Unit Tests Domain Rules

## Phase
V1 — Testing

## Module
testing

---

# Objective

Implement comprehensive unit tests for all Domain Rules to guarantee that every business rule behaves correctly under valid and invalid scenarios.

The goal is to ensure long-term reliability of the business layer while maintaining:

- deterministic tests
- complete business rule coverage
- isolated unit tests
- high readability
- easy maintenance

---

# Scope

This task includes:

- unit tests for domain validation rules
- positive scenarios
- negative scenarios
- exception validation
- test constants
- reusable test builders
- coverage verification

---

# Problem Context

The Domain layer contains the core business logic of the platform.

Any regression in business rules may lead to:

- invalid financial transactions
- inconsistent processing
- corrupted persistence
- incorrect reports

Every business rule must be protected by deterministic unit tests.

---

# Architecture Placement

The tests belong to the test source set.

Structure:

src

└── test

&nbsp;&nbsp;&nbsp;&nbsp;└── java

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── ...

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── domain

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── validation

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;├── rules

&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;└── builder

The test layer may depend on:

- Domain
- JUnit 5
- Mockito (when necessary)

The test layer must NOT depend on:

- PostgreSQL
- Kafka
- Spring Context
- Docker
- External services

---

# Testing Flow

Create Test Data

↓

Execute Domain Rule

↓

Assert Result

↓

Validate Business Behavior

---

# Test Strategy

Each business rule must have:

- success scenario
- failure scenario
- edge case validation
- exception validation

Every test must be independent.

---

# Test Constants

Every input value used by tests MUST be declared as constants.

Avoid:

```java
new Transaction(
    UUID.randomUUID(),
    "123",
    BigDecimal.valueOf(100)
);
```

Preferred:

```java
private static final UUID VALID_TRANSACTION_ID = UUID.randomUUID();
private static final String VALID_ACCOUNT_ID = "ACC-001";
private static final BigDecimal VALID_AMOUNT = BigDecimal.valueOf(100.00);
private static final String VALID_CURRENCY = "USD";
private static final String VALID_SOURCE = "IMPORT";
private static final Instant VALID_TIMESTAMP = Instant.now();
```

Rules:

- no magic values
- no duplicated literals
- constants grouped at beginning of test class
- descriptive constant names

---

# Test Builder

Complex objects should be created using reusable builders.

Example:

TransactionTestBuilder

Capabilities:

- buildValidTransaction()
- buildInvalidAmountTransaction()
- buildInvalidCurrencyTransaction()
- buildDuplicatedTransaction()

Builders should minimize duplicated setup across test classes.

---

# Business Rules to Validate

## Transaction Identifier

Must validate:

- valid identifier
- null identifier
- empty identifier
- duplicated identifier

---

## Account Identifier

Must validate:

- valid account
- null account
- empty account

---

## Amount

Must validate:

- positive value
- zero value
- negative value
- null value

---

## Currency

Must validate:

- supported currency
- unsupported currency
- null currency

---

## Timestamp

Must validate:

- valid timestamp
- invalid timestamp
- null timestamp

---

## Source

Must validate:

- valid source
- null source
- empty source

---

# Assertion Rules

Assertions must validate:

- returned object
- exception type
- exception message
- validation code
- business behavior

Avoid generic assertions.

Preferred:

- assertEquals()
- assertThrows()
- assertTrue()
- assertFalse()

---

# Performance Requirements

The unit test suite must:

- execute quickly
- avoid unnecessary mocks
- avoid Spring Boot startup
- execute independently

Target:

- complete execution in a few seconds

---

# Memory Rules

Tests MUST NOT:

- allocate unnecessary objects
- duplicate large datasets
- create unnecessary collections

Preferred:

- reusable constants
- reusable builders
- immutable test data

---

# Coding Requirements

Follow:

- SOLID principles
- Clean Code
- Arrange Act Assert pattern
- descriptive test names

Preferred naming:

- shouldAcceptValidTransaction()
- shouldRejectNegativeAmount()
- shouldRejectUnsupportedCurrency()
- shouldRejectDuplicatedTransaction()

---

# Testing Requirements

## Coverage

Every business rule must have:

- success test
- failure test
- edge case test

Target coverage:

- 100% Domain Rules
- 100% Validation Rules

Focus on business behavior instead of implementation details.

---

# Acceptance Criteria

This task is complete when:

- all Domain Rules have unit tests
- all input parameters use constants
- reusable test builders exist
- no duplicated test setup exists
- test suite is deterministic
- domain rule coverage reaches 100%

---

# Out of Scope

This task does NOT include:

- integration tests
- database tests
- Testcontainers
- API tests
- performance tests

---

# Output

A deterministic and maintainable unit test suite that fully validates the business rules of the Domain layer while following project-wide testing standards.

---

# Notes

Unit tests are considered first-class citizens in this project.

All future features must follow the same testing standards established by this task.

Every new business rule introduced into the Domain layer must include corresponding unit tests before implementation is considered complete.

The use of constants for every input parameter is mandatory to improve readability, eliminate magic values, and simplify future maintenance.