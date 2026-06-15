# Backend Technical Debt — Remediation Plan
**Project:** Neversion API (`apps/api`)  
**Assessed:** 2026-06-15  
**Overall backend maturity:** ~77% production-ready  
**Architecture:** Spring Boot 3.5 / Java 17 / Hexagonal + DDD / PostgreSQL

---

## Context for the implementing agent

This document is the authoritative input for remediating identified technical debt in the Neversion backend. The codebase is a real hexagonal monolith with disciplined layering (domain → application → infrastructure). The business domain is **multi-tenant SaaS for reselling streaming platform access** — vendors manage accounts/profiles and sell subscriptions to clients.

Key domain concepts to understand before touching any code:
- **Account** — a streaming platform account (e.g. a Netflix account). Has `SaleMode`: `FULL_ACCOUNT` (sell the whole account) or `PER_PROFILE` (sell individual profiles).
- **Profile** — a slot inside an account. Has `ProfileStatus`: `available`, `reserved`, `active`, `expired`.
- **Subscription** — the commercial link between a client and a profile. Has `SubStatus`: `active`, `pending`, `suspended`, `cancelled`.
- **Vendor** — the reseller. All data is tenant-scoped to a vendor.

> **Status values are stored in lowercase in the database and displayed in lowercase in the UI. Java enums use UPPERCASE by convention only.**

---

## Debt Inventory

### Category A — Operational Risk (fix soon, affects data integrity)

#### A1 — Inconsistent state across tables (no structural guarantee)
**Files affected:**
- `subscription/application/service/UpdateSubscriptionService.java` — `suspend()` was not updating `profiles` (bug found and fixed 2026-06-15, but similar omissions may exist in other paths)
- `subscription/application/service/DetectExpiredSubscriptionsService.java` — `expireInventory()`
- `subscription/application/service/RevokeSubscriptionService.java` — `releaseInventory()`

**Problem:** When a subscription changes status, 2–3 tables must be updated atomically (`subscriptions`, `profiles`, `accounts`). There is no structural mechanism that forces this — each developer must remember to do it manually. A missed update leaves the system in a silent inconsistent state.

**Risk:** A profile stuck in `active` status after its subscription is `suspended` can block new assignments or mislead the vendor's inventory view.

#### A2 — Ownership checks manual and dispersed
**Files affected:** Every application service that accepts a `callerExternalId` parameter:
- `ClientService.java`, `RevokeSubscriptionService.java`, `CreateManualSubscriptionService.java`, `ListSubscriptionsService.java`, etc.

**Problem:** Each service independently implements `resolveVendorId(callerExternalId)` + ownership assertion. Pattern is repeated verbatim across ~8 services. If a new endpoint is added and the developer forgets the check, cross-tenant data leaks silently.

**Current duplicated pattern (example from `RevokeSubscriptionService`):**
```java
private Long resolveVendorId(String callerExternalId) {
    var user = userRepositoryPort.findByExternalId(callerExternalId)
            .orElseThrow(() -> new ResourceNotFoundException(...));
    return vendorRepositoryPort.findByUserId(user.getId())
            .orElseThrow(() -> new ResourceNotFoundException(...))
            .getId();
}
```

#### A3 — N+1 queries in subscription list endpoint
**File:** `subscription/infrastructure/adapters/in/rest/controller/SubscriptionController.java`

**Problem:** `toListResponse()` (line ~188) performs 4 separate repository queries per subscription (profile, client, account, service). With 100 subscriptions this produces 400 queries per list request. Logic is embedded directly in the controller, not in a use case.

```java
private SubscriptionResponse toListResponse(Subscription subscription) {
    var profile = profileRepositoryPort.findByInternalId(subscription.getProfileId())...
    var client = clientRepositoryPort.findByInternalId(subscription.getClientId())...
    Account account = accountRepositoryPort.findByInternalId(profile.getAccountId())...
    var service = serviceRepositoryPort.findByInternalId(account.getServiceId())...
    return subscriptionMapper.toResponse(subscription, profile, client, account, service);
}
```

---

### Category B — Maintenance Debt (hurts medium-term)

#### B1 — `releaseInventory()` / `expireInventory()` duplicated across services
**Files affected:**
- `RevokeSubscriptionService.java` — `releaseInventory()`
- `DetectExpiredSubscriptionsService.java` — `expireInventory()`
- `UpdateSubscriptionService.java` — partial, sets `RESERVED` only

**Problem:** The `FULL_ACCOUNT` vs per-profile bifurcation logic is implemented three times. A change in business rules (e.g. a new `SaleMode`) requires updating three files.

#### B2 — Frontend assignment form duplicated
**Tracked in:** `docs/backlog/tech-debt-panel-assignment-form.md`  
~150 lines of identical logic between `subscription-form.component.ts` and `manual-assignment-modal.component.ts`.

#### B3 — `confirm()` native browser dialogs in panel
**Count:** ~6 usages across subscriptions, clients, reservations pages.  
**Problem:** Not styleable, blocks the JS thread, breaks in some SSR contexts, inconsistent UX.

---

### Category C — Architectural Debt (hurts long-term)

#### C1 — No domain events
State transitions are handled via direct service-to-service calls. Adding cross-cutting concerns (audit log, webhooks, metrics) requires modifying existing services.

#### C2 — Notifications coupled inside business transactions
`NotificationLogPort.record()` is called inside `@Transactional` business methods. A failure in notification infrastructure can potentially affect the main transaction, or a rollback can leave notification records in an inconsistent state.

#### C3 — No subscription state history / audit trail
There is no `subscription_events` table. If a client disputes access or a vendor disputes a charge, there is no record of who triggered which state transition and when.

---

## Remediation Plan

### Phase 1 — Contain the risk (no refactor, only safety nets)
**Goal:** Prevent silent consistency bugs from reaching production.  
**Estimated effort:** 1–2 weeks  
**Do not touch existing service logic.**

Tasks:
1. Write integration tests (Testcontainers) for all critical state transition flows:
   - `suspend()` → assert `profile.status = 'reserved'`
   - `revoke()` → assert `profile.status = 'available'`, `account.status` if `FULL_ACCOUNT`
   - `detectAndSuspend()` → assert `profile.status = 'expired'`
   - `assign()` → assert anti-overbooking throws on second active subscription for same profile
2. Add a scheduled reconciliation job (`@Scheduled`) that runs nightly, queries for subscriptions in `active`/`suspended` status whose profile is in an inconsistent state, and logs a `WARN` entry — **no automatic correction**, only alerting.

---

### Phase 2 — Extract duplicated inventory logic (surgical refactor)
**Goal:** Single source of truth for inventory state transitions.  
**Estimated effort:** 3–5 days  
**Prerequisite:** Phase 1 tests must be green before starting.

Tasks:
1. Create `profile/domain/service/InventoryStateService.java` (domain service) with:
   - `releaseForSubscription(Account account, Profile profile)` — sets profile/account to `available`
   - `suspendForSubscription(Account account, Profile profile)` — sets profile to `reserved`
   - `expireForSubscription(Account account, Profile profile)` — sets profile/account to `expired`
2. Refactor `RevokeSubscriptionService`, `DetectExpiredSubscriptionsService`, and `UpdateSubscriptionService` to inject and delegate to `InventoryStateService`.
3. Delete the three private `releaseInventory()` / `expireInventory()` methods.

---

### Phase 3 — Centralize ownership security
**Goal:** Make it structurally impossible to forget vendor ownership checks.  
**Estimated effort:** 1 week  
**Prerequisite:** Phase 2 complete.

Tasks:
1. Create `shared/application/service/VendorSecurityService.java` with:
   - `resolveVendorId(String callerExternalId): Long`
   - `assertOwnership(Long callerVendorId, Long resourceVendorId, String resourceDescription)`
2. Refactor all application services that currently implement their own `resolveVendorId()` to inject `VendorSecurityService`.
3. Delete all private `resolveVendorId()` duplicates.

---

### Phase 4 — Observability (when real users exist)
**Goal:** Know what happened without debugging in production.  
**Estimated effort:** 1–2 weeks  
**Can be done in parallel with Phase 3.**

Tasks:
1. Add Flyway migration: `subscription_events(id, subscription_id, from_status, to_status, triggered_by, created_at)`.
2. Create `SubscriptionEventPort` (out port) and JPA adapter.
3. Append an event record after every successful state transition in `UpdateSubscriptionService`, `RevokeSubscriptionService`, `DetectExpiredSubscriptionsService`, `RenewSubscriptionService`.
4. Move `NotificationLogPort.record()` calls outside of `@Transactional` scope using `TransactionSynchronizationManager.registerSynchronization()` with `afterCommit()` — ensures notification is only attempted if the transaction committed successfully.

---

### Phase 5 — Fix N+1 (performance)
**Goal:** Subscription list loads in O(1) queries regardless of result size.  
**Estimated effort:** 2–3 days  

Tasks:
1. Add a JPQL query in `JpaSubscriptionAdapter` that joins `profiles`, `clients`, `accounts`, `services` in a single query returning a projection DTO.
2. Move `toListResponse()` logic from `SubscriptionController` into `ListSubscriptionsUseCase` / a dedicated `ListSubscriptionsService`.
3. Delete the four repository calls inside the controller.

---

## Recommended execution order

| Priority | Item | Rationale |
|---|---|---|
| 1 | A3 (N+1 queries) | Immediate performance impact, self-contained fix |
| 2 | Phase 1 (integration tests) | Safety net before any refactor |
| 3 | Phase 2 (inventory service) | Removes the class of bug found today |
| 4 | Phase 3 (ownership central) | Security correctness |
| 5 | Phase 4 (audit trail) | Required before production launch |
| 6 | B3 (confirm() dialogs) | UX polish, frontend only |

---

## What NOT to do

- Do not refactor all services at once. One phase at a time, tests green between phases.
- Do not introduce MapStruct or other annotation processors — project convention is manual mappers.
- Do not add `@Autowired` field injection — constructor injection only.
- Do not merge to `develop` until Alex reviews and confirms tests pass.
- Do not modify `docs/implementation/` files — they are historical read-only logs.
