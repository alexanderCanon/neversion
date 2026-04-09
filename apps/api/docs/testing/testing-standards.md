# Testing Standards

These standards govern test implementation in the Spring Boot monolith to ensure a production-ready, reliable, and decoupled MVP.

## 1. General Principles
* **Framework:** All tests must use JUnit 5.
* **Assertions:** Prefer AssertJ (`assertThat`) over basic JUnit assertions.
* **Metrics:** Do not rely on code coverage percentage as the sole definition of quality. Focus on covering business invariants and critical flows.
* **Isolation:** Tests must be deterministic and isolated. Avoid shared state or mutable static fields across tests.
* **Test Utilities:** Reuse fixtures, object builders, and testing utilities when appropriate (e.g., using a test data builder pattern) to avoid duplication.

## 2. Mocking Guidelines
* **Mockito Limits:** Use Mockito only in the Application Layer (Use Case tests) when mocking external ports or dependencies.
* **Domain Layer:** Do not overuse mocks in the domain layer. Domain tests should be framework-independent and test pure Java business logic whenever possible. Instantiate real value objects and entities.

## 3. Persistence and Integration Testing
* **No H2:** Do not use H2 as a replacement for PostgreSQL in integration tests.
* **Testcontainers:** Use Testcontainers with a real PostgreSQL instance for persistence-related integration tests (`*IT.java`).
* Ensure Flyway migrations run successfully within the Testcontainer before testing DB operations.

## 4. API Testing
* **Alignment:** Controller and API tests must align with the OpenAPI contracts and specifications.
* Validate JSON serialization/deserialization, HTTP status codes (like 400, 404, 409), and validation annotations (`@Valid`, etc.).

## 5. Naming Conventions
* **Classes:**
  * Suffix `UT` for Unit Test classes (e.g., `ReservationPricingServiceUT`).
  * Suffix `IT` for Integration Test classes (e.g., `SpringDataSubscriptionRepositoryIT`).
  * Keep class names concise and descriptive. Avoid excessively long names.
* **Methods:**
  * Use method names that describe the behavior clearly, e.g., `givenActiveSubscription_whenSuspend_thenStatusIsSuspended`.
* **Documentation:**
  * Add a brief JavaDoc or class-level comment when the test's purpose is not obvious from the name alone.
