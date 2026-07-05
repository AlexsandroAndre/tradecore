# TASK-014 — Persistence Mapper Layer

## Phase
V1 — Persistence

## Module
persistence

---

# Objective

Implement the persistence mapper layer responsible for converting objects between the Domain Model and the Persistence Model.

The goal is to guarantee complete separation between business logic and persistence while maintaining:

- framework independence
- immutable domain models
- explicit object mapping
- high performance
- easy maintainability

---

# Scope

This task includes:

- creation of persistence mappers
- Domain → Entity mapping
- Entity → Domain mapping
- collection mapping
- mapper unit tests

---

# Problem Context

The project follows Hexagonal Architecture.

The Domain layer represents the business and must never depend on persistence technologies.

The Infrastructure layer is responsible for interacting with PostgreSQL through JPA entities.

Because both layers have different responsibilities, they must use different models.

The mapper layer becomes the bridge between these two worlds.

---

# Architecture Placement

The mapper belongs to the Infrastructure layer.

Structure:

infrastructure

├── persistence

│   ├── entity

│   ├── mapper

│   ├── repository

│   └── configuration

The mapper layer may depend on:

- Domain Model
- Persistence Entity

The mapper layer must NOT depend on:

- Controllers
- API DTOs
- Kafka
- AWS
- External services

---

# Mapping Flow

Processing Engine

↓

Domain Transaction

↓

Persistence Mapper

↓

Transaction Entity

↓

Repository

↓

PostgreSQL

---

# Domain Entity

The mapper receives:

Transaction

Represents the business entity.

The domain object must remain immutable.

---

# Persistence Entity

The mapper converts the domain object into:

TransactionEntity

The persistence entity represents the database model and contains all JPA annotations required by Hibernate.

---

# Mapping Responsibilities

The mapper must support:

- Domain → Entity
- Entity → Domain
- List → List
- Batch → Batch

---

# Mapping Rules

## Domain → Entity

Rules:

- preserve every business value
- perform direct field mapping
- never modify business data

Failure:

INVALID_DOMAIN_MAPPING

---

## Entity → Domain

Rules:

- recreate immutable domain objects
- ignore persistence implementation details
- never expose JPA entities outside Infrastructure

Failure:

INVALID_ENTITY_MAPPING

---

## Collection Mapping

Rules:

- map collections efficiently
- preserve ordering
- avoid unnecessary allocations

Failure:

INVALID_COLLECTION_MAPPING

---

## Null Handling

Rules:

- null input returns null
- never throw unexpected NullPointerException
- perform explicit null validation

Failure:

NULL_MAPPING

---

# Builder Usage

Domain objects should be created using the Builder Pattern.

Example:

Transaction.builder()

.id(...)

.accountId(...)

.amount(...)

.currency(...)

.build()

---

# Mapper Design

Each mapper should expose explicit methods.

Example:

- toEntity()
- toDomain()
- toEntityList()
- toDomainList()

The mapper must be stateless.

---

# Error Handling Strategy

Mapping failures:

- must fail fast
- must provide meaningful exceptions
- must never silently ignore invalid values

---

# Performance Requirements

The mapper layer must support:

- millions of object conversions
- low memory allocation
- deterministic execution time

Avoid:

- reflection
- dynamic field inspection
- unnecessary object creation

---

# Memory Rules

The mapper layer MUST NOT:

- cache entities
- cache domain objects
- keep processing state

Preferred:

- stateless implementation
- immutable objects
- direct field mapping

---

# Coding Requirements

Follow:

- SOLID principles
- Clean Code
- Single Responsibility Principle

Preferred:

- dedicated mapper per aggregate
- explicit conversion methods
- immutable domain objects
- builder pattern
- Java Records where applicable

---

# Testing Requirements

## Unit Tests

Must validate:

- Domain → Entity mapping
- Entity → Domain mapping
- null handling
- collection mapping
- batch mapping
- field consistency

---

# Acceptance Criteria

This task is complete when:

- mapper layer exists
- Domain Model remains independent from JPA
- Entity conversion works correctly
- collection mapping is supported
- batch mapping is supported
- unit tests cover all mapping scenarios

---

# Out of Scope

This task does NOT include:

- repository implementation
- database operations
- transaction management
- Kafka integration
- distributed processing

---

# Output

A stateless persistence mapper layer capable of converting Domain Models and Persistence Entities while preserving complete architectural separation.

---

# Notes

The mapper layer is one of the fundamental components of Hexagonal Architecture.

It guarantees that changes in the persistence technology do not affect the Domain Model, allowing the application to evolve independently from its storage implementation.