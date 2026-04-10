# Test Scope and Coverage Matrix

Current state of test coverage across all backend modules. Assessed against existing test files under `src/test/java/com/neversion/api/`.

---

## Coverage Matrix

| Feature | UT (Application layer) | Repository IT | Controller IT | Security IT |
|---------|----------------------|---------------|---------------|-------------|
| **account** | `CreateAccountServiceUT.java` (5 tests), `GetAccountServiceUT.java` (4 tests) | -- | -- | -- |
| **service** | -- | -- | -- | `SecurityFilterChainIT` covers `GET /api/v1/services` (public), `POST /api/v1/services` (401 w/o token) |
| **profile** | -- | -- | -- | -- |
| **client** | `ClientServiceUT.java` (12 tests) | -- | -- | -- |
| **subscription** | `SubscriptionServiceTest.java` (4 tests)*, `UpdateSubscriptionServiceUT.java` (5 tests) | -- | -- | `SecurityFilterChainIT` covers `GET /api/v1/subscriptions` (401 w/o token) |
| **reservation** | -- | -- | -- | -- |
| **order** | -- | -- | -- | -- |
| **dashboard** | -- | -- | -- | `SecurityFilterChainIT` covers `GET /api/v1/dashboard/products` (401 w/o token) |

**Cross-cutting**:

| Asset | Type | Coverage |
|-------|------|----------|
| `BaseIntegrationTest.java` | Base class | Testcontainers PostgreSQL 16, `@ActiveProfiles("test")` |
| `ApplicationSmokeIT.java` | Smoke IT | Full context boot, Flyway migration, bean wiring (1 test) |
| `SecurityFilterChainIT.java` | Security IT | Public endpoints (3), protected w/o token (4), invalid JWT (2) = 9 tests |

*`SubscriptionServiceTest.java` needs rename to `SubscriptionServiceUT.java`. A correctly named copy already exists in the same package.

---

## Total Test Count

| Category | Count |
|----------|-------|
| Unit tests (UT) | 30 |
| Integration tests (IT) | 10 |
| **Total** | **40** |

---

## Gap Analysis

### Critical Gaps (blocks MVP production readiness)

1. **Zero Repository ITs**: No test verifies that JPA adapters correctly persist, query, or soft-delete against real PostgreSQL. Flyway migrations are validated only by the smoke test boot -- no CRUD assertions exist.

2. **Zero Controller ITs**: No test exercises the full HTTP request-response cycle (JSON serialization, validation annotations, status codes, error responses). Manual testing is the only verification path.

3. **Reservation module has zero tests**: This is the most complex domain module (state machine with 5 states, pricing service, expiration logic, receipt upload with uniqueness constraint BR-14). It carries the highest defect risk.

4. **Order module has zero tests**: Order creation is triggered by reservation validation and involves cross-module orchestration.

5. **Service module has zero tests**: BR-17 (unique service name) is enforced in `DigitalServiceService` but has no test coverage.

### High-Priority Gaps

6. **Profile module has zero tests**: Profile auto-generation (BR-01) is tested indirectly through `CreateAccountServiceUT`, but `ProfileService` itself (save, find, delete, `generateProfilesForAccount`) has no direct coverage.

7. **No authenticated Controller IT**: `SecurityFilterChainIT` only tests rejection paths (401). No test proves that a valid admin JWT grants access and returns correct data (200/201).

8. **Dashboard module has zero UT**: `GetProductsSummaryService`, `GetAccountsByProductService`, `GetProfilesByAccountService` are untested.

### Lower-Priority Gaps

9. **`DeleteAccountService` has no UT**: Account deactivation/deletion logic is untested.

10. **No edge-case tests for `UploadReceiptService`**: BR-05 (receipt URL uniqueness) and state guard (only PENDING can receive receipt) are untested.

---

## Priority Ranking for Sprint Planning

| Priority | Module / Layer | Rationale |
|----------|---------------|-----------|
| **P0** | Reservation UT + Controller IT | Highest complexity, customer-facing, state machine, pricing |
| **P0** | Service UT | BR-17 uniqueness constraint must be verified |
| **P1** | Repository ITs (all modules) | Without these, schema drift between Flyway and JPA is undetectable |
| **P1** | Subscription Controller IT | 409 overbooking response needs HTTP-level verification |
| **P1** | Profile UT | BR-01 auto-generation, direct service coverage |
| **P2** | Account Controller IT | CRUD + profile generation side effects |
| **P2** | Order UT | Cross-module orchestration (reservation -> order) |
| **P2** | Authenticated Security ITs | Prove that valid JWT + ROLE_ADMIN grants access |
| **P3** | Dashboard UT | Read-only queries, lower defect risk |
| **P3** | DeleteAccountService UT | Simple delegation, low risk |
