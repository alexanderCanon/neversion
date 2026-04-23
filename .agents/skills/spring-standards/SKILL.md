---
name: spring-standards
description: Production-grade Spring Boot engineering standards. Use this skill whenever working on a Spring Boot project, creating new Spring services, implementing features in Spring, reviewing Spring code, setting up Spring project architecture, or discussing Spring Boot best practices. Activates for any Spring Boot, Spring MVC, Spring Security, Spring Data, JPA, Flyway, or hexagonal architecture work. Also use when the user mentions modular monolith, microservices architecture, domain-driven design in Java, or production readiness for Spring applications.
---

# Spring Production Standards

This skill enforces production-grade engineering standards for Spring Boot projects. It covers both **modular monoliths** and **microservices**, with a shared foundation that applies to all Spring Boot work.

## How This Skill Works

1. The **shared foundation** always applies — it defines the non-negotiable baseline for any Spring Boot service
2. Based on the project type, the relevant architecture guide is loaded
3. If the project type is unclear, ask the user once: "Is this a modular monolith or a microservices system?"

## Step 1: Detect Project Type

Before writing or reviewing code, determine the architecture:

**Signals for microservices:**
- Multiple `application.yml` / `application.properties` in separate service directories
- Docker Compose with multiple Spring Boot services
- Service discovery configuration (Eureka, Consul)
- API gateway configuration
- Inter-service communication (Feign, WebClient, messaging)
- Multiple independent `pom.xml` or `build.gradle` per service

**Signals for modular monolith:**
- Single `application.yml` at root
- One deployable artifact
- Package-by-feature structure within a single application
- Single `pom.xml` or `build.gradle`
- Modules communicating via direct in-process calls

**If ambiguous:** Ask the user. Do not guess.

## Step 2: Load the Shared Foundation

Read [references/shared-foundation.md](references/shared-foundation.md) — this applies to every Spring Boot project regardless of architecture style.

Key principles to enforce immediately:
- Hexagonal architecture with clear domain/application/infrastructure separation
- Domain must not depend on Spring or persistence frameworks
- JPA entities are not domain models
- Controllers must not contain business logic
- Ports define contracts; adapters implement them
- Feature-oriented package organization, not framework-driven
- Explicit mappers, no magic mapping
- Flyway for all schema changes
- Testcontainers for integration tests
- Spring Security with deny-by-default

## Step 3: Load the Architecture-Specific Guide

**For modular monolith** — read [references/modular-monolith-guide.md](references/modular-monolith-guide.md)

Key rules:
- One deployable unit with strong internal module boundaries
- Modules divided by business capability
- Module A must not manipulate module B's persistence internals
- Cross-module calls through explicit application/domain contracts
- Local ACID transactions — use the monolith advantage
- Shared DB does not mean shared ownership

**For microservices** — read [references/microservices-guide.md](references/microservices-guide.md)

Key rules:
- Each service owns its data — no shared-write database ownership
- Integration through APIs, events, or designed read models
- Resilience is mandatory: timeouts, retries, circuit breakers, idempotency
- Distributed tracing and correlation IDs required
- Service-to-service authentication explicitly defined
- Contract governance — breaking changes require consumer impact review

## Architecture Baseline (Always Enforced)

### Package Structure Per Feature

```
<feature>/
  domain/
    model/
    service/
    port/out/
    exception/
  application/
    port/in/
    service/
  infrastructure/
    adapters/in/rest/
      dto/
      mapper/
    adapters/out/persistence/
      entity/
      mapper/
      repository/
    config/
```

### Layer Rules

| Rule | Violation Example |
|------|-------------------|
| Domain has no Spring dependencies | `@Autowired` in a domain service |
| Controllers delegate to use cases | Business logic in a `@RestController` |
| JPA entities stay in infrastructure | Returning a JPA entity from a domain service |
| DTOs are not domain models | Passing a request DTO into domain logic |
| Transactions at application layer | `@Transactional` on a domain service |
| Explicit mappers | Using ModelMapper or MapStruct with implicit conventions |

### API Design

- Stable resource naming
- Consistent error response: `timestamp`, `status`, `error`, `message`, `errorCode`, `path`, `traceId`
- OpenAPI with summaries, schemas, validation semantics, error responses, auth requirements
- Never expose internal entities through the API

### Validation Levels

1. **Request** — Bean Validation for nullability, ranges, formats, size
2. **Application** — State-dependent rules, existence checks, authorization
3. **Domain** — Impossible states, invalid transitions, value object construction

### Security Checklist

- Deny-by-default
- Least privilege
- Secure headers
- Protected actuator exposure
- No secrets in source control
- CORS/CSRF policies defined
- Clear public vs internal endpoint boundaries

### Testing Requirements

| Level | What to Validate |
|-------|-----------------|
| Unit | Domain rules, value objects, pure business logic |
| Integration | Persistence, migrations, security config, controller integration |
| Contract/API | Endpoint contracts, error stability, auth behavior |
| Non-functional | Performance, concurrency, idempotency (when justified) |

Use Testcontainers for realistic integration tests against PostgreSQL.

### Observability

- Structured logging
- Correlation/trace IDs
- Health, readiness, liveness endpoints
- Metrics and error-rate visibility
- No sensitive data in logs

## When Writing Code

1. Read the project's existing architecture docs and ADRs first
2. Follow the package structure — do not create global `service` or `repository` packages
3. Create ports before adapters
4. Write the use case in the application layer, not the controller
5. Map between layers explicitly
6. Write tests at the appropriate level
7. Update OpenAPI documentation
8. Verify migrations against a real PostgreSQL instance

## When Reviewing Code

Check for:
- Layer boundary violations (domain depending on infrastructure)
- Missing validation at any level
- Business logic in controllers
- JPA entities leaking to API or domain
- Missing error handling or generic catch-all
- Untested paths
- N+1 query patterns
- Missing security annotations
- Hardcoded configuration or secrets

## Definition of Done

A feature is complete only when:
- Architecture boundaries respected
- Tests exist at correct levels
- API docs updated
- Validation complete at all levels
- Error handling controlled
- Logs/metrics sufficient for operations
- Security considered
- Migration impact reviewed
- Deployment impact known
- Change is reviewable with evidence of verification
