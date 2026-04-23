# Spring Modular Monolith Production Guide

## 1. Purpose

This guide defines the standard for building a **production-grade modular monolith** on top of the shared Spring foundation.

A monolith is not a temporary shortcut by default.
A well-designed modular monolith is often the correct production architecture when:

- the domain is still evolving
- the team is small or medium-sized
- operational simplicity matters
- end-to-end consistency matters more than independent deployment
- the organization is not yet ready for distributed systems complexity

This guide standardizes how to build a monolith that remains modular, testable, scalable, and capable of evolving without collapsing into a big ball of mud.

---

## 2. Architectural Goal

The goal is **one deployable unit with strong internal boundaries**.

Target characteristics:
- one application runtime
- one deployment artifact
- one operational surface
- explicit domain/module boundaries
- low accidental coupling
- high internal cohesion
- clean path to future extraction only if ever justified

This is **not** a layered monolith where everything can call everything.

---

## 3. Module Boundary Rules

The monolith must be divided by **business capability**, not technical layer alone.

Each module should own:
- its use cases
- its domain model
- its persistence adapter(s)
- its API surface or internal entry points
- its mappers and DTOs where applicable

Rules:
- module A must not directly manipulate module B persistence internals
- cross-module calls should happen through explicit application/domain contracts
- avoid a giant shared domain package
- avoid "common" becoming a hidden dependency landfill
- internal reuse is acceptable only when ownership remains clear

A modular monolith succeeds or fails on boundary discipline.

---

## 4. Recommended Internal Shape

A typical module may look like:

```text
billing/
  domain/
  application/
  infrastructure/

identity/
  domain/
  application/
  infrastructure/

catalog/
  domain/
  application/
  infrastructure/
```

Cross-cutting packages may exist for:
- security
- global exception handling
- shared kernel only when truly justified
- platform configuration
- observability
- bootstrap

Do not centralize all repositories, services, or DTOs into global folders.

---

## 5. Transaction Strategy

A monolith can use local ACID transactions effectively.
This is an advantage and should be used intentionally.

Rules:
- define transaction boundaries in application services
- avoid large transactions spanning too many responsibilities
- keep domain invariants protected inside transactional operations
- prefer consistency over eventual-workaround complexity when the monolith allows it
- be careful with lazy loading outside transactional scope

Use the monolith advantage:
- one process
- one transaction manager
- simpler consistency model

Do not imitate distributed patterns unnecessarily if a local transaction solves the problem cleanly.

---

## 6. Persistence and Data Ownership

Even if the monolith uses one PostgreSQL database, ownership must remain modular.

Recommended practices:
- module tables grouped by naming conventions or schema strategy if helpful
- repositories scoped by module
- no direct table ownership confusion
- no feature reading random tables "because it is easy"

Rules:
- JPA entities stay in infrastructure
- domain objects stay in domain
- use query-specific projections for read-heavy flows
- optimize joins intentionally
- review indexes for critical paths

Shared DB does not mean shared ownership.

---

## 7. API and Internal Interaction

A monolith may expose:
- external REST APIs
- internal module-to-module application contracts
- asynchronous internal events if needed

Guidance:
- use direct in-process calls for synchronous module collaboration
- use domain/application events only when they improve decoupling, not because they feel more "microservices-like"
- do not over-engineer internal communication
- keep internal contracts explicit and testable

If an interaction is simple and strongly consistent, a normal in-process call is usually correct.

---

## 8. Caching, Jobs, and Async Work

Production monoliths often need:
- scheduled jobs
- asynchronous notifications
- report generation
- transactional outbox when integrating externally
- cache for heavy reads
- background processing

Recommended:
- use async processing carefully and explicitly
- require idempotency for retriable jobs
- document retry behavior
- isolate long-running work from request threads
- monitor queue/job failures

A monolith can still need mature background processing.
Do not leave it ad hoc.

---

## 9. Security in a Monolith

In addition to the shared security baseline:

- define clear authentication entry points
- centralize authorization rules where practical
- separate public/admin/internal access models
- protect module-specific operations with explicit permissions
- audit privileged actions
- avoid role checks scattered chaotically across controllers

Because everything lives in one runtime, the risk of hidden privilege leakage is high if authorization is not structured.

---

## 10. Observability for a Monolith

A monolith is simpler to operate than microservices, but production observability is still mandatory.

You should be able to answer:
- which feature is failing
- which operation is slow
- which queries are expensive
- which background job is stuck
- which user flow is degraded

Minimum expectations:
- structured logs
- trace IDs at request level
- per-module metrics where possible
- job execution visibility
- database performance visibility
- actuator exposure with proper protection

---

## 11. Performance and Scalability

A monolith can scale successfully if performance is handled deliberately.

Priorities:
- database query efficiency
- right transaction scope
- JVM tuning when needed
- caching where justified
- avoiding chatty internal abstractions
- efficient serialization
- batch operations when appropriate

Scale strategy may include:
- vertical scaling first
- horizontal scaling if the monolith is stateless enough
- separating heavy jobs from request path
- extracting only proven bottlenecks if necessary

Do not split to microservices just because the monolith became popular.
First measure the real bottleneck.

---

## 12. Testing Strategy for a Modular Monolith

Required coverage includes:

### 12.1 Module-level unit tests
- domain rules
- application services
- value objects
- policies

### 12.2 Integration tests
- persistence
- migrations
- security
- REST endpoints
- transaction behavior

### 12.3 Module-boundary tests
- verify modules interact through intended contracts
- prevent illegal dependency shortcuts
- validate that module-level behavior remains isolated

### 12.4 End-to-end critical flow tests
- only for essential business journeys
- should prove that the main production paths actually work

---

## 13. Deployment and Operations

A modular monolith should be easy to deploy and operate.

Minimum:
- one primary deployable artifact
- environment-specific config externalized
- Flyway executed safely as part of rollout strategy
- readiness and liveness checks
- logging and metrics connected to platform tooling
- rollback plan
- backup/restore procedure for PostgreSQL

Because operational simplicity is one of the main reasons to choose a monolith, deployment must remain boring and reliable.

---

## 14. When Not to Choose a Monolith

Do not force a monolith if the architecture definition process already demonstrates:

- independently evolving domains with separate operational needs
- radically different scaling profiles
- strict team autonomy requirements
- incompatible release cadences
- regulatory or isolation requirements
- high-volume asynchronous integration topology that would be distorted in one runtime

Choose the monolith because it is the right system shape, not because it is familiar.

---

## 15. Monolith Definition of Done

A monolith feature is production-ready only when:

- module ownership is clear
- cross-module interaction is explicit
- transaction behavior is correct
- API contract is documented
- errors are controlled
- security is enforced
- tests exist at module and integration levels
- migrations are reviewed
- logs and metrics support operations
- background behavior is idempotent where needed
- no architectural shortcut was introduced "just for speed"

---

## 16. Final Rule

A modular monolith is a deliberate architecture.
If teams stop respecting module boundaries, the system stops being a modular monolith and becomes an unstructured monolith.
That outcome is not acceptable.
