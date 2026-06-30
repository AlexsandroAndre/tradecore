# Coding Standards

This document defines mandatory coding rules for the project.
Violations of these rules are considered architectural defects.


---

# General Principles

- Code must be simple and explicit
- No hidden side effects
- Business logic must be deterministic
- Performance is a first-class concern
- The system is designed for processing millions of records efficiently


---

# Architecture Enforcement Rules

## Domain Layer Rules

The domain layer MUST NOT depend on:

- Spring Framework
- JPA / Hibernate
- Kafka
- Redis
- any external library


The domain must:

- contain only business logic
- be framework independent
- be fully testable without infrastructure


---

## Application Layer Rules

Use cases must:

- orchestrate domain logic only
- not contain business rules
- not contain persistence logic
- not perform I/O directly


A Use Case represents a business capability, not a CRUD operation.


---

## Infrastructure Layer Rules

Infrastructure must:

- implement ports defined in application layer
- contain all external integrations
- contain database and messaging logic


Infrastructure must NOT contain business rules.


---

## Interface Layer Rules

Controllers must:

- only handle HTTP concerns
- validate input
- call use cases
- return DTOs (Records)


Controllers must NOT:

- contain business logic
- access database directly


---

# Naming Conventions

## Classes

Use PascalCase:
