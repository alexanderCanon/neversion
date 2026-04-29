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
