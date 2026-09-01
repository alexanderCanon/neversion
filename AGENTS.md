# AGENTS.md

## Project Overview

**Neversion** is a multi-tenant SaaS platform for managing and reselling digital services. Vendors manage accounts, profiles, and clients; clients purchase subscriptions through a storefront.

```
apps/
  panel/                # Angular 21 — Admin UI (standalone components + Signals)
  store/                # React 19 + Vite 8 + Bun — Client storefront (SPA)
  api-gateway/          # Cloudflare Worker — Edge API Gateway & Auth router
  notification-worker/  # Cloudflare Worker — Transactional email worker (Resend / Supabase Webhook)
  reservation-service/  # Rust — Service reservation manager (gRPC / SQLite)
  db/                   # PostgreSQL 17 configuration & compose
  monitoring/           # Observability stack (Grafana, Alloy, Prometheus)
  www/                  # Marketing landing site (Astro)
packages/
  models/               # Shared TypeScript interfaces
  utils/                # Shared Angular utilities
docs/
  agents/               # Agent protocols (authoritative)
  domain/               # Ubiquitous language, business rules (Spanish)
  architecture/         # ADRs, ER diagram, NFRs
  backlog/              # Epics + per-epic story files
  implementation/       # Historical implementation logs (read-only for agents)
```

---

## Core Rules (Non-Negotiable)

### Session Start & Git Branch Protocol
- At the start of **every session**, before writing code or making modifications, the agent must:
  1. Verify the current git branch (`git branch --show-current` / `git status`).
  2. If not on a dedicated new feature branch, proactively suggest switching to `main`, pulling the latest changes (`git checkout main && git pull origin main`), and branching off a new descriptive branch (e.g., `feat/<description>`, `fix/<description>`, `chore/<description>`).
  3. **Every Pull Request must have its own isolated, descriptive branch** branched off the updated `main`.

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
- `/docs` is read-only for agents unless Alex explicitly asks for documentation changes.

### Surgical Execution
- Every task is scoped to the minimum necessary change.
- Do not refactor unrelated code, add unrequested features, or change files outside the plan.
- Each code change must compile cleanly and not break the codebase state.

### Language Standards
- Code and comments: **English**
- UI labels, placeholders, user-facing messages: **Spanish**
- Domain terminology: must match `/docs/domain/ubiquitous-language.md` exactly

### Documentation Updates
- Agents must **not** update bitácoras or Mermaid diagrams as part of normal implementation work.
- Treat `docs/implementation/*.md` as historical reference only.
- Only modify documentation when Alex explicitly requests documentation changes.

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

# Backend (apps/api)
cd apps/api && ./mvnw spring-boot:run        # Run with hot reload
cd apps/api && ./mvnw test                   # All unit tests
cd apps/api && ./mvnw test -Dtest=ClassName  # Single test class
cd apps/api && ./mvnw verify                 # Unit + integration tests (Testcontainers)

# Panel (apps/panel)
cd apps/panel && pnpm start                  # Dev server → http://localhost:4200
cd apps/panel && pnpm test                   # All Karma tests

# Store (apps/store)
cd apps/store && bun dev                  # Dev server → http://localhost:4000
cd apps/store && bun build                # SPA production build
```
---

*Keep this file current as conventions evolve. Stale instructions cost sessions.*
