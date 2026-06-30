# Architecture

## Architectural Style

This project follows:

* Hexagonal Architecture (Ports and Adapters)
* Clean Architecture principles
* Separation of concerns

The goal is to create a maintainable architecture capable of evolving from a local high-performance processor into a distributed cloud platform.

---

# Dependency Rule

Dependencies must always point inward.

Allowed dependency flow:

```
Interfaces → Application → Domain

Infrastructure → Application → Domain
```

The domain layer must not depend on:

* Spring Framework
* Hibernate
* Database
* Messaging technologies
* External services

The domain must remain framework independent.

---

# Project Structure

```
src/main/java

com.company.processor

├── domain
│   ├── model
│   ├── rules
│   ├── validation
│   └── exception
│
├── application
│   ├── usecase
│   ├── service
│   ├── port
│   └── dto
│
├── infrastructure
│   ├── persistence
│   │   ├── entity
│   │   ├── repository
│   │   └── mapper
│   │
│   ├── messaging
│   └── configuration
│
└── interfaces
    ├── api
    │   ├── controller
    │   ├── request
    │   └── response
    │
    └── batch
```

---

# Layer Responsibilities

## Domain Layer

Responsible for:

* business rules
* domain models
* validations
* domain exceptions

The domain layer contains the core business logic.

It must not know about:

* HTTP
* database
* persistence
* frameworks

---

## Application Layer

Responsible for:

* orchestrating business flows
* executing use cases
* coordinating domain behavior
* defining application contracts

Contains:

* use cases
* application services
* ports
* application DTOs

---

## Infrastructure Layer

Responsible for technical implementations.

Contains:

* database access
* messaging
* external integrations
* framework configuration

Examples:

* PostgreSQL implementation
* Kafka integration
* Redis integration

Infrastructure implements interfaces defined by the application layer.

---

## Interfaces Layer

Responsible for communication with external systems.

Contains:

### API

Responsible for:

* receiving HTTP requests
* validating input
* returning responses

### Batch

Responsible for:

* file ingestion
* batch execution
* processing triggers

---

# Use Case Design

Use cases represent system capabilities.

The project does not create one use case for every method or CRUD operation.

Avoid:

```
CreateTransactionUseCase
UpdateTransactionUseCase
DeleteTransactionUseCase
```

Prefer:

```
ProcessTransactionsUseCase
GenerateProcessingReportUseCase
ManageProcessingJobUseCase
```

A use case represents a business capability.

Example:

```java
public interface ProcessTransactionsUseCase {

    ProcessingResult process(InputFile input);

    ProcessingStatus status(UUID jobId);

}
```

---

# Domain Entity vs Persistence Entity

Domain objects and database objects must be separated.

## Domain Entity

Location:

```
domain.model
```

Responsible for:

* business behavior
* validations
* business rules

Example:

```
Transaction
```

---

## Persistence Entity

Location:

```
infrastructure.persistence.entity
```

Responsible for:

* Hibernate mapping
* database representation
* persistence configuration

Example:

```
TransactionEntity
```

The persistence entity must never contain business rules.

---

# Mapping Strategy

All conversions must be explicit.

The application must not expose:

* Hibernate entities
* Domain entities

directly through APIs.

Conversion flow:

```
Persistence Entity

        ↓

Domain Entity

        ↓

Response DTO
```

Mappers are responsible for conversion.

Example:

```
TransactionMapper

- toDomain()

- toEntity()

- toResponse()

- toDto()
```

---

# DTO Strategy

DTOs should use Java Records whenever possible.

Example:

```java
public record ProcessingResponse(
        UUID jobId,
        Integer processed,
        Integer rejected
) {}
```

Benefits:

* immutable objects
* less boilerplate
* clear contracts

DTOs must not contain business logic.

---

# Builder Strategy

Builder should be used for:

* complex domain objects
* entities with many attributes

Example:

```java
Transaction.builder()
        .id(uuid)
        .amount(value)
        .status(status)
        .build();
```

Avoid builders for simple DTOs.

Prefer Records for simple data transfer objects.

---

# Ports and Adapters

The application layer defines contracts.

Example:

```
application.port

TransactionRepositoryPort

FileStoragePort

MessagePublisherPort
```

Infrastructure provides implementations.

Example:

```
infrastructure.persistence

PostgresTransactionAdapter
```

---

# Processing Flow

## V1

```
Input File

↓

Batch Interface

↓

Streaming Processor

↓

Batch Processor

↓

Validation Engine

↓

Domain Rules

↓

Persistence Port

↓

Database Adapter
```

---

# Performance Principles

## Low Memory Usage

Avoid:

* loading millions of records into memory
* unnecessary object creation

Prefer:

* streaming
* batch processing
* controlled buffers

---

## Controlled Concurrency

Use:

* Java 21 features
* Virtual Threads when applicable
* controlled execution

Avoid:

* uncontrolled thread creation

---

## Database Efficiency

Prefer:

* batch operations
* optimized queries
* controlled transactions

Avoid:

* one database operation per record

---

# Evolution Architecture

## V1

High Performance Core Processor

* local processing
* Docker environment
* optimized resource consumption

## V2

Distributed Processing Engine

* Kafka
* workers
* asynchronous processing

## V3

Data Optimization Layer

* Redis
* caching
* performance tuning

## V4

Cloud Native Deployment

* AWS
* ECS
* S3
* RDS
* Terraform

## V5

Enterprise Observability and Security

* monitoring
* tracing
* authentication
* security scanning

## V6

Enterprise Scale Architecture

* Kubernetes
* autoscaling
* high availability
* multi-region processing

---

# Architecture Goal

Build a platform that demonstrates:

* enterprise backend architecture
* high-performance data processing
* distributed systems design
* cloud readiness
* scalability
* maintainable software engineering practices

```
```
