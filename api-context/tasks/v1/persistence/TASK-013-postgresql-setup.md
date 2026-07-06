## Module

persistence

---

# Objective

Configure PostgreSQL as the primary transactional database for the platform.

The goal is to provide a production-ready local environment capable of supporting millions of financial records while maintaining:

- transactional consistency
- high write throughput
- optimized indexing
- predictable performance
- easy local development

---

# Scope

This task includes:

- PostgreSQL Docker container setup
- database creation
- application configuration
- connection pool configuration
- schema initialization
- performance-oriented defaults

---

# Problem Context

The processing pipeline requires a reliable transactional database capable of handling:

- high-volume inserts
- batch operations
- indexed lookups
- duplicate detection
- future distributed processing workloads

PostgreSQL is chosen because it provides:

- ACID transactions
- excellent write performance
- mature indexing support
- strong ecosystem
- cloud portability

---

# Architecture Placement

The persistence layer belongs to the infrastructure layer.

Structure:

infrastructure

├── persistence

│   ├── configuration

│   ├── entity

│   ├── mapper

│   ├── repository

│   └── adapter

---

# Docker Configuration

Create a PostgreSQL container using Docker Compose.

Configuration:

- PostgreSQL 16
- exposed port 5432
- persistent volume
- health check
- restart unless stopped

Environment variables:

- POSTGRES_DB=financial_processor
- POSTGRES_USER=postgres
- POSTGRES_PASSWORD=postgres

---

# Spring Boot Configuration

Configure application.yml with:

Datasource:

- JDBC URL
- username
- password

Connection Pool:

- HikariCP
- maximumPoolSize = 20
- minimumIdle = 5
- connectionTimeout configured

JPA:

- ddl-auto = validate
- open-in-view = false

---

# Database Schema

Create database:

financial_processor

Create table:

transactions

Columns:

- id
- transaction_id
- account_id
- amount
- currency
- source
- timestamp
- processing_status
- created_at

---

# Index Strategy

Create indexes for:

- transaction_id (unique)
- account_id
- processing_status
- created_at

---

# Duplicate Detection Rule

The database must guarantee that duplicated transaction identifiers cannot be persisted.

Rule:

transaction_id must be unique.

Failure:

UNIQUE_CONSTRAINT_VIOLATION

---

# Performance Requirements

The database configuration must support:

- batch inserts
- indexed lookups
- millions of records
- predictable write performance

Avoid:

- unnecessary indexes
- sequential scans on transaction lookups
- connection leaks

---

# Memory Rules

The persistence layer MUST NOT:

- keep database connections open unnecessarily
- disable connection pooling
- perform one insert per transaction when batching is available

Preferred:

- HikariCP
- batch operations
- prepared statements

---

# Coding Requirements

Follow:

- SOLID principles
- Clean Code
- Repository Pattern
- Mapper Pattern

Preferred:

- Flyway-ready structure
- immutable DTOs
- configuration by profiles

---

# Testing Requirements

## Integration Tests

Must validate:

- PostgreSQL container startup
- successful connection
- schema creation
- insert transaction
- unique constraint
- indexed queries

---

# Acceptance Criteria

This task is complete when:

- PostgreSQL runs using Docker
- Spring Boot connects successfully
- transactions table exists
- indexes are created
- unique constraint works
- integration tests pass

---

# Out of Scope

This task does NOT include:

- Flyway migrations
- replication
- sharding
- cloud deployment
- backup strategy

---

# Output

A fully configured PostgreSQL environment ready for the V1 processing platform.

---

# Notes

This configuration serves as the persistence foundation for future Kafka workers, distributed processing and cloud deployment.
"""