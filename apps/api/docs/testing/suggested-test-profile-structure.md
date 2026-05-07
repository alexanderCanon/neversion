# Suggested Test Directory Structure

Complete tree of all test classes (existing and planned), organized by module and layer. Mirrors the hexagonal source structure.

Legend:
- **existing** — file exists today
- **Sprint A** through **Sprint E** — planned creation sprint

---

```
src/test/java/com/neversion/api/
│
├── BaseIntegrationTest.java                                          ✓ existing
├── ApplicationSmokeIT.java                                           ✓ existing
├── SecurityFilterChainIT.java                                        ✓ existing
├── JwtTestHelper.java                                                ← Sprint C (shared utility)
│
├── account/
│   ├── application/service/
│   │   ├── CreateAccountServiceUT.java                               ✓ existing (5 tests)
│   │   ├── GetAccountServiceUT.java                                  ✓ existing (4 tests)
│   │   └── DeleteAccountServiceUT.java                               ← Sprint A
│   └── infrastructure/adapters/
│       ├── out/
│       │   └── AccountRepositoryIT.java                              ← Sprint B
│       └── in/rest/controller/
│           └── AccountControllerIT.java                              ← Sprint C
│
├── service/
│   ├── application/service/
│   │   └── DigitalServiceServiceUT.java                              ← Sprint A
│   └── infrastructure/adapters/
│       ├── out/
│       │   └── ServiceRepositoryIT.java                              ← Sprint B
│       └── in/rest/controller/
│           └── ServiceControllerIT.java                              ← Sprint C
│
├── profile/
│   ├── application/service/
│   │   └── ProfileServiceUT.java                                     ← Sprint A
│   └── infrastructure/adapters/
│       ├── out/
│       │   └── ProfileRepositoryIT.java                              ← Sprint B
│       └── in/rest/controller/
│           └── (no controller IT planned — profile endpoints
│                are tested through account E2E flow)
│
├── client/
│   ├── application/service/
│   │   └── ClientServiceUT.java                                      ✓ existing (12 tests)
│   └── infrastructure/adapters/
│       ├── out/
│       │   └── ClientRepositoryIT.java                               ← Sprint B
│       └── in/rest/controller/
│           └── (covered by security IT)
│
├── subscription/
│   ├── application/service/
│   │   ├── SubscriptionServiceUT.java                                ✓ existing (4 tests)
│   │   ├── SubscriptionServiceTest.java                              ✓ existing — DELETE (duplicate, wrong naming)
│   │   └── UpdateSubscriptionServiceUT.java                          ✓ existing (5 tests)
│   └── infrastructure/adapters/
│       ├── out/
│       │   └── SubscriptionRepositoryIT.java                         ← Sprint B
│       └── in/rest/controller/
│           └── SubscriptionControllerIT.java                         ← Sprint C
│
├── reservation/
│   ├── application/service/
│   │   ├── CreateReservationServiceUT.java                           ← Sprint A
│   │   ├── UploadReceiptServiceUT.java                               ← Sprint A
│   │   └── ValidateReservationServiceUT.java                         ← Sprint A
│   ├── domain/service/
│   │   └── ReservationPricingServiceUT.java                          ← Sprint A
│   └── infrastructure/adapters/
│       ├── out/
│       │   └── ReservationRepositoryIT.java                          ← Sprint B
│       └── in/rest/
│           └── ReservationControllerIT.java                          ← Sprint C
│
├── order/
│   ├── application/service/
│   │   ├── GetOrderServiceUT.java                                    ← Sprint A
│   │   └── CreateOrderServiceUT.java                                 ← Sprint A
│   └── infrastructure/adapters/
│       ├── out/
│       │   └── OrderRepositoryIT.java                                ← Sprint B
│       └── in/rest/controller/
│           └── (covered by reservation controller IT and E2E)
│
├── dashboard/
│   ├── application/service/
│   │   └── (deferred — read-only queries, low priority)
│   └── infrastructure/adapters/
│       └── (deferred)
│
├── security/                                                         ← Sprint D
│   ├── AccountSecurityIT.java
│   ├── ServiceSecurityIT.java
│   ├── ProfileSecurityIT.java
│   ├── ClientSecurityIT.java
│   ├── SubscriptionSecurityIT.java
│   ├── ReservationSecurityIT.java
│   ├── OrderSecurityIT.java
│   └── DashboardSecurityIT.java
│
└── e2e/                                                              ← Sprint E
    ├── AccountCreationE2EIT.java
    ├── ReservationLifecycleE2EIT.java
    ├── SubscriptionLifecycleE2EIT.java
    └── FullSaleFlowE2EIT.java
```

---

## File Count Summary

| Category | Existing | Planned | Total |
|----------|----------|---------|-------|
| Base / utility | 1 | 1 | 2 |
| Smoke IT | 1 | 0 | 1 |
| Security filter IT | 1 | 0 | 1 |
| Unit tests (UT) | 5 (+1 to delete) | 9 | 14 |
| Repository ITs | 0 | 7 | 7 |
| Controller ITs | 0 | 4 | 4 |
| Per-module security ITs | 0 | 8 | 8 |
| E2E ITs | 0 | 4 | 4 |
| **Total** | **8** | **33** | **41 files** |

Note: `SubscriptionServiceTest.java` is counted as existing but marked for deletion. After deletion, existing count drops to 8 (unchanged since `SubscriptionServiceUT.java` already exists as its replacement).
