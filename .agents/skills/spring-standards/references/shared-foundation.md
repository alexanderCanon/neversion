# Spring Shared Foundation Standard

## 1. Purpose

This document defines the **non-negotiable baseline** for Spring Boot services used in this organization.
It applies to both **modular monoliths** and **microservices**.

This standard exists to ensure that engineering decisions are not left to vague prompts, ad-hoc coding, or tool defaults.
Project-specific technical documentation created through the **Architecture Definition Process** remains the primary source of truth for business context, bounded contexts, use cases, and system-level decisions.
This guide standardizes the implementation baseline that must exist **before** writing production code.

---

## 2. Relationship to Project Documentation

Before implementing any feature, engineers and AI coding tools must read the following, in this order:

1. Architecture Definition Process outputs
2. ADRs (Architecture Decision Records), if available
3. This shared foundation guide
4. The monolith or microservices guide that applies to the project
5. Feature- or service-specific technical specifications

If a conflict exists:
- **Project architecture docs** override generic implementation preferences
- **Security, resilience, and data integrity requirements** do not get weakened without an explicit ADR
- AI tools must not invent architecture that contradicts the project documents

---

## 3. Core Engineering Principles

The system must be designed with the following principles:

- explicit architecture over accidental architecture
- clear boundaries over convenience shortcuts
- business-oriented design over framework-oriented design
- reproducibility over local hacks
- observability over assumptions
- verification over confidence
- secure defaults over permissive defaults
- backward-compatible evolution over breaking changes
- code that is reviewable, testable, and operable in production

Recommended complementary principles:
- SOLID
- KISS
- YAGNI
- DRY with moderation
- DDD where complexity justifies it
- hexagonal architecture for boundary clarity
- feature-oriented organization over framework-driven package sprawl

---

## 4. Mandatory Technology Baseline

Unless the project explicitly documents a justified exception, the standard baseline is:

- Spring Boot
- OpenAPI / Swagger
- global exception handling with a stable API error contract
- Spring Security
- Spring Boot Actuator
- Flyway for schema versioning
- PostgreSQL as the primary relational database
- Docker for local and deployable packaging
- Hexagonal Architecture + DDD-inspired boundaries
- Lombok only where it improves clarity without hiding important semantics
- builders only where they improve correctness/readability
- manual mappers instead of implicit runtime mapping
- Testcontainers for integration testing
- automated testing as a first-class engineering concern

---

## 5. Architecture Baseline

## 5.1 Architectural Style

Each service must separate concerns clearly across the following layers:

- **domain**
  - business model
  - value objects
  - domain services
  - domain policies
  - domain exceptions
  - outbound ports

- **application**
  - inbound use cases
  - orchestration of domain logic
  - transaction boundaries
  - command/query handling
  - validation that depends on application state or external collaborators

- **infrastructure**
  - REST controllers
  - persistence adapters
  - security adapters
  - messaging adapters
  - external provider adapters
  - database entities
  - framework configuration

## 5.2 Rules

- Domain must not depend on Spring or persistence frameworks
- Controllers must not contain business logic
- JPA entities are not domain models
- Request/response DTOs are not domain models
- Ports define contracts; adapters implement them
- Transactions must be defined at the application layer unless a documented exception exists
- Mappers must be explicit and testable
- Cross-feature dependencies must be intentional and visible

---

## 6. Package and Module Governance

The codebase must be organized by **feature/domain capability**, not mainly by file type.

Preferred internal structure per feature:

```text
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

Rules:
- Do not create a giant global `service` package
- Do not leak repository interfaces directly into controllers
- Do not bypass use cases for convenience
- Shared utilities must remain small and intentional
- If something becomes a dumping ground, split it or remove it

---

## 7. API Design Standard

Every HTTP API must define:

- stable resource naming
- versioning strategy if public or externally consumed
- pagination/filter/sorting conventions where needed
- idempotency strategy for write operations where applicable
- consistent response envelope strategy if the project uses one
- consistent error response model

Minimum OpenAPI expectations:
- endpoint summaries and descriptions
- request/response schemas
- validation semantics
- error responses
- authentication/authorization requirements
- examples for critical endpoints

Do not expose internal entities directly through the API.

---

## 8. Error Handling Standard

A production service must expose a **stable, structured error contract**.

Recommended fields:
- `timestamp`
- `status`
- `error`
- `message`
- `errorCode`
- `path`
- `traceId`

Rules:
- Separate business errors from technical failures
- Never leak sensitive internals
- Map domain/application errors intentionally
- Handle validation errors consistently
- Unhandled exceptions must still produce a controlled response and be logged with trace context

---

## 9. Validation Standard

Validation exists at multiple levels:

### 9.1 Request validation
Use Bean Validation for:
- nullability
- ranges
- formats
- size constraints
- simple structural rules

### 9.2 Application validation
Use application services for:
- state-dependent rules
- existence checks
- authorization-sensitive rules
- workflow constraints

### 9.3 Domain validation
Use domain invariants for:
- impossible states
- invalid aggregate transitions
- invalid value object construction

Never rely on controller validation alone.

---

## 10. Security Standard

Spring Security is mandatory, but production security requires more than adding the dependency.

Every project must define:
- authentication mechanism
- authorization model
- role/scope model
- credential/token lifecycle
- CORS policy
- CSRF policy where relevant
- password and secret handling
- audit expectations
- public vs internal endpoint boundaries

Minimum expectations:
- deny-by-default mindset
- least privilege
- secure headers
- protected actuator exposure
- no secrets in source control
- clear handling for anonymous, authenticated, and privileged actions

Where applicable, also include:
- rate limiting
- brute-force protection
- IP or network restrictions
- service-to-service auth
- token rotation / refresh strategy
- sensitive data encryption or tokenization

---

## 11. Observability Standard

Actuator is a starting point, not the full observability strategy.

Every production service should support:

- structured logging
- correlation/trace IDs
- health endpoints
- readiness/liveness awareness
- metrics
- latency visibility for critical operations
- error-rate visibility
- audit or business events where required

Recommended stack direction:
- Micrometer
- Prometheus
- Grafana
- OpenTelemetry
- centralized logs (e.g. Loki, ELK, OpenSearch)

Rules:
- logs must be useful, not noisy
- no sensitive data in logs
- business-critical workflows should be observable end-to-end
- do not claim a feature is production ready if it cannot be monitored

---

## 12. Configuration and Secrets Standard

Configuration must be externalized and environment-aware.

Required practices:
- separate config by environment
- immutable build artifact, mutable environment configuration
- secrets outside source control
- documented required environment variables
- explicit defaults only where safe
- startup failure when critical configuration is missing

Preferred secret sources depend on platform:
- Vault
- AWS Secrets Manager
- Azure Key Vault
- Doppler
- Kubernetes Secrets with proper governance

---

## 13. Database and Migration Standard

For PostgreSQL + Flyway projects:

- Flyway is the source of truth for schema evolution
- migrations must be deterministic and reviewed
- schema changes must be backward-compatible when required by deployment strategy
- indexes must be intentional
- constraints must reflect domain invariants where appropriate
- transactional boundaries must be explicit
- long-running or destructive migrations require rollout planning

Rules:
- do not rely on Hibernate schema generation in production
- test migrations against a real PostgreSQL instance
- review performance-critical queries
- watch for N+1 issues
- use projections/read models when needed

---

## 14. Docker Standard

Every service must provide a production-conscious containerization strategy.

Minimum expectations:
- reproducible build
- clear runtime entrypoint
- environment-driven configuration
- non-root where feasible
- small and understandable image
- health integration where possible

Also define:
- local developer flow
- integration test support
- CI/CD build flow
- deploy artifact tagging strategy

---

## 15. Testing Standard

Testing is required at multiple levels.

### 15.1 Unit tests
Validate:
- domain rules
- value objects
- pure business logic
- mapping logic where risk justifies it

### 15.2 Integration tests
Validate:
- persistence behavior
- Flyway migrations
- repository correctness
- security configuration
- controller + application + infrastructure integration
- external adapter behavior when testable

### 15.3 Contract / API tests
Validate:
- endpoint contracts
- response structure
- error contract stability
- auth behavior

### 15.4 Non-functional tests
When justified:
- performance
- concurrency
- idempotency
- resilience behavior
- load testing
- failure scenarios

Rules:
- Testcontainers should be used for realistic integration testing
- tests are part of the design, not post-facto decoration
- production bugs should lead to regression tests

---

## 16. CI/CD and Quality Gates

A production service is incomplete without automated quality controls.

Minimum pipeline expectations:
- build
- unit tests
- integration tests
- artifact packaging
- vulnerability scanning
- container build
- migration awareness if applicable
- deploy automation
- rollback strategy

Recommended additions:
- static analysis
- dependency scanning
- code coverage policy
- SBOM generation
- signed artifacts if required

A change should not be merged solely because "it works locally."

---

## 17. Definition of Done for Production-Oriented Features

A feature is not done when the endpoint responds correctly once.

A feature is done when:
- requirements are implemented
- architecture boundaries were respected
- tests exist at the correct levels
- API docs are updated
- validation is complete
- error handling is controlled
- logs/metrics are sufficient
- security was considered
- migration impact is reviewed
- deployment impact is known
- reviewers can understand the change
- verification was executed with evidence

---

## 18. AI Coding Tool Operating Rules

This section exists for tools such as Codex, Claude Code, Cursor, or similar agents.

Before changing code, the tool must:
1. read the project architecture docs
2. identify whether the project is a monolith or microservices system
3. read this shared guide
4. read the relevant system-specific guide
5. avoid inventing structure that conflicts with the repository standard

The tool must not:
- create architecture by guesswork
- bypass ports/use cases for speed
- introduce hidden magic mappers
- weaken validation or security without instruction
- claim success without running verification commands
- modify persistence or contracts casually

The tool should:
- keep changes small and reviewable
- prefer explicitness over framework magic
- write or update tests
- explain trade-offs when making architecture-impacting changes
- preserve consistency with existing project standards

---

## 19. Recommended Companion Skills

There is currently strong value in complementary skills for process discipline, but they do not replace this document.

Useful categories:
- finding skills
- test-driven development
- code review workflows
- verification before completion
- debugging workflows

These skills complement the standard; they do not define the architecture baseline.

---

## 20. Final Rule

If a project needs a weaker standard, that decision must be explicit and documented.
The default assumption is that production systems require discipline, not convenience.
