# UI/UX Refactor — Changes Walkthrough

> Automatically generated file during the refactoring session.
> Each section corresponds to a backlog item in `docs/uiux-audit/index.md`.

---

## Bug #1 — Occupied slots not reflected in Accounts module

**Root cause:** `GET /api/v1/accounts` returned the `status` field persisted in the DB (static). The frontend displayed it directly without the context of actual slots.

| File | Change |
|---|---|
| `api/.../account/infrastructure/adapters/in/rest/dto/AccountResponse.java` | Added fields `maxSlots`, `occupiedSlots`, `availableSlots` |
| `api/.../account/infrastructure/adapters/in/rest/mapper/AccountMapper.java` | Injected `ProfileRepositoryPort`; counts `OCCUPIED` slots when mapping |
| `panel/src/app/features/accounts/models/account.model.ts` | New typed fields in `AccountResponse` |
| `panel/src/app/features/accounts/pages/accounts-list/accounts-list.component.html` | Dynamic badge (Available/Partial/No availability) + `X/Y` counter + Spanish translations |

---

## Bug #2 — Subscriptions would not load in the list (Incorrect empty state)

**Root cause:** When the frontend requested `GET /api/v1/subscriptions` without sending a filter (normal behavior on initial load), the endpoint returned `List.of()` instead of listing all subscriptions, thus hiding the entered data.

| File | Change |
|---|---|
| `api/.../subscription/domain/port/out/SubscriptionRepositoryPort.java` | Added `findAll()` method |
| `api/.../subscription/infrastructure/adapters/out/JpaSubscriptionAdapter.java` | Implemented `findAll()` executing proxy to the database |
| `api/.../subscription/infrastructure/adapters/in/rest/controller/SubscriptionGetController.java` | Modified empty `else` to call `findAll()` |

---

## UI #3 and UX #5 — Translation, Primary Color Centralization and Flash Loading Fix

**Context:** Users, Subscriptions, and Products modules were in "Spanglish", had different "primary" button colors (`success`, `info`, `primary`), and the "Loading..." state was never properly triggered before executing HTTP requests, causing a "No data" flash for a few microseconds.

| File | Change |
|---|---|
| `panel/src/styles.scss` | Design System centralization (OKLCH and semantic variables). Overrode `.btn-primary` to use the primary oklch. Base typography `Inter`. |
| `panel/src/app/features/*/pages/.../*.component.html` | Spanish translation (Users, Subscriptions, Products). Migrated to Angular 17 control flow validation (`@if`, `@for`). |
| `panel/src/app/features/*/pages/.../*.component.ts` | Refactored alerts, toast notifications for deletions, cancellations and translated switch/enums in helper methods. |
| `panel/src/app/features/*/services/*.service.ts` | Used pipe with `finalize()` operator to set the `isLoading` signal to false at the end of the request, preventing flickers in empty states. |

---

## UI #6 and UX #8 — Global Sentence Case and Currency/Phone format (+502 / Q)

**Context:** There were many titles and tables in "ALL CAPS" (e.g., "ADMINISTRACIÓN DE PRODUCTOS") which affected the visual hierarchy of the Notion UI. Also, the strict prefix for the Quetzal currency (GTQ) had to be properly formatted and phone logic adjusted with +502.

| File | Change |
|---|---|
| Multiple `.html` and `.ts` | Refactored all tables (`th`) with `text-uppercase` classes and hardcoded raw text to use "Sentence case". |
| `panel/src/app/shared/pipes/phone.pipe.ts` | Created a global Angular pipe to clean up and inject the `+502` prefix by default to all phone numbers printed in any module. Imported in Customers List and Dashboard Slots. |
| Multiple `.html` | Refactored dollar symbols `$` to fixedly point to `Q` under string format or using the local `number` pipe to `1.2-2`. |

---

## UX #7 and UI #9 — Logical filters and Visual Stepper

**Context:** The toolbar inputs in "Products" were sterile (did not work). Furthermore, creating new products depended on a very clunky two-step modal, which did not visually report which phase it was in.

| File | Change |
|---|---|
| `panel/src/app/features/products/products.component.ts` | Refactored the static `products` property to an Angular Signal. Added localized `filteredProducts` helper via a `computed()` evaluating signals tied to the HTML search bar's Two-Way data binding. |
| `panel/src/app/features/products/products.component.html` | Connected `[(ngModel)]` of the "Bar inputs" to logic. Injected a controlled alert if no matches were found + a button to un-collapse the filter. |
| `panel/src/app/features/products/components/new-product-modal/new-product-modal.component.html` | Implemented a dynamic Wizard Header with pills and icons to represent the Base -> Inventory flow via Bootstrap 5 class conditionals, aesthetically compliant with the audit. |

---

## UX #10 — Navigable Dashboard metric cards

**Context:** The top dashboard cards (`app-dashboard-metrics`) were static and provided no quick way to jump to corresponding filtered list views.

| File | Change |
|---|---|
| `panel/src/app/features/subscriptions/pages/subscriptions-list/subscriptions-list.component.ts` | Configured `ActivatedRoute` injection to capture and dynamically initialize the `filterStatus` signal based on URL `queryParams`. |
| `panel/src/app/features/accounts/pages/accounts-list/accounts-list.component.ts` | Configured `ActivatedRoute` along with a new `filterType` signal (ALL, AVAILABLE, OCCUPIED) to bind the `computed()` directly to URL queries on initial load and reloads. |
| `panel/src/app/features/dashboard/components/dashboard-metrics/dashboard-metrics.component.ts` | Incorporated `RouterModule`, `[routerLink]` attribute, and `[queryParams]` directives to the cards to convert them into interactive Call-To-Actions (with hover effect). |

---

## UX #11 and UX #12 — Client Editing and Active Subscription Details

**Context:** The `Client` entity did not have a functionality in the UI to modify its data after creation. Likewise, there was no way from the client list to quickly jump to a report of which subscriptions are assigned to the client.

| File | Change |
|---|---|
| Multiple `.java` | Created `UpdateClientUseCase`, service, and backend implementation for the `PUT /api/v1/clients/{id}` endpoint to enable reactivity from the client. Documented in API contracts. |
| `panel/src/app/features/users/components/user-form/...` | Adapted static modal to support a fluid "Edit" mode recycling the same structure with a detection mode (`mode === 'CREATE'` / `mode === 'EDIT'`). |
| `panel/src/app/features/users/services/users.service.ts` | Integrated HTTP PUT method in the users service, injecting it directly to the Signal with on-the-fly updating. |
| `panel/src/app/features/users/pages/users-list/users-list.component.html` | Inserted an edit button and an interactive "View Subscriptions" button that programmatically jumps to `/subscriptions?userGuestId={id}`. |
| `panel/src/app/features/subscriptions/pages/subscriptions-list/subscriptions-list.component.ts` | Extended parsing using `queryParams` from `ActivatedRoute` to capture the `userGuest` id, connecting the search to the subscriptions service via `filteredSubscriptions`. |

---
---

## "Low" Priorities (Angular) - UX #13, #15, #16, #17

**Context:** A set of minor UI/UX improvements were requested in global Dashboard perspectives. It was required to omit any setup with S3 components or Backend refactors for these changes.

| File | Change |
|---|---|
| `panel/src/index.html`<br>`main-layout.component.*` | [UX #13] Added a toggle button for Dark Theme. Implemented a `theme` Signal with validation and immediate rehydration in `<head>` using `localStorage` to avoid flash of unstyled content (FOUC). Replaced `bg-white` utilities with semantic native utilities like `bg-body` in Layout headers. |
| `panel/.../new-product-modal.component.html` | [UX #15] Standardized Modal headers adopting a solid primary color (`bg-primary text-white`), unifying the view to the same visual style across the panel. |
| `panel/.../subscription-form.component.*` | [UX #16] Added auto-calculation for Renewal Date. Injected `ProductService` to retrieve the assigned Inventory entity for the selected account. Then its `durationDays` were added to the `purchaseDate` with a reload of the `FormGroup`. |
| `docs/uiux-audit/index.md` | [UX #17] The global unified notification system (Toast system) had already been implemented as part of the general work, consolidating the architecture in past Sprints (injected `ToastService`). |

**PENDING: [UX #14] — Replace URL field with direct S3 upload:** The user requested to keep this topic aside due to backend/S3 integration. This task was skipped so it can be handled manually.

---
