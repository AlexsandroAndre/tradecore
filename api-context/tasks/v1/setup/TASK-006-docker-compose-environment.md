# TASK-006 — Docker Compose Environment Setup

## Phase
V1 — Setup

## Module
setup

---

# Objective

Create a local development environment using Docker Compose to enable the application to run in a fully reproducible stack with supporting infrastructure services.

This setup allows:

- local integration testing
- realistic production-like environment
- future compatibility with V2 (Kafka), V3 (Redis), V4 (AWS simulation)
- simplified onboarding for development

---

# Scope

This task includes:

- creation of docker-compose.yml
- orchestration of application container
- PostgreSQL service setup (baseline persistence layer)
- optional Redis setup placeholder (future V3 usage)
- network configuration between services
- environment variable wiring

---

# Technical Requirements

## Containers Included

### Application

- Spring Boot service (built from Dockerfile in TASK-005)

---

### PostgreSQL

Used for:

- transactional persistence (V1 foundation)

Requirements:

- stable version (e.g. 15+)
- persistent volume
- exposed only for local dev

---

### Redis (Optional in V1, prepared for V3)

Used for:

- future caching layer
- deduplication strategies

Must be included as:

- optional service or commented configuration

---

# Docker Compose Structure

## Services

- app
- postgres
- redis (optional / future-ready)

---

## Networking

All services must share a single internal network:

- processor-network

Ensures:

- service isolation
- internal DNS resolution
- no external exposure except required ports

---

# Environment Variables

All configuration must be externalized:

## Application

- SPRING_DATASOURCE_URL
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD

## PostgreSQL

- POSTGRES_DB
- POSTGRES_USER
- POSTGRES_PASSWORD

---

# Volume Management

## PostgreSQL

Must include persistent volume:

- ensures data survives container restart
- simulates production-like persistence behavior

---

# Port Mapping

## Application

- 8080:8080 (configurable)

## PostgreSQL

- 5432:5432 (local access only)

## Redis (if enabled)

- 6379:6379 (local only)

---

# Performance Considerations

Even in local environment:

- avoid unnecessary container overhead
- ensure fast startup
- keep services lightweight
- prevent resource contention between containers

---

# Isolation Rules

- application must not depend on host machine services
- all dependencies must be containerized
- no local database usage allowed

---

# Future Compatibility Requirements

This setup must support:

## V2 — Kafka Integration
- Kafka service can be added without restructuring

## V3 — Redis Optimization
- Redis already available for activation

## V4 — AWS Migration
- environment variables must map directly to cloud services

## V6 — Kubernetes Migration
- docker-compose services must be easily translatable to pods

---

# Acceptance Criteria

This task is complete when:

- docker-compose.yml runs successfully
- application starts via docker compose up
- PostgreSQL is correctly connected
- environment variables are properly injected
- network communication between services works
- Redis is optionally available or prepared
- no manual host dependency is required

---

# Out of Scope

This task does NOT include:

- Kafka setup (V2)
- AWS deployment (V4)
- Kubernetes configuration (V6)
- application business logic
- API implementation

---

# Output

A fully containerized local development environment that replicates production-like infrastructure for the V1 system.

---

# Notes

This Docker Compose setup is the first step toward a distributed system architecture.

It ensures that V1 is already designed with future scalability in mind.