# Spring Microservices Production Guide

## 1. Purpose

This guide defines the standard for building a **production-grade microservices system** on top of the shared Spring foundation.

Microservices are justified only when the architecture definition process shows that the system truly benefits from:

- independently deployable services
- bounded contexts with strong ownership
- different scaling needs
- organizational/team autonomy
- fault isolation requirements
- asynchronous or integration-heavy topology
- controlled decentralization with operational maturity

This guide exists to prevent "distributed monolith" outcomes.

---

## 2. Architectural Goal

The goal is **independent services with explicit contracts, owned data, resilient communication, and operable production behavior**.

Microservices are not "many Spring Boot apps."
They are a socio-technical system requiring discipline in:
- service boundaries
- contracts
- data ownership
- deployment
- observability
- resilience
- security
- platform operations

---

## 3. Service Boundary Rules

Every service must represent a meaningful business capability or bounded context.

A service should own:
- its use cases
- its domain model
- its persistence model
- its database or clearly owned schema/storage boundary
- its public/internal contracts
- its deployment lifecycle

Do not split services by technical layer.
Do not create services just because a module feels large.
Do not split only to appear "modern."

Red flags:
- services that must always be deployed together
- services that share most database tables
- services with constant chatty synchronous calls
- services that know too much about each other's internals
- teams that cannot explain the ownership model clearly

---

## 4. Data Ownership Standard

Each microservice must own its data.

Rules:
- no shared-write database ownership across services
- direct cross-service database reads are strongly discouraged
- integration must happen through APIs, events, or explicitly designed read models
- schema evolution must be owned by the service that owns the data
- eventual consistency must be designed, not ignored

Patterns to use where justified:
- outbox pattern
- CDC-based integration
- domain events
- saga / process manager
- dedicated read models
- idempotent consumers

A shared database with unrestricted access is usually evidence that the system is not actually microservices.

---

## 5. Communication Standard

### 5.1 Synchronous communication
Use synchronous HTTP/gRPC only when:
- immediate response is required
- latency budget is acceptable
- failure semantics are understood
- dependency chain is not excessive

Rules:
- define timeouts explicitly
- apply retries carefully
- use circuit breakers where justified
- avoid deep synchronous call chains
- avoid orchestration through fragile request waterfalls

### 5.2 Asynchronous communication
Use messaging/events when:
- eventual consistency is acceptable
- decoupling is valuable
- retries are required
- workloads are bursty
- integrations are naturally event-driven

Rules:
- messages must be versioned where necessary
- consumers must be idempotent
- retry/dead-letter behavior must be defined
- event meaning must be stable and documented

---

## 6. API and Contract Governance

Every service contract must be treated as a product.

Minimum expectations:
- documented OpenAPI for HTTP APIs
- explicit auth rules
- versioning/deprecation approach
- stable error contract
- backward compatibility policy
- consumer impact review for breaking changes

Strongly recommended:
- consumer-driven contract testing where appropriate
- examples for critical payloads
- schema validation in CI for important APIs/events

A service is not autonomous if its contracts change casually.

---

## 7. Resilience Standard

Distributed systems fail in more ways than monoliths.
Resilience is mandatory.

Every service interaction should consider:
- timeout
- retry
- backoff
- circuit breaker
- bulkhead/isolation
- fallback or graceful degradation
- idempotency
- poison message handling for async flows

Rules:
- do not retry non-idempotent operations blindly
- do not stack retries across multiple layers without analysis
- surface failure semantics intentionally
- understand partial failure and duplicate delivery

Recommended tools/patterns:
- Resilience4j
- transactional outbox
- dead-letter queues
- compensating actions when business flows require them

---

## 8. Observability Standard for Microservices

Observability is not optional.

Minimum:
- structured logs
- correlation IDs
- distributed tracing
- metrics
- request latency visibility
- error-rate visibility
- dependency visibility
- deployment/version visibility
- health/readiness/liveness endpoints
- business-event observability for critical flows

Recommended stack direction:
- Micrometer
- Prometheus
- Grafana
- OpenTelemetry
- centralized logging
- trace visualization tools

You must be able to answer:
- where the request failed
- how far the workflow progressed
- which downstream dependency degraded
- which service version introduced the regression

If you cannot answer those questions, the system is not production ready.

---

## 9. Security Standard for Microservices

In addition to the shared foundation, microservices require distributed security discipline.

Define explicitly:
- user authentication model
- token propagation model
- service-to-service authentication
- authorization boundaries per service
- gateway policy
- secret distribution
- network trust assumptions
- internal vs external exposure rules

Minimum expectations:
- zero trust mindset between services where applicable
- no blind trust in "internal network" assumptions
- scoped access tokens or equivalent controls
- secure actuator exposure
- auditability of privileged or sensitive actions

---

## 10. Platform and Deployment Expectations

Microservices require operational maturity.

Expected capabilities:
- independent deploy pipelines
- service-level rollback strategy
- environment promotion model
- central secret/config management
- service discovery/routing strategy
- gateway/ingress strategy
- autoscaling or scaling policy if needed
- runtime resource governance
- deployment version traceability

Without platform maturity, microservices increase failure modes faster than they increase value.

---

## 11. Testing Strategy for Microservices

Testing must reflect distributed-system risk.

### 11.1 Unit tests
- domain logic
- use cases
- policies
- mapping
- serializers/deserializers where risk exists

### 11.2 Integration tests
- persistence
- Flyway migrations
- security behavior
- REST/messaging adapters
- external dependency adapters using realistic environments where possible

### 11.3 Contract tests
- provider and/or consumer contracts
- backward compatibility expectations
- error contract stability
- event payload compatibility

### 11.4 End-to-end tests
Use sparingly for critical system journeys.
Do not rely on E2E tests alone to prove correctness.

### 11.5 Resilience and failure tests
When justified:
- timeout behavior
- retry behavior
- duplicate message handling
- degraded dependency behavior
- concurrency edge cases

Testcontainers remains highly valuable for local realistic integration tests.

---

## 12. Data Consistency Strategy

A microservices system must explicitly document:
- where strong consistency is required
- where eventual consistency is acceptable
- compensation strategy
- reconciliation strategy
- duplicate handling strategy
- ordering assumptions

Never hide a distributed transaction problem behind vague wording.

If a business flow crosses multiple services, define:
- command owner
- event flow
- failure states
- retry/compensation
- observability signals

---

## 13. Common Failure Modes to Prevent

The architecture must actively prevent:

- distributed monolith behavior
- chatty service meshes of synchronous calls
- shared database anti-patterns
- fragile orchestration through multiple synchronous hops
- hidden coupling through copied DTOs and undocumented assumptions
- missing idempotency in consumers
- claiming autonomy without contract governance
- poor local developer experience that discourages verification

---

## 14. Local Development Standard

Even for microservices, local development must remain usable.

Expected capabilities:
- clear service startup strategy
- Docker Compose or equivalent local orchestration where helpful
- local PostgreSQL and infrastructure dependencies
- seeded data or fixtures where justified
- smoke verification commands per service
- isolated service testing when possible
- reproducible developer workflow

A system that only works in the shared environment is not healthy.

---

## 15. Microservices Definition of Done

A microservice change is production-ready only when:

- service ownership is clear
- data ownership is preserved
- contracts are documented and reviewed
- backward compatibility impact is understood
- retries/timeouts/failure behavior are designed
- logs, traces, and metrics support operations
- tests include the right contract/integration level
- migrations are safe
- security is explicit
- deployment/rollback implications are known
- eventual consistency consequences are acknowledged and acceptable

---

## 16. Final Rule

Microservices are justified only when the business and operating model need them.
If teams distribute code without distributed-system discipline, they do not get microservices.
They get complexity without autonomy.
