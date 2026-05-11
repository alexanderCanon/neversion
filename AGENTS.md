# AGENTS.md — Global Agent Protocol

This file is the universal entry point for every AI agent operating in this repository — Codex, Jules, Antigravity, OpenCode, and any other tool that reads `AGENTS.md`. Read it entirely before taking any action.

---

## Step 0 — Establish Identity (Mandatory)

Before anything else, read these two files:

```
/.agents/USER.md     → Who is the human you are collaborating with
/.agents/IDENTITY.md → Who you are on this project
```

- The human is **Alex** (Alexander), a Guatemalan developer. He communicates in both **Spanish and English** — respond in whichever language he uses in a given message.
- Your project identity is defined in `IDENTITY.md`. Adopt it.

---

## Step 1 — Read Your Specific Protocol

Detailed protocols live in `docs/agents/`. After reading the two identity files, read the protocol that matches your task:

| Task scope | Protocol file |
|------------|--------------|
| Global governance (any agent) | `docs/agents/AGENTS.md` |
| Backend — `apps/api` (Spring Boot) | `docs/agents/CLAUDE.md` |
| Frontend — `apps/panel` / `apps/store` (Angular) | `docs/agents/GEMINI.md` |

Do not start reading unrelated documentation. Read only the protocol for your scope.

---

## Project Overview

**Neversion** is a multi-tenant SaaS platform for managing and reselling digital services. Vendors manage accounts, profiles, and clients; clients purchase subscriptions through a storefront.

```
apps/
  api/        # Spring Boot 3 / Java 17 — Hexagonal + DDD
  panel/      # Angular 17 — Admin UI (standalone components + Signals)
  store/      # Angular 16 — Client storefront (SSR via NgUniversal)
packages/
  api-client/ # Generated TypeScript Angular services (OpenAPI)
  models/     # Shared TypeScript interfaces
  utils/      # Shared Angular utilities
docs/
  agents/           # Agent protocols (authoritative)
  domain/           # Ubiquitous language, business rules (Spanish)
  architecture/     # ADRs, ER diagram, NFRs
  backlog/          # Epics + per-epic story files
  implementation/   # Bitácoras — cross-session progress logs (critical memory)
```

---

## Core Rules (Non-Negotiable)

### Work Together, Assume Nothing
- You are a collaborator, not an autonomous executor. Never make product, business, or architectural decisions on your own.
- If a requirement is undocumented or ambiguous → stop immediately and report a **BLOCKER** to Alex.
- If you are unsure → ask. Never invent or assume.

### Always Create a Plan First
- Before writing any code, produce a structured implementation plan and present it to Alex.
- Wait for explicit approval before proceeding.
- Follow the plan. Each step is surgical: one module at a time, one US at a time.

### Tests Are Manual Unless Stated Otherwise
- After completing a module, **stop** and ask Alex to run the relevant tests.
- Do not proceed to the next module until Alex confirms tests are green.
- Never treat `./mvnw compile` as a test pass — it only compiles `src/main`, not `src/test`.
- Backend tests that use Mockito inline or Testcontainers must run **outside the sandbox**.
  The local sandbox blocks or restricts Java agent attachment and Docker socket access,
  causing false failures unrelated to application code.
- If a backend test command fails with Mockito `MockMaker`, Byte Buddy agent attachment,
  Docker, or Testcontainers errors, immediately rerun the same command with escalated
  permissions instead of treating the test failure as a code failure.

### Scope Isolation
- Work exclusively within your assigned directory.
- Backend agent: `apps/api` only — never touch `apps/panel` or `apps/store`.
- Frontend agent: `apps/panel` and/or `apps/store` only — never touch `apps/api` or the database.
- `/docs` is read-only **except** for `/docs/implementation/` (bitácoras).

### Surgical Execution
- Every task is scoped to the minimum necessary change.
- Do not refactor unrelated code, add unrequested features, or change files outside the plan.
- Each code change must compile cleanly and not break the codebase state.

### Language Standards
- Code and comments: **English**
- UI labels, placeholders, user-facing messages: **Spanish**
- Domain terminology: must match `/docs/domain/ubiquitous-language.md` exactly

### Update the Bitácoras
After every completed module, append an entry to the relevant progress log:

| Log | Path |
|-----|------|
| Backend | `docs/implementation/backend-construction.md` |
| Panel UI | `docs/implementation/panel-construction.md` |
| Store UI | `docs/implementation/store-construction.md` |

The bitácoras are the single source of truth for project state across sessions. If it is not logged, it did not happen. After every completed EPIC/US, agents must also create or update Mermaid diagrams in `/docs/diagrams/` to maintain visual control.

---

## Blocker Protocol

When you hit missing information, conflicting docs, a technical wall, or anything ambiguous:

```
BLOCKER: [Module Name] — [Scope: API | Panel | Store]
Problem: [Clear description of what is missing or broken]
Reference: [Which documentation file needs updating]
Options: [Alternative approaches if any exist]
Action: Waiting for Alex to clarify before continuing.
```

Do not work around blockers with assumptions.

---

## AI Model Guidance

Suggest the appropriate model to Alex based on the complexity of the task:

| Task type | Suggested model |
|-----------|----------------|
| New module design, complex refactor, architectural decision | Claude Opus 4.7 / GPT-5 (high reasoning) |
| Standard feature implementation, CRUD endpoints, Angular components | Claude Sonnet 4.6 / Gemini 2.5 Pro |
| Boilerplate generation, migrations, simple bug fixes, documentation | Gemini 2.5 Flash / Claude Haiku 4.5 |
| Multi-step planning + long context analysis | Claude Sonnet 4.6 (extended thinking) / Gemini 2.5 Pro |
| Frontend UI with visual iteration | Gemini 2.5 Pro / Claude Sonnet 4.6 |

Include the model suggestion at the start of your implementation plan when relevant, but first ask what model who you are.

---

## Forbidden Commands

Never execute these without explicit instruction from Alex:

- `mvn spring-boot:run`, `pnpm start`, `ng serve`, `docker-compose up`
- `pnpm install -g`, `pip install`, `apt install`
- `psql` (raw DB access), `flyway migrate` (backend agent may use via Maven only)
- `rm -rf`, `DROP TABLE`, `git push --force`, `git reset --hard`

---

## Development Pipeline

```
feature/backend  ──┐
                   ├──> develop ──> main
feature/panel    ──┤
feature/store    ──┘
```

1. Backend agent completes and tests a module in `feature/backend`
2. Alex reviews and merges into `develop`
3. Frontend agents implement the UI in `feature/panel` / `feature/store`
4. Alex does final review and merges into `develop`
5. Repeat for next module

Merges to `develop` and `main` are **Alex's responsibility**, not the agent's.

---

## Quick Command Reference

```bash
# Root
pnpm install            # Install all workspace dependencies
pnpm -r build           # Build all packages
pnpm api:sync           # Regenerate api-client from running backend
make help               # All root Make targets

# Backend (apps/api)
./mvnw test                              # All unit tests
./mvnw test -Dtest=ClassName             # Single test class
./mvnw verify                            # Unit + integration tests (Testcontainers)
make -C apps/api run                     # Run with hot reload

# Panel (apps/panel)
make -C apps/panel start                 # Dev server → http://localhost:4200
make -C apps/panel test                  # All Karma tests

# Store (apps/store)
cd apps/store && pnpm start              # Dev server
cd apps/store && pnpm run build:ssr      # SSR production build
```

---

*Keep this file current as conventions evolve. Stale instructions cost sessions.*


<claude-mem-context>
# Memory Context

# [neversion] recent context, 2026-05-11 12:01pm CST

Legend: 🎯session 🔴bugfix 🟣feature 🔄refactor ✅change 🔵discovery ⚖️decision 🚨security_alert 🔐security_note
Format: ID TIME TYPE TITLE
Fetch details: get_observations([IDs]) | Search: mem-search skill

Stats: 2 obs (1,783t read) | 11,775t work | 85% savings

### Apr 28, 2026
1 2:25p 🔵 Spring Boot Backend Domain Architecture Mapped Across Five Modules
2 5:34p 🔵 Flyway Migration Schema State and Event-Driven Notification Architecture
S1 Plan implementation approach for EPIC-06 "Asignación y Entrega de Accesos" backend module covering order-to-subscription workflow (Apr 28, 5:34 PM)
S2 Complete planning for EPIC-06 backend implementation and prepare to begin execution phase starting with V24 Flyway migration (Apr 28, 5:45 PM)
**Investigated**: Primary session performed exhaustive codebase exploration reading 60+ files including Service.java (durationDays field verification), Vendor.java (email field check), existing subscription services, profile management patterns, order status handling, notification infrastructure, and migration history. Verified current state: V23 latest migration, all EPICs 00-05 complete, hexagonal architecture patterns established.

**Learned**: Service.durationDays (Integer, nullable) exists since V11 - will drive endDate calculation using .plusDays(). Vendor model lacks email field - NO_INVENTORY_ALERT notifications must include vendorId in JSON payload for external agent email resolution. ProfileStatus enum already defines ACTIVE/RESERVED states with manual-setting guards in ProfileService. NotificationLogPort implements fire-and-forget pattern with status='pending' insertion. OrderStatus.VALIDATED is source state for assignment suggestion flow. Existing SubscriptionService provides manual assignment reference pattern with anti-overbooking via existsActiveByProfileId check.

**Completed**: Comprehensive 12-phase EPIC-06 implementation plan written to both ~/.claude/plans/vamos-a-trabajar-en-sprightly-cerf.md and project root EPIC-06-plan.md. Plan covers: (1) V24 migration adding order_id/end_date columns with indexes, (2) Domain enrichment across 11 existing files, (3) New assignment/ module with 4 services (SuggestAssignmentService, ConfirmAssignmentService, DeliverAccessService, ManualAssignmentService), (4) REST layer with 3 endpoints, (5) Complete test strategy with unit and integration tests. Total scope: 26 new files + 11 modified files. Architectural decisions documented: use durationDays not months, DeliverAccessService uses @Transactional(REQUIRES_NEW) for failure isolation, ConfirmAssignmentService bypasses ChangeOrderStatusUseCase for atomic transaction control.

**Next Steps**: Awaiting user approval to exit plan mode and begin implementation. Once confirmed, will start with Phase 1 step [1]: creating V24 Flyway migration file at apps/api/src/main/resources/db/migration/V24__enrich_subscriptions_for_epic06.sql to add order_id and end_date columns to subscriptions table with foreign key constraints and indexes. Implementation will proceed sequentially through 12 phases with manual testing checkpoints between each phase.


Access 12k tokens of past work via get_observations([IDs]) or mem-search skill.
</claude-mem-context>