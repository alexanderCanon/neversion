# GEMINI.md - Neversion System Context

This file serves as the primary instructional context for Gemini CLI when working on the Neversion project.

## 🚀 Project Overview
Neversion is a decoupled, API-driven system designed for managing accounts, subscriptions, and orders. It consists of a Java/Spring Boot backend and two Angular-based frontends.

- **Backend (`api`):** Spring Boot 3 REST API (Java 17).
- **Admin Panel (`panel`):** Angular 17 Administrative Dashboard.
- **Storefront (`store`):** Angular 16 Customer-facing site.

---

## 🏗️ Architecture & Tech Stack

### System Architecture
The system follows a decoupled architecture where frontends consume a secure RESTful API.
- **Auth:** Supabase Auth (OAuth2 / JWT).
- **Database:** PostgreSQL (Supabase) with Flyway migrations.
- **Storage:** AWS S3 for receipt images.

### Backend (`api`) - Hexagonal Architecture & DDD
Strict adherence to **Hexagonal Architecture (Ports & Adapters)** and **Domain-Driven Design (DDD)**.
- **Domain Layer:** Pure Java, no framework dependencies. Aggregates, Entities, Value Objects.
- **Application Layer:** Orchestrates business logic via Use Case interfaces (Inbound Ports).
- **Infrastructure Layer:** Framework-specific adapters (REST Controllers, JPA Repositories, S3).
- **Style:** Constructor-only injection (no `@Autowired` on fields), Java Records for DTOs, Manual mappers (no MapStruct).

### Frontend (`panel`) - Angular 17 Modern Paradigms
- **Standalone Components:** No `NgModule`.
- **Reactive State:** Angular Signals (signal, computed, effect) for state management.
- **UI:** Bootstrap 5 (utility-first), SCSS (OKLCH variables).
- **Patterns:** Feature-based architecture, Smart/Dumb components, Reactive Forms.

---

## 📁 Project Structure

```
neversion/
├── api/                # Spring Boot 3 Backend
│   ├── src/main/java/  # Java Source (Hexagonal Layers)
│   └── src/test/       # JUnit 5 / Mockito / Testcontainers
├── panel/              # Angular 17 Admin Panel (pnpm)
├── store/              # Angular 16 Storefront (npm)
├── docs/               # Source of Truth Documentation
│   ├── api-contracts/  # OpenAPI and REST mappings
│   ├── modules/        # Business rules and Use Cases
│   └── system-architecture.md
└── artifacts/          # Diagrams and design assets
```

---

## 🛠 Building and Running

### Backend (`api`)
Requires Java 17.
- **Run:** `./mvnw spring-boot:run`
- **Build:** `./mvnw clean package`
- **Test:** `./mvnw test` (Uses Testcontainers for Integration Tests)

### Admin Panel (`panel`)
Uses `pnpm`.
- **Install:** `pnpm install`
- **Run:** `pnpm start`
- **Build:** `pnpm run build`
- **Test:** `pnpm test`

### Storefront (`store`)
Uses `npm`.
- **Install:** `npm install`
- **Run:** `npm start`
- **Build:** `npm build`

---

## 📜 Development Guidelines

### Documentation Precedence
The `docs/` directory is the **absolute source of truth**.
- **Phase 1 (Analysis):** `docs/business-context.md`, `docs/modules/`
- **Phase 2 (Architecture):** `docs/system-architecture.md`, `docs/schema/`
- **Phase 3 (Engineering):** `docs/api-contracts/`, `docs/bugs/`

### Backend Coding Style (`api/AGENTS.md`)
- **Package Naming:** `com.neversion.api.<feature>.<layer>`
- **Interfaces:** `<Name>Port`, `<Name>UseCase`.
- **Mappers:** Separate `RequestMapper`, `ResponseMapper`, `EntityMapper`.
- **Soft Delete:** Enabled via `@SQLDelete` and `@SQLRestriction`.

## 🤖 Agent Specialization (Senior Angular Developer)
When acting as the senior engineer, the focus is strictly on **Angular (Frontend)** within `apps/panel/**` and `apps/store/**`. 
- **Frameworks:** Angular 17+ (Panel) and Angular 16 (Store).
- **Style:** Standalone components, Signals, Reactive Forms, and clean RxJS patterns.
- **Backend:** Explicitly state requirements/contracts but **DO NOT** modify code in `api/` unless authorized.

## ⚖️ Interaction and Planning Protocol (Research First)
For any requested task (bug report, new feature, or refactoring), the following order MUST be followed:
1. **Analysis & Diagnosis:** Before any change, explain the identified error, its root cause, and its impact on the system.
2. **Proposed Solution:** Detail the technical strategy to resolve it.
3. **Implementation Plan:** List the specific steps to be taken.
4. **Git Branching:** Create a dedicated branch for the task before making any file modifications (e.g., `git checkout -b feat/task-name`).
5. **Confirmation:** Wait for user approval before proceeding with execution (Act), unless the user has given a direct and explicit execution instruction like "execute" or "fix now".
6. **Validation & Commit:** After implementation, verify the changes (build/test). If successful, commit the changes with a descriptive message.

*Note:* Even if "Plan Mode" is not explicitly activated, this Analysis -> Plan process remains mandatory via text before modifying code.

## 🔄 API Synchronization Protocol
When the backend undergoes changes in its contracts (Swagger/OpenAPI):
1. **Sync:** Run `pnpm run api:sync` (optionally passing `API_URL`).
2. **Validate:** Review compilation errors in `apps/panel` and `apps/store` resulting from changes in `@neversion/api-client` models.
3. **Refactor:** Update components and services to consume the new contracts.

## 📓 Session & Task Logging (2026-04-21)
- **Objective:** Fix panel build errors and sync with new API contracts generated via OpenAPI.
- **Changes:**
  - Configured `tsconfig.json` and `pnpm-workspace.yaml` for workspace package resolution.
  - Resolved `tslib` and `@angular/core` dependencies for `@neversion/api-client`.
  - Implemented Data Mappers in `AccountsService`, `ClientsService`, `ReservationsService`, and `ServicesDataService`.
  - Refactored `SubscriptionFormComponent` to align with the new `CreateSubscriptionRequest` (removed `price`, added `accountId`).
  - Fixed `OrdersListComponent` and `ReservationsListComponent` template type errors (undefined properties).
- **Pending:**
  - Verify if remaining features (Dashboard, Profiles) need deeper contract synchronization.
- **Backend Needs:**
  - The API currently lacks the `price` field in `CreateSubscriptionRequest`; verify if this is intentional or if it should be handled differently.
- **Status:** Panel build is successful (`exit 0`).

---
*End of Session Log*
