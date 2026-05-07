---
name: spring-monolith-testing-skill
description: Analyze a Spring Boot monolith and produce the context, standards, and phased plan required to implement production-oriented tests for an MVP.
---

# spring-monolith-testing-skill

## Purpose
This skill helps an agent understand a Spring Boot monolith and its documentation in order to define and implement production-oriented tests for an MVP.

## When to Use This Skill
Use this skill when working with a Spring Boot monolith that needs a structured, production-oriented testing strategy. This skill is especially useful when the project already includes elements such as Spring Security, JPA, Flyway, OpenAPI, Testcontainers, and a layered or hexagonal design.

Use it when the goal is to:
- understand what project context is required before writing tests
- identify essential test layers for MVP production readiness
- extract structured testing context from code and documentation
- create a phased implementation plan for testing
- prepare reusable testing artifacts for specialized agents or developers

Do not use this skill for microservices, frontend testing, performance testing, or CI/CD pipeline design.

## Supported Project Profile
This skill is designed for:
- Spring Boot monoliths
- Spring Security
- Spring Data JPA
- Hexagonal Architecture
- DDD-inspired design
- PostgreSQL
- Flyway
- OpenAPI
- Testcontainers

---

## Project Intake
The agent must inspect the project in a prioritized way before proposing or generating tests. It must not read the entire codebase without purpose.

Inspect the following sources in this order:
1. Project documentation related to architecture and requirements
2. OpenAPI specification or API contracts
3. Spring Security configuration
4. Flyway migrations
5. Test profile and test-related configuration
6. Package structure of the codebase
7. Existing test classes and test utilities

The goal of this intake is to understand the system boundaries, identify critical flows, detect test-relevant constraints, and determine which parts of the codebase require deeper inspection.

## Context Extraction Rules
After the intake, the agent must extract and structure the minimum testing context required to guide implementation.

The extracted context must include:
1. Architecture overview
2. Use cases
3. Domain invariants
4. API endpoints and contracts
5. Security rules
6. Persistence and database constraints
7. Existing test assets

This information should be structured rather than kept as a purely narrative summary whenever possible.

The agent must not assume business rules without evidence from code or documentation. If relevant information is missing or ambiguous, it must state that explicitly.

---

## MVP Testing Strategy
The skill must recommend the following minimum test layers for a production-oriented Spring Boot monolith MVP:

1. Domain unit tests
2. Application or use case tests
3. Repository integration tests
4. Controller or API tests
5. Security tests
6. Critical end-to-end tests

These layers should be prioritized according to business risk, architectural importance, and production readiness rather than test volume.

Domain unit tests must validate domain invariants, value objects, aggregates, and pure business rules without relying on Spring or infrastructure.

Application or use case tests must validate orchestration logic, interaction with ports, and expected success and failure paths. Mocks may be used here for external dependencies or ports.

Repository integration tests must validate JPA mappings, custom queries, and persistence behavior against PostgreSQL using Testcontainers.

Controller or API tests must validate request handling, response structure, validation errors, and HTTP behavior, and they should remain aligned with the OpenAPI contract when available.

Security tests must verify authentication, authorization, role restrictions, and protected endpoint behavior.

Critical end-to-end tests should cover only the most important business flows for the MVP. They should be few, high-value, and carefully selected.

## Testing Standards
The skill must enforce the following standards:

1. Use JUnit 5 as the testing framework
2. Use Mockito only in the application layer when mocking ports or external dependencies
3. Do not use H2 as a replacement for PostgreSQL in integration tests
4. Use Testcontainers with PostgreSQL for persistence-related integration tests
5. Keep domain tests framework-independent whenever possible
6. Prefer deterministic and isolated tests
7. Use explicit naming conventions for test classes and methods:
   - Suffix `UT` for unit test classes
   - Suffix `IT` for integration test classes
   - Keep class names concise and descriptive
   - Avoid excessively long class names
   - Use test method names that describe behavior clearly
   - Add brief documentation when the class purpose is not obvious from the name alone
8. Align API tests with OpenAPI contracts
9. Reuse fixtures, builders, and test utilities when appropriate
10. Prefer AssertJ `assertThat` assertions over basic JUnit assertions whenever possible
11. Do not rely on code coverage percentage as the only quality metric

---

## Output Artifacts
The skill should produce or help produce the following artifacts:

1. `testing-standards.md`
2. `test-scope.md`
3. `context-pack/`
4. `phased-testing-plan.md`
5. `reusable-test-templates.md`
6. `suggested-test-profile-structure.md`

The `context-pack/` directory should contain structured artifacts such as:
- `architecture.json`
- `use-cases.json`
- `domain-invariants.json`
- `api-contracts.json`
- `security-rules.json`
- `persistence-constraints.json`
- `existing-test-assets.json`

These artifacts should be clear, reusable, and suitable for handoff to another specialized agent or developer.

## Anti-Patterns and Constraints
The skill must prevent the following:

1. Reading the entire codebase without prioritization
2. Assuming business rules without evidence from code or documentation
3. Mixing domain testing concerns with infrastructure concerns
4. Writing non-test-related code
5. Overusing mocks in domain tests
6. Creating excessively broad end-to-end test coverage for an MVP
7. Using code coverage percentage as the sole definition of quality
8. Duplicating fixtures or test utilities unnecessarily
9. Creating unclear or excessively verbose test names

If the project context is incomplete, ambiguous, or inconsistent, the agent must say so explicitly instead of inventing missing details.

## Workflow
The skill must guide the agent through the following sequence:

1. Validate project fit
2. Run project intake
3. Extract structured context
4. Define MVP testing scope
5. Apply testing standards
6. Produce a phased testing plan
7. Generate or prepare reusable testing artifacts

During this workflow, the agent should prioritize clarity, structure, and explicit reasoning about scope and risk.

If the project does not fit the skill profile, the agent should state that clearly and limit its recommendations accordingly.

## Response Style and Expected Behavior
When using this skill, the agent must behave as a structured and evidence-driven testing architect.

It should:
- be explicit when information is missing, ambiguous, or inconsistent
- avoid making unsupported assumptions about business rules
- prioritize structured outputs over vague summaries
- recommend phased implementation instead of an unstructured list of tests
- focus on production-oriented MVP needs rather than exhaustive coverage
- separate context extraction from test implementation concerns
- keep recommendations aligned with the project’s actual architecture and constraints

When appropriate, the agent should produce outputs that are easy to hand off to another specialized agent or developer.

The agent should prefer clarity, traceability, and practical usefulness over verbosity.

