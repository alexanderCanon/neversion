# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**Neversion** is a multi-tenant SaaS platform for managing and reselling digital services (streaming subscriptions, etc.). Vendors manage accounts, profiles, and clients; clients purchase subscriptions through a storefront.

## Monorepo Structure

```
apps/
  api/        # Spring Boot 3 / Java 17 backend (Hexagonal + DDD)
  panel/      # Angular 17 admin UI (standalone components + Signals)
  store/      # Angular 16 client storefront (SSR via NgUniversal)
packages/
  api-client/ # OpenAPI-generated TypeScript Angular services
  models/     # Shared TypeScript interfaces
  utils/      # Shared Angular utilities
docs/
  agents/     # Agent protocols (AGENTS.md, CLAUDE.md, GEMINI.md)
  domain/     # Ubiquitous language, business rules, use cases (Spanish)
  architecture/ # ADRs, ER diagram, NFRs
  backlog/    # Epics + per-epic story files
  implementation/ # Bitácoras (progress logs — critical cross-session memory)
```

## Commands

### Root
```bash
pnpm install          # Install all workspace dependencies
pnpm -r build         # Build all packages
pnpm api:sync         # Regenerate api-client from running backend (port 8080)
make help             # Show all root Make targets
make up-local         # Start full local stack (API + DB + Panel)
make down-local       # Stop local stack
```

### Backend (`apps/api`)
```bash
make -C apps/api run          # Run with Spring Boot dev tools (hot reload)
make -C apps/api dev-up       # Start PostgreSQL + API via Docker Compose
make -C apps/api test         # Run all unit tests
make -C apps/api verify       # Run unit + integration tests (Testcontainers)

# Via Maven wrapper (from apps/api/)
./mvnw test                                            # All unit tests
./mvnw test -Dtest=CreateAccountServiceUT              # Single test class
./mvnw test -Dtest=CreateAccountServiceUT#methodName   # Single test method
./mvnw verify                                          # Unit + integration tests
```

> **Critical:** `./mvnw compile` only validates `src/main` — it does NOT compile or run `src/test`. A green compile does not mean tests pass. Always run `./mvnw test` to verify.

### Panel (`apps/panel`)
```bash
make -C apps/panel start      # Dev server at http://localhost:4200
make -C apps/panel test       # Run all Karma tests
make -C apps/panel build      # Production build

# Single spec file
make -C apps/panel test-file SPEC=auth.service.spec.ts
```

### Store (`apps/store`)
```bash
cd apps/store
pnpm start            # Dev server
pnpm run build:ssr    # SSR production build
pnpm test             # Karma tests
```

## Architecture

### Backend — Hexagonal (Ports & Adapters) + DDD

Each feature module follows this layout under `apps/api/src/main/java/com/neversion/api/<module>/`:

```
domain/
  model/        # Immutable entities (records or @Builder Lombok)
  service/      # Domain services (pure business logic, no persistence)
application/
  port/         # Use case interfaces + repository port interfaces
  service/      # Application service implementations
infrastructure/
  adapters/in/  # REST controllers (input adapters)
  adapters/out/ # JPA repositories (output adapters)
  config/       # Module-specific @Configuration (implements HttpSecurityCustomizer)
  mapper/       # Request/response DTOs + mappers (manual, no MapStruct)
```

**Key constraints:**
- Constructor-only DI — no `@Autowired` on fields
- DTOs are Java records or use `@Builder`
- Public-facing IDs are always UUID (field named `id`) — never expose BIGINT PKs
- Enums are persisted as lowercase strings
- All schema changes via Flyway migrations in `src/main/resources/db/migration`
- Every endpoint requires OpenAPI `@Operation`/`@ApiResponse` annotations
- Business rule references in comments: `// BR-US012-01`
- Security: per-module `@Configuration` class implementing `HttpSecurityCustomizer`

**Multi-tenancy:** Every core entity carries `vendor_id`. All repository queries filter by `vendor_id`. 403 is returned when the caller's vendor ≠ the resource's vendor.

**Auth:** Supabase JWT. Frontend creates the Supabase account and sends `externalId` to the backend. `externalId` (Supabase UUID) is stored in `users.external_id`. Backend validates the JWT via Spring Security OAuth2 Resource Server.

**Test conventions:**
- Unit tests: `*UT.java` suffix, `@ExtendWith(MockitoExtension.class)`, naming `method_scenario_expected`
- Integration tests: `*IT.java` suffix, `@SpringBootTest` + Testcontainers (PostgreSQL)

### Frontend — Angular (panel + store)

- **Panel (Angular 17):** Standalone components, Signals + RxJS for state, Reactive Forms, Bootstrap 5
- **Store (Angular 16):** Module-based, SSR via NgUniversal, Bootstrap 5
- Structure per app: `core/` (singleton services, guards, interceptors), `features/` (feature slices), `shared/` (reusable components)
- HTTP calls go through the generated `@neversion/api-client` package — never write raw `HttpClient` calls against the API
- UI labels in Spanish; code and comments in English

### API Client Regeneration

When backend endpoints change, regenerate the TypeScript client:
```bash
pnpm api:sync   # requires API running on http://localhost:8080
```

## Critical Documentation

Before working on any feature, read these files:

| File | Purpose |
|------|---------|
| `docs/implementation/backend-construction.md` | Backend progress log — the authoritative record of what is done |
| `docs/implementation/panel-construction.md` | Panel UI progress log |
| `docs/implementation/store-construction.md` | Store UI progress log |
| `docs/backlog/stories/EPIC-XX-<name>.md` | User stories for the active EPIC |
| `docs/domain/ubiquitous-language.md` | Canonical Spanish/English business terminology |
| `docs/architecture/decisions.md` | ADRs 1–10 |

**The bitácoras are cross-session memory.** Update them after every completed module. At the end of an EPIC/US, also create or update Mermaid diagrams in `/docs/diagrams/` for visual tracking.

## EPIC Status

| EPIC | Domain | Status |
|------|--------|--------|
| EPIC-00 | Foundation | ✅ Done |
| EPIC-01 | Auth | ✅ Done |
| EPIC-02 | Services | ✅ Done |
| EPIC-03 | Accounts & Profiles | ✅ Done |
| EPIC-04 | Clients | ✅ Done |
| EPIC-05 | Orders/Checkout | ⬜ In progress |
| EPIC-06 | Assignment | ⬜ Pending |
| EPIC-07 | Subscriptions | ⬜ Pending |
| EPIC-08 | Notifications | ⬜ Pending |
| EPIC-10 | KPIs | ⬜ Pending |
| EPIC-11 | Migration | ⬜ Pending |

## Agent Protocols

Full per-agent protocols live in `docs/agents/`:
- `docs/agents/AGENTS.md` — global rules for all agents
- `docs/agents/CLAUDE.md` — backend agent protocol (scope: `apps/api` only)
- `docs/agents/GEMINI.md` — frontend agent protocol (scope: `apps/panel`, `apps/store`)

**Isolation rule:** Backend agent never modifies frontend; frontend agent never touches backend or the database directly.
