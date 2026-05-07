# Phased Testing Plan

Five sprints to bring the Neversion API from current state (40 tests, zero repository/controller ITs) to production-grade coverage.

---

## Sprint A: Stabilize and Fill Unit Test Gaps

**Goal**: Achieve UT coverage for every application service that contains business logic. Fix the naming violation.

### Housekeeping

- Delete `SubscriptionServiceTest.java` (the old file). `SubscriptionServiceUT.java` already exists as the correctly named replacement in the same package.

### New test classes

#### service module
**Package**: `com.neversion.api.service.application.service`

| Class | Scenarios |
|-------|-----------|
| `DigitalServiceServiceUT.java` | `create_shouldSaveService`, `create_shouldThrowBusinessRuleException_whenNameAlreadyExists` (BR-17), `getById_shouldReturnService_whenFound`, `getById_shouldThrowResourceNotFound_whenNotFound`, `getAll_shouldReturnAllServices`, `update_shouldUpdateExistingFields`, `update_shouldThrowResourceNotFound_whenNotFound`, `delete_shouldDeleteService_whenFound`, `delete_shouldThrowResourceNotFound_whenNotFound` |

#### profile module
**Package**: `com.neversion.api.profile.application.service`

| Class | Scenarios |
|-------|-----------|
| `ProfileServiceUT.java` | `save_shouldDelegateSaveToRepository`, `findById_shouldReturnProfile_whenFound`, `findByAccountId_shouldDelegateToRepository`, `findAvailableByAccountId_shouldDelegateToRepository`, `generateProfilesForAccount_shouldCreateNProfiles` (BR-01, verify count, owner flag on first profile, names "Perfil 1..N"), `generateProfilesForAccount_shouldSetFirstProfileAsOwner`, `deleteById_shouldDeleteProfile_whenFound`, `deleteById_shouldThrowResourceNotFound_whenNotFound` |

#### reservation module
**Package**: `com.neversion.api.reservation.application.service`

| Class | Scenarios |
|-------|-----------|
| `CreateReservationServiceUT.java` | `create_shouldPersistReservationWithPendingStatus`, `create_shouldSetExpirationDate60MinutesFromNow` (BR-12), `create_shouldCalculatePricingUsingDomainService` (BR-03), `create_shouldResolveClientId_whenClientUuidProvided`, `create_shouldAllowNullClientUuid`, `create_shouldPersistAllDetails` |
| `UploadReceiptServiceUT.java` | `uploadReceipt_shouldTransitionPendingToUploaded`, `uploadReceipt_shouldThrowBusinessRuleException_whenStatusNotPending`, `uploadReceipt_shouldThrowBusinessRuleException_whenReceiptUrlAlreadyUsed` (BR-14/BR-05), `uploadReceipt_shouldThrowResourceNotFound_whenReservationNotFound` |
| `ValidateReservationServiceUT.java` | `validate_shouldTransitionUploadedToValidated`, `validate_shouldTriggerOrderCreation`, `validate_shouldThrowBusinessRuleException_whenStatusNotUploaded`, `validate_shouldThrowResourceNotFound_whenReservationNotFound` |
| `ReservationPricingServiceUT.java` | `calculateGrossTotal_shouldSumQtyTimesUnitPrice`, `calculateComboDiscount_shouldReturn2Percent_whenTwoOrMoreItems` (BR-03), `calculateComboDiscount_shouldReturnZero_whenSingleItem`, `calculateFinalTotal_shouldSubtractDiscount` |

#### order module
**Package**: `com.neversion.api.order.application.service`

| Class | Scenarios |
|-------|-----------|
| `GetOrderServiceUT.java` | `getById_shouldReturnOrder_whenFound`, `getById_shouldReturnEmpty_whenNotFound`, `getByReservationId_shouldDelegateToRepository` |
| `CreateOrderServiceUT.java` | `createFromReservation_shouldPersistOrder` (dependent on actual `CreateOrderService` implementation) |

#### account module (gap fill)
**Package**: `com.neversion.api.account.application.service`

| Class | Scenarios |
|-------|-----------|
| `DeleteAccountServiceUT.java` | `delete_shouldDeleteAccount_whenFound`, `delete_shouldThrowResourceNotFound_whenNotFound` |

**Estimated new tests**: ~45 tests across 9 new classes.

---

## Sprint B: Repository Integration Tests

**Goal**: Verify that every JPA adapter correctly translates between domain models and PostgreSQL via Flyway-managed schema.

All classes extend `BaseIntegrationTest`, annotated with `@SpringBootTest`.

### New test classes

#### account module
**Package**: `com.neversion.api.account.infrastructure.adapters.out`

| Class | Scenarios |
|-------|-----------|
| `AccountRepositoryIT.java` | `save_shouldPersistAndReturnAccountWithUuid`, `findById_shouldReturnAccount_whenExists`, `findById_shouldReturnEmpty_whenNotExists`, `findByServiceId_shouldReturnMatchingAccounts`, `findAll_shouldReturnAllNonDeletedAccounts`, `softDelete_shouldMarkDeletedAtButRetainRow` |

#### service module
**Package**: `com.neversion.api.service.infrastructure.adapters.out`

| Class | Scenarios |
|-------|-----------|
| `ServiceRepositoryIT.java` | `save_shouldPersistService`, `existsByName_shouldReturnTrue_whenDuplicate` (BR-17), `existsByName_shouldReturnFalse_whenUnique`, `findById_shouldReturnService`, `findAll_shouldReturnAllServices`, `deleteById_shouldSoftDelete` |

#### profile module
**Package**: `com.neversion.api.profile.infrastructure.adapters.out`

| Class | Scenarios |
|-------|-----------|
| `ProfileRepositoryIT.java` | `save_shouldPersistProfile`, `saveAll_shouldPersistMultipleProfiles`, `findByAccountId_shouldReturnLinkedProfiles`, `findAvailableByAccountId_shouldExcludeOccupiedProfiles`, `deleteById_shouldSoftDeleteProfile` |

#### client module
**Package**: `com.neversion.api.client.infrastructure.adapters.out`

| Class | Scenarios |
|-------|-----------|
| `ClientRepositoryIT.java` | `save_shouldPersistClient`, `findById_shouldReturnClient`, `findByName_shouldReturnMatchingClients`, `findByPhone_shouldReturnMatchingClients`, `deleteById_shouldSoftDeleteClient`, `findAll_shouldExcludeSoftDeletedClients` |

#### subscription module
**Package**: `com.neversion.api.subscription.infrastructure.adapters.out`

| Class | Scenarios |
|-------|-----------|
| `SubscriptionRepositoryIT.java` | `save_shouldPersistSubscription`, `existsActiveByProfileId_shouldReturnTrue_whenActiveExists` (BR-04), `existsActiveByProfileId_shouldReturnFalse_whenNoneActive`, `findById_shouldReturnSubscription` |

#### reservation module
**Package**: `com.neversion.api.reservation.infrastructure.adapters.out`

| Class | Scenarios |
|-------|-----------|
| `ReservationRepositoryIT.java` | `save_shouldPersistReservation`, `saveDetail_shouldLinkToReservation`, `findById_shouldReturnReservation`, `existsByReceiptUrl_shouldReturnTrue_whenDuplicate` (BR-14), `existsByReceiptUrl_shouldReturnFalse_whenUnique`, `update_shouldPersistStatusTransition` |

#### order module
**Package**: `com.neversion.api.order.infrastructure.adapters.out`

| Class | Scenarios |
|-------|-----------|
| `OrderRepositoryIT.java` | `save_shouldPersistOrder`, `findById_shouldReturnOrder`, `findByReservationId_shouldReturnLinkedOrder` |

**Estimated new tests**: ~35 tests across 7 new classes.

---

## Sprint C: Controller Integration Tests

**Goal**: Verify the full HTTP cycle -- JSON serialization, request validation, status codes, error responses -- for the highest-risk endpoints.

All classes extend `BaseIntegrationTest`, annotated with `@SpringBootTest` + `@AutoConfigureMockMvc`. Use a JWT test utility to mint valid admin tokens.

### New test classes

#### account module
**Package**: `com.neversion.api.account.infrastructure.adapters.in.rest.controller`

| Class | Scenarios |
|-------|-----------|
| `AccountControllerIT.java` | `POST /api/v1/accounts` returns 201 with valid admin JWT + valid body; returns 400 with missing required fields; returns 401 without token. `GET /api/v1/accounts` returns 200 with admin JWT. `GET /api/v1/accounts/{uuid}` returns 200 when found, 404 when not found. |

#### service module
**Package**: `com.neversion.api.service.infrastructure.adapters.in.rest.controller`

| Class | Scenarios |
|-------|-----------|
| `ServiceControllerIT.java` | `GET /api/v1/services` returns 200 without token (public). `POST /api/v1/services` returns 201 with admin JWT, 409 when name already exists (BR-17), 401 without token. `PUT /api/v1/services/{uuid}` returns 200. `DELETE /api/v1/services/{uuid}` returns 204. |

#### reservation module
**Package**: `com.neversion.api.reservation.infrastructure.adapters.in.rest`

| Class | Scenarios |
|-------|-----------|
| `ReservationControllerIT.java` | `POST /api/v1/reservations` returns 201 with valid body. `PUT /api/v1/reservations/{id}/receipt` returns 200, 409 when receipt URL already used. `PUT /api/v1/reservations/{id}/validate` returns 200 with admin JWT, 409 when status is not UPLOADED. Full lifecycle: create (PENDING) -> upload receipt (UPLOADED) -> validate (VALIDATED, order created). |

#### subscription module
**Package**: `com.neversion.api.subscription.infrastructure.adapters.in.rest.controller`

| Class | Scenarios |
|-------|-----------|
| `SubscriptionControllerIT.java` | `POST /api/v1/subscriptions` returns 201 with admin JWT, 409 on overbooking (BR-04). `PUT /api/v1/subscriptions/{uuid}/suspend` returns 200, 409 when not ACTIVE. `PUT /api/v1/subscriptions/{uuid}/terminate` returns 200, 409 when already CANCELLED. |

**Estimated new tests**: ~25 tests across 4 new classes.

---

## Sprint D: Per-Module RBAC Security Tests

**Goal**: Verify that every protected endpoint rejects unauthenticated requests and that ROLE_ADMIN grants access. Extend `SecurityFilterChainIT` or create per-module security ITs.

### Approach

Extend `BaseIntegrationTest` + `@AutoConfigureMockMvc`. For each module, test three scenarios per endpoint:

1. No token -> 401
2. Valid JWT without `ROLE_ADMIN` -> 403
3. Valid JWT with `ROLE_ADMIN` -> 2xx

### New test classes

**Package**: `com.neversion.api`

| Class | Endpoints covered |
|-------|-------------------|
| `AccountSecurityIT.java` | `POST /api/v1/accounts`, `GET /api/v1/accounts`, `GET /api/v1/accounts/{uuid}`, `DELETE /api/v1/accounts/{uuid}` |
| `ServiceSecurityIT.java` | `POST /api/v1/services` (admin), `GET /api/v1/services` (public, verify no 401), `PUT /api/v1/services/{uuid}`, `DELETE /api/v1/services/{uuid}` |
| `ProfileSecurityIT.java` | `GET /api/v1/profiles/{uuid}`, `DELETE /api/v1/profiles/{uuid}` |
| `ClientSecurityIT.java` | `POST /api/v1/clients`, `GET /api/v1/clients`, `PUT /api/v1/clients/{uuid}`, `DELETE /api/v1/clients/{uuid}` |
| `SubscriptionSecurityIT.java` | `POST /api/v1/subscriptions`, `PUT /api/v1/subscriptions/{uuid}/suspend`, `PUT /api/v1/subscriptions/{uuid}/terminate` |
| `ReservationSecurityIT.java` | `POST /api/v1/reservations`, `PUT /api/v1/reservations/{id}/receipt`, `PUT /api/v1/reservations/{id}/validate` (admin only) |
| `OrderSecurityIT.java` | `GET /api/v1/orders/{uuid}`, `GET /api/v1/orders/reservation/{uuid}` |
| `DashboardSecurityIT.java` | `GET /api/v1/dashboard/products`, `GET /api/v1/dashboard/accounts/{productId}`, `GET /api/v1/dashboard/profiles/{accountId}` |

**Estimated new tests**: ~50 tests across 8 new classes.

---

## Sprint E: End-to-End Scenario Tests

**Goal**: Verify multi-step business flows that cross module boundaries. These are full-context integration tests with real database state.

### New test classes

**Package**: `com.neversion.api.e2e`

| Class | Scenario |
|-------|----------|
| `AccountCreationE2EIT.java` | Create a Service -> Create an Account (BY_PROFILE) -> Verify N Profiles auto-generated -> Verify first profile is owner -> Query profiles by account ID -> Assert count matches `service.maxProfiles`. Covers BR-01 end-to-end. |
| `ReservationLifecycleE2EIT.java` | Create Reservation (PENDING) -> Upload receipt (UPLOADED) -> Validate (VALIDATED) -> Verify Order created -> Verify reservation status transitions. Also test: expired reservation cannot be uploaded; cancelled reservation cannot be validated. Covers BR-12, BR-13, BR-14. |
| `SubscriptionLifecycleE2EIT.java` | Create Account + Profiles -> Assign subscription to profile (ACTIVE) -> Attempt second subscription on same profile (409 overbooking) -> Suspend subscription (SUSPENDED) -> Terminate subscription (CANCELLED) -> Verify profile is now available. Covers BR-04, BR-06. |
| `FullSaleFlowE2EIT.java` | Service creation -> Account creation -> Profile auto-generation -> Client creation -> Reservation creation -> Receipt upload -> Validation -> Order created -> Subscription assigned to profile. The complete happy path. |

Each E2E test uses `MockMvc` with a valid admin JWT to drive the API and verifies state through both HTTP responses and direct repository queries.

**Estimated new tests**: ~15 tests across 4 new classes.

---

## Summary

| Sprint | Focus | New classes | Estimated tests |
|--------|-------|-------------|-----------------|
| A | Unit test gaps + housekeeping | 9 | ~45 |
| B | Repository ITs | 7 | ~35 |
| C | Controller ITs | 4 | ~25 |
| D | RBAC security ITs | 8 | ~50 |
| E | E2E scenario ITs | 4 | ~15 |
| **Total** | | **32** | **~170** |

Combined with the existing 40 tests, this brings the suite to approximately **210 tests** covering all layers of the hexagonal architecture.
