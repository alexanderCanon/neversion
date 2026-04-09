# Phased Testing Plan

Implementing tests for the entire monolith at once is impractical. This plan defines a structured, phased approach, starting from the most critical, least framework-dependent business rules and expanding outward.

## Phase 1: Core Domain Rules (Unit Tests)
**Goal:** Establish confidence in the system's pure business logic without spinning up Spring context.

1. **Reservation Pricing (`BR-02`, `BR-03`):**
   * Target: `ReservationPricingServiceUT`
   * Validate discount calculations and total computations.
2. **Account Availability (`BR-06`):**
   * Target: Models or domain logic computing `AccountAvailability` based on associated profiles.
3. **Subscription State Machine (`BR-11`):**
   * Target: `Subscription` domain model. Verify valid transitions to `CANCELLED` and `SUSPENDED`.

## Phase 2: Application Use Cases (Unit Tests with Mocks)
**Goal:** Validate orchestrations, rule enforcement, and external port interactions using Mockito.

1. **Anti-Overbooking Guard (`BR-04`):**
   * Target: `SubscriptionServiceUT` / `AssignSubscriptionUseCase`.
   * Mock repositories to simulate a profile that already has an active subscription and ensure the `AccountOverbookingException` is thrown.
2. **Profile Generation (`BR-01`):**
   * Target: `CreateAccountServiceUT`.
   * Verify that creating an account automatically requests the creation of N default profiles via the `ProfileUseCase` port.
3. **Receipt Uniqueness (`BR-05`):**
   * Target: `UploadReceiptServiceUT`.
   * Mock the reservation repository to ensure reusing a receipt URL triggers the correct business exception.

## Phase 3: Infrastructure and Persistence (Integration Tests)
**Goal:** Ensure the database schemas, Flyway migrations, and Spring Data mappings work correctly with a real PostgreSQL instance via Testcontainers.

1. **Base Configuration:**
   * Ensure `BaseIntegrationTest` is correctly set up with `@Testcontainers` and `@ServiceConnection`.
2. **Subscription Repository (`SpringDataSubscriptionRepositoryIT`):**
   * Test the `unique_active_profile` constraint explicitly by attempting to persist two active subscriptions for the same profile.
   * Verify query methods like `findByStatus` are correct.
3. **Database Views and Migrations (`FlywayIT` / Repository):**
   * Persist test data and query the `upcoming_renewals` view to verify date math (`days_until_due`).

## Phase 4: API, Web, and Security (Integration Tests)
**Goal:** Ensure REST controllers map payloads correctly, handle HTTP statuses appropriately, and apply RBAC rules.

1. **Security Filters (`SecurityFilterChainIT`):**
   * Verify `/v3/api-docs` returns 200 without a token.
   * Verify `/api/v1/subscriptions` returns 401 without a valid JWT.
2. **Reservation Endpoints (`ReservationControllerIT`):**
   * Test JSON validation (400 Bad Request) on missing required fields for checkout payloads.
3. **Subscription Endpoints (`SubscriptionControllerIT`):**
   * Ensure the controller maps the `AccountOverbookingException` to a 409 Conflict using the global exception handler.

## Phase 5: Critical E2E Flows
**Goal:** Prove the full stack for the highest-value MVP features.

1. **E2E Checkout Flow:**
   * Submit reservation -> Upload receipt -> Validate reservation. Verify DB state at the end.
2. **E2E Onboarding Flow:**
   * Create Service -> Create Account -> Verify Profiles are generated -> Assign Subscription -> Verify Subscription is active.
