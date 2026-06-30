# Domain Rules

## Core Domain Concept

A Transaction represents an immutable financial event received from external systems.

Once created, a transaction must not be modified, only processed through state transitions.


---

# Transaction Invariants

Every Transaction MUST satisfy the following invariants at creation time:

- transactionId must be unique within the system scope
- amount must be greater than zero
- currency must be a supported ISO currency
- timestamp must be a valid past or present time
- source system must be identified


If any invariant is violated, the transaction is rejected before entering the processing pipeline.


---

# Processing Lifecycle

A transaction follows a strict lifecycle:

PENDING → PROCESSING → COMPLETED

or

PENDING → PROCESSING → FAILED


Rules:

- PENDING: received but not processed
- PROCESSING: currently being handled
- COMPLETED: successfully processed
- FAILED: processing failed with recorded reason


A transaction cannot move backwards in state.


---

# Idempotency Rule

The system must guarantee idempotent processing.

If a transaction with the same transactionId is received multiple times:

- it must not be processed twice
- subsequent occurrences must be ignored or marked as DUPLICATE


This rule is critical for distributed processing (V2+ Kafka phase).


---

# Duplicate Detection Strategy

Duplicate detection must be efficient and scalable.

Rules:

- duplicate detection is based on transactionId
- system must support millions of identifiers
- detection must not require full dataset scan


Preferred strategy:

- in-memory set for V1 (bounded)
- Redis set for V3+
- distributed deduplication for V2+


---

# Error Handling Rules

The processing pipeline must be resilient.

Rules:

- invalid records must NOT stop processing
- each error must be isolated per transaction
- system must continue processing remaining batch
- all errors must be recorded with reason


Error types:

- VALIDATION_ERROR
- DUPLICATE_ERROR
- PROCESSING_ERROR
- SYSTEM_ERROR


---

# Batch Processing Rules

Transactions are processed in batches for performance reasons.

Rules:

- batch size must be configurable
- large files must be streamed
- system must NOT load full dataset into memory
- batch processing must be parallelizable


Batch failure rules:

- failure of one batch does not affect others
- partial success is allowed


---

# Performance Rules (Domain Level)

The domain is designed for high-throughput processing.

Rules:

- no blocking operations inside domain logic
- no I/O inside domain layer
- no external service calls inside domain
- domain logic must be pure and deterministic


This ensures horizontal scalability.


---

# Data Consistency Rules

The system follows eventual consistency.

Rules:

- processing results may be delayed
- intermediate states are allowed
- final state must always be consistent


This enables distributed processing in V2+.


---

# Validation Strategy

Validation is split into two phases:

## 1. Pre-processing validation

- format validation
- schema validation
- required fields check


## 2. Domain validation

- business rules validation
- invariants check
- duplicate detection


---

# Domain Boundaries

The domain layer MUST NOT depend on:

- Spring Boot
- JPA / Hibernate
- Kafka
- Redis
- any infrastructure technology


Domain must remain pure and testable.