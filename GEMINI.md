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
- **Build:** `npm run build`

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

## 🔄 Protocolo de Sincronización API
Cuando el backend sufra cambios en sus contratos (Swagger/OpenAPI):
1. **Sincronizar:** Ejecutar `pnpm run api:sync` (opcionalmente pasando `API_URL`).
2. **Validar:** Revisar errores de compilación en `apps/panel` y `apps/store` derivados de cambios en los modelos de `@neversion/api-client`.
3. **Refactorizar:** Actualizar componentes y servicios para consumir los nuevos contratos.

## 📓 Session & Task Logging
In every session, for each task, a **Short-Term Memory Log (Bitácora)** must be maintained to ensure a clean and traceable workflow:
- **Objective:** Main goal of the current task.
- **Changes:** Summary of modified files and logic.
- **Pending:** Tasks left for the next turn or session.
- **Backend Needs:** Any API changes required to support the frontend.
- **Identity Provider:** Supabase Auth.
- **Resource Server:** Spring Boot validates JWT signature via Supabase JWKS.
- **Roles:** Permissions are managed via JWT claims (`app_metadata`).
