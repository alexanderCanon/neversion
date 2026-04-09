# Frontend Roadmap - Sprint 1.5 (UI/UX & Contracts)

> **Focus:** Angular 17 Standalone, Signals, Bootstrap 5, RxJS Interceptors.

## Objective
Immersively adapt the Admin Panel (`panel`) and ensure contract compatibility in the Storefront (`store`) towards the new central Spring Boot structure, successfully assimilating the new corporate terms (Profiles, Clients, Services).

---

## Implementation Phases

### Phase 1: Alignment of Contracts and Models (TypeScript)
1. **DTO Review:** Align with the new Request/Response JSONs coming from the backend.
   * `AccountSlot` -> `Profile`
   * `Product/Inventory` -> `Service`
   * `UserGuest` -> `Client`
2. **HTTP Routes:** Ensure that services point to the new API endpoints (e.g., from `/api/account-slots` to `/api/profiles`).

### Phase 2: State and Signals Refactoring
1. **Smart Components:** Modify local `Signal` based state that consumes previous interfaces. For example, when injecting a list of "Slots" to a table, the underlying logic must be renamed to process arrays of `Profile`.
2. **Reactive Forms:** Adjust the form control to send the fields demanded by the new Database (e.g., now it is mandatory to send `is_owner` in a profile, or `phone` in a client).

### Phase 3: UI / Visual Components (Bootstrap 5)
1. **Dumb Components:** Visually change any mention of inventory for the administrator.
   * Visual Labels: Change titles and tabs of the Dashboard sidebar (e.g., "Slot Management" to "Profile Management").
   * Grids / Tables: Explicitly reflect the exposed data in HTML (now "Services" has the `max_profiles` field).
2. **Modal Forms:** Ensure compatibility and UX in screens for subscription assignments with the new nomenclature.

---
**Reading Restrictions:** The assigned agent MUST ONLY focus on reading the API contracts (when they exist/are modified in backend) and rules that directly affect the UI (forms, synchronous or reactive validations). Any direct DB handling must be ignored.
