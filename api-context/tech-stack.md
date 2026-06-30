# Technology Stack

This project is designed for high-performance financial data processing and scalable distributed architecture.

Each technology is introduced based on system evolution phases (V1 → V6).


---

# Backend

## Java 21

Used as core runtime.

Reasons:

- Virtual Threads for high concurrency processing
- Records for immutable DTOs
- improved GC and performance characteristics
- modern JVM optimizations for batch processing workloads


Focus:

- low memory consumption
- high throughput processing


---

## Framework

## Spring Boot 3

Used only in interface and infrastructure layers.

Responsibilities:

- API exposure
- dependency injection
- configuration management


Not used in:

- domain layer
- business logic


---

# Data Layer

## PostgreSQL

Primary relational database.

Used for:

- transactional storage
- batch inserts
- reporting queries


Limitations:

- not designed for streaming processing
- not used for high-frequency per-record writes in isolation


---

## Redis (V3+)

Used as performance optimization layer.

Responsibilities:

- caching
- deduplication support
- reducing database load


Not a source of truth.


---

# Messaging Layer

## Kafka (V2+)

Used for distributed processing.

Responsibilities:

- decoupling ingestion from processing
- enabling horizontal scaling
- buffering high-volume event streams


Enables:

- worker-based architecture
- fault tolerance
- replayability


---

# Infrastructure

## Docker (V1+)

Used for:

- reproducible environment
- isolated execution
- performance benchmarking


Constraints:

- optimized image size
- controlled JVM memory usage
- minimal runtime overhead


---

## Terraform (V4+)

Used for:

- infrastructure as code
- reproducible cloud environments
- AWS provisioning automation


---

## AWS (V4+)

Cloud execution environment.

Used services:

- ECS (container execution)
- S3 (file storage)
- RDS (database)
- CloudWatch (monitoring)
- IAM (security control)


---

# Observability

## Prometheus + Grafana (V5+)

Used for:

- system metrics
- throughput monitoring
- latency tracking
- memory usage tracking


Critical for:

- validating performance goals
- production readiness simulation


---

# Testing

## JUnit 5

Unit testing framework.

Focus:

- domain rules
- use cases


## Mockito

Used for mocking dependencies in application layer tests.


## Testcontainers

Used for:

- integration testing
- real database validation
- Kafka/Redis simulation (V2+)


---

# Architectural Constraint Summary

- Domain layer is framework-free
- Spring Boot only in outer layers
- Kafka only in distributed layer (V2+)
- Redis only in optimization layer (V3+)
- AWS only in deployment layer (V4+)
- Kubernetes only in scale simulation (V6+)


---

# Design Philosophy

This stack is not a static selection of tools.

It is an evolving architecture where each technology is introduced only when the system requires it.