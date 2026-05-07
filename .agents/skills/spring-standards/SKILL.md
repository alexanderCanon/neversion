---
name: spring-standards
description: Canonical Spring skill for Neversion. Use for Spring Boot 3 / Java 17 work in apps/api, including hexagonal architecture, DDD, REST APIs, security, persistence, Flyway, OpenAPI, and production standards.
---

# Spring Standards for Neversion

Use this skill for all Spring work in this repo. It is the only Spring implementation skill agents should load here.

## Scope

- Target application: `apps/api`
- Stack: Spring Boot 3, Java 17, PostgreSQL, Flyway, OpenAPI, Testcontainers
- Architecture: hexagonal / DDD-inspired modular monolith
- Do not treat the API as microservices unless the repo docs say so

## Defaults

- Read repo architecture docs before coding.
- Keep domain free of Spring and persistence dependencies.
- Keep controllers thin.
- Use explicit ports and adapters.
- Use manual mappers when crossing layers.
- Put transactions in application services.
- Keep code and comments in English.
- Keep user-facing messages in Spanish only when the API is returning visible text.

## Domain and architecture

- Prefer feature-oriented package structure over framework-driven packages.
- Keep JPA entities in infrastructure.
- Keep request and response DTOs out of domain logic.
- Use value objects and domain services where they improve clarity.
- Do not invent architecture that conflicts with the repo docs.

## API standards

- Define stable resources and explicit contracts.
- Document validation and error responses.
- Use versioned endpoints consistently.
- Avoid exposing internal entities through the API.

## Security and persistence

- Deny by default.
- Make authorization explicit.
- Use Flyway for schema changes.
- Use Testcontainers for persistence integration tests.
- Treat PostgreSQL as the real target database.

## Testing

- Write unit tests for domain and application logic.
- Write integration tests for repositories, security, and controller contracts when relevant.
- Prefer evidence-driven testing over coverage targets alone.

## Testing companion

- Use `spring-monolith-testing-skill` only when the task is specifically about test strategy, test planning, or extracting testing context.
- Do not load a second general Spring implementation skill.

