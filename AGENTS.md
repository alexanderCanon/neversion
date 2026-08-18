# AGENTS.md

## Project Overview

**Neversion** is a multi-tenant SaaS platform for managing and reselling digital services. Vendors manage accounts, profiles, and clients; clients purchase subscriptions through a storefront.

```
apps/
  panel/      # Angular 17 — Admin UI (standalone components + Signals)
  store/      # Angular 17 — Client storefront (SPA)
packages/
  api-client/ # Generated TypeScript Angular services (OpenAPI)
  models/     # Shared TypeScript interfaces
  utils/      # Shared Angular utilities
docs/
  agents/           # Agent protocols (authoritative)
  domain/           # Ubiquitous language, business rules (Spanish)
  architecture/     # ADRs, ER diagram, NFRs
  backlog/          # Epics + per-epic story files
  implementation/   # Historical implementation logs (read-only for agents)
```

---

## Core Rules

### Always make questions to work together
- You are a collaborator, not an autonomous executor. Never make product, business, or architectural decisions on your own.
- If a requirement is undocumented or ambiguous → stop immediately and report a **BLOCKER**
- If you are unsure → ask. Never invent or assume.

### Always Create a Plan First
- Before writing any code, produce a structured implementation plan and present it to user
- Wait for explicit approval before proceeding.
- Follow the plan. Each step is surgical: one module at a time, one US at a time.

### Surgical Execution
- Every task is scoped to the minimum necessary change.
- Do not refactor unrelated code, add unrequested features, or change files outside the plan.
- Each code change must compile cleanly and not break the codebase state.

### Language Standards
- Code and comments: **English**
- UI labels, placeholders, user-facing messages: **Spanish**

### Documentation Updates
- Agents must **not** update bitácoras or Mermaid diagrams as part of normal implementation work.
- Treat `docs/implementation/*.md` as historical reference only.

---

## Blocker Protocol

When you hit missing information, conflicting docs, a technical wall, or anything ambiguous:

```
BLOCKER: [Module Name] — [Scope: API | Panel | Store]
Problem: [Clear description of what is missing or broken]
Reference: [Which documentation file needs updating]
Options: [Alternative approaches if any exist]
Action: Waiting for user to clarify before continuing.
```

Do not work around blockers with assumptions.

---

## Forbidden Commands

Never execute these without explicit instruction from Alex:

- `mvn spring-boot:run`, `pnpm start`, `ng serve`, `docker-compose up`
- `pnpm install -g`, `pip install`, `apt install`
- `psql` (raw DB access), `flyway migrate` (backend agent may use via Maven only)
- `rm -rf`, `DROP TABLE`, `git push --force`, `git reset --hard`

---

## Quick Command Reference

```bash
# Panel (apps/panel)
cd apps/panel && pnpm start                  # Dev server → http://localhost:4200

# Store (apps/store)
cd apps/store && pnpm start                  # Dev server
cd apps/store && pnpm build                  # SPA production build
```
---

*Keep this file current as conventions evolve. Stale instructions cost sessions.*
