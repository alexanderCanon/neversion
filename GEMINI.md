# GEMINI.md - Neversion System Context

This file serves as the primary instructional context for Gemini CLI when working on the Neversion project. It defines the permanent engineering standards and operational protocols.

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
- **Domain Layer:** Pure Java, no framework dependencies.
- **Application Layer:** Orchestrates business logic via Use Case interfaces.
- **Infrastructure Layer:** Framework-specific adapters (REST, JPA, S3).
- **Style:** Constructor-only injection, Java Records for DTOs, Manual mappers.

### Frontend (`panel`) - Angular 17+ Modern Paradigms
- **Standalone Components:** No `NgModule`.
- **Reactive State:** Angular Signals (`signal`, `computed`, `effect`).
- **Dependency Injection:** Mandatory use of `inject()` function.
- **Control Flow:** Prefer `@if`, `@for`, `@defer` over structural directives.

### Frontend (`store`) - Angular 16 Standard
- **Module-based:** Uses `NgModule`.
- **Reactive State:** RxJS Observables with clean `async` pipe usage.
- **UI:** Bootstrap 5 (utility-first).

---

## 💎 Frontend Engineering Pillars (The "Surgical" Code)

### 1. Absolute Type Safety (No `any`)
- The use of `any` is strictly prohibited.
- Use explicit interfaces from `@neversion/models`.
- If a type is truly dynamic or unknown (e.g., API error payloads), use `unknown` combined with **Type Guards**.

### 2. Framework Excellence
- **Panel (A17):** Leverage Signals for local state and `inject()` for cleaner DI.
- **Store (A16):** Ensure clean RxJS streams, avoid manual subscriptions (prefer `async` pipe), and respect the modular structure.
- **Global:** Keep components "Dumb" (UI only) and delegate logic to "Smart" services.

### 3. API-First & Contract Sync
- Never "guess" or manually create API models if a contract exists.
- Always verify that `pnpm run api:sync` has been executed before implementing new features.
- All frontend data mapping must align with the generated `@neversion/api-client`.

---

## 🛡️ Context Isolation & Preciseness

To avoid information overload and maintain surgical precision when working on both apps:

1. **Explicit Switching:** When switching work from `panel` to `store` (or vice versa), state it explicitly in the plan.
2. **Path Mapping:** Always use absolute path aliases (e.g., `@app/core/...`) where configured to avoid relative path confusion.
3. **Surgical Edits:** Apply changes step-by-step. Do not refactor unrelated files.
4. **Validation Reliance:** The human operator is responsible for executing `pnpm lint` and `pnpm build`. The agent must wait for the report of these commands to proceed or fix errors.

---

## ⚖️ Interaction and Planning Protocol

1. **Analysis & Diagnosis:** Explain the error or requirement and its impact.
2. **Proposed Solution:** Technical strategy aligned with the pillars above.
3. **Implementation Plan:** Sequential steps with testing strategy.
4. **Execution:** Surgical application of changes.
5. **Validation:** Wait for human feedback on build/lint status.

---
*Last Updated: 2026-04-25*
