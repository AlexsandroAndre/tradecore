# TASK-005 — Docker Base Setup

## Phase
V1 — Setup

## Module
setup

---

# Objective

Create a minimal, optimized Docker configuration for the Spring Boot application to ensure:

- reproducible execution environment
- low resource consumption
- fast startup time
- container-ready architecture for future scaling (V4+ AWS, V6 Kubernetes)

This task establishes the base container strategy for the entire system.

---

# Scope

This task includes:

- creation of Dockerfile
- JVM optimization inside container
- minimal image strategy
- .dockerignore configuration
- container runtime configuration baseline

---

# Technical Requirements

## Java Runtime

- Java 21

Must support:

- efficient memory usage in containers
- predictable GC behavior
- compatibility with virtual threads (future V2+)

---

# Docker Strategy

## Base Image

Use a minimal and production-oriented base image:

Preferred options:

- eclipse-temurin:21-jdk-alpine (or slim variant)

Rules:

- avoid heavy full JDK images
- prefer slim or alpine-based images

---

## Container Design Principles

The container must:

- be stateless
- have minimal filesystem footprint
- avoid unnecessary tools
- run only the application process

---

# Dockerfile Requirements

## Build Stage

- compile application using Maven
- isolate build dependencies

## Runtime Stage

- copy only compiled JAR
- run application with minimal JVM overhead

---

## JVM Optimization (Mandatory)

Include JVM tuning for containers:

- memory limit awareness
- container-friendly GC behavior
- avoid excessive heap allocation

Example concepts:

- Use -XX:MaxRAMPercentage instead of fixed Xmx
- Enable container awareness flags

---

# .dockerignore

Must exclude:

- target/
- .idea/
- *.log
- local environment files
- git metadata not needed in image

Goal:

- reduce build context size
- improve build speed
- avoid leaking unnecessary files into image

---

# Performance Requirements

Container must:

- start fast (< a few seconds in local environment)
- use minimal memory baseline
- avoid heavy initialization overhead
- support high-throughput processing workloads

---

# Security Considerations

- Do not run container as root (preferred)
- expose only required ports
- avoid embedding secrets in image
- configuration must come from environment variables

---

# Port Configuration

Application must expose:

- 8080 (default, configurable via environment variable)

---

# Future Compatibility Requirements

This Docker setup must support:

## V4 — AWS Deployment
- ECS container deployment
- S3/RDS external integration via env vars

## V6 — Kubernetes
- horizontal scaling
- health checks
- readiness/liveness probes

---

# Acceptance Criteria

This task is complete when:

- Dockerfile builds successfully
- application runs inside container
- container exposes correct port
- image size is minimal and optimized
- no secrets are hardcoded
- .dockerignore is properly configured
- JVM runs in container-aware mode

---

# Out of Scope

This task does NOT include:

- docker-compose setup
- Kubernetes configuration
- AWS deployment
- application logic implementation
- database configuration

---

# Output

A production-ready, lightweight Docker container capable of running the Spring Boot application efficiently in any environment.

---

# Notes

This container is the foundation for all future cloud and distributed deployments.

Poor optimization here will directly impact scalability in V4 and V6.