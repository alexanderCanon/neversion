# MVP Testing Scope

This document defines the MVP testing scope for the Spring Boot monolith application, prioritized according to business risk, architectural importance, and production readiness rather than mere test volume.

## 1. Domain Unit Tests

Validates pure business rules, aggregations, value objects, and domain invariants. No Spring context or infrastructure should be involved here.

**Priorities:**
* **Reservations:** Calculation of pricing (`BR-02`) and combo discounts (`BR-03`). Testing the `ReservationPricingService`.
* **Subscriptions:** Testing that the subscription `status` logic transitions correctly (especially to `CANCELLED`, `BR-11`).
* **Accounts/Profiles:** The logic dictating profile creation limits (`BR-01`) and the query-time calculated availability rules (`BR-06`).

## 2. Application/Use Case Tests

Validates orchestration logic, use cases, and interaction with domain ports. Use Mockito for external ports.

**Priorities:**
* **Subscriptions (`AssignSubscriptionUseCase`, `UpdateSubscriptionUseCase`):** Anti-overbooking guard logic (`BR-04`). Verify that a profile cannot have two active subscriptions.
* **Reservations (`CreateReservationUseCase`, `UploadReceiptUseCase`):** Verify receipt URL uniqueness (`BR-05`). Verify reservation state machine transitions (e.g. `PENDING` -> `UPLOADED` -> `VALIDATED`).
* **Accounts (`CreateAccountUseCase`):** Verify that N blank profiles are auto-generated when a new account is created according to service max limits (`BR-01`).

## 3. Repository Integration Tests

Validates custom queries, mappings, and database constraints against a real PostgreSQL instance using Testcontainers.

**Priorities:**
* **Flyway Migrations:** Baseline validation that `V1`, `V2`, `V3` apply successfully.
* **Database Views:** Verify that the views `upcoming_renewals` and `upcoming_account_renewals` compute the `days_until_due` correctly.
* **Subscriptions (`SpringDataSubscriptionRepository`):** Verify the unique constraint `unique_active_profile` (`BR-04`). Ensure finding by status works as expected for automations (`BR-10`).
* **Reservations (`SpringDataReservationRepository`):** Verify the `subtotal` generation logic in `reservation_details` table and relations.

## 4. Controller/API Tests

Validates HTTP request parsing, response mappings, structure, validation errors, and alignment with the OpenAPI contract.

**Priorities:**
* **Reservation Checkout Flow:** Check validation constraints on `CreateReservationRequest` and `UploadReceiptRequest`.
* **Subscription Assignment:** Verify 400/409 HTTP status responses for domain exceptions like `AccountOverbookingException`.
* **Dashboard Retrieval:** Ensure the dynamic calculated fields (like `AccountAvailability`) are correctly formatted in JSON responses.

## 5. Security Tests

Validates authentication and authorization using Spring Security OAuth2.

**Priorities:**
* Ensure public endpoints (`/v3/api-docs/**`, `/actuator/**`) are freely accessible.
* Verify that missing or invalid JWT tokens result in 401 Unauthorized for protected endpoints.
* Validates RBAC configurations defined in the `*SecurityConfig` classes per domain.

## 6. Critical End-to-End (E2E) Tests

High-value, complete flow validations for MVP. These test the system entirely, from HTTP down to DB.

**Priorities:**
1. **The Checkout Flow:** Create a Reservation -> Upload a Receipt -> Validate the Reservation (Order created).
2. **The Subscription Flow:** Create an Account -> N Profiles auto-generated -> Assign a Client to a Profile -> Subscription created. Ensure trying to assign a second client to that profile yields an error.
