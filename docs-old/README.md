# Neversion — Documentation

> **Entry point:** Start with [`INDEX.md`](INDEX.md) for the full navigable index with FAQ.
> For a quick map of all folders, see [`GUIDE.md`](GUIDE.md).

---

## Structure

```
docs/
  INDEX.md              ← Navigable book: FAQ + chapter-per-folder
  GUIDE.md              ← Quick folder map for humans and AI agents
  system/               ← Technical specification (architecture, schema, stack, API)
  modules/              ← Domain modules: business rules + REST contracts per entity
  archive/              ← Deprecated content preserved for historical reference
```

```
project/                ← Operational project management (outside docs/)
  sprints/              ← Sprint definitions, scope, roadmap, session summaries
  bugs/                 ← Bug tracking
  vault/                ← Brainstorming and raw notes (Obsidian)
  prompt/               ← AI prompt history (audit trail)
```

---

## Development Philosophy

The project follows **Documentation-First, then Code** — based on DDD Architecture Definition:

1. **Define** the domain and business rules in `docs/modules/`
2. **Design** the API contract in `docs/modules/{module}.md → Endpoints section`
3. **Model** the database in `docs/system/schema.md` (verified against Flyway migrations)
4. **Implement** the backend following the contract exactly
5. **Validate** against Integration Tests

> *"If the contract is wrong, the system is wrong."*

---

## Three Phases of the System

### Phase 1 — Analysis
Defines the What and Why.

→ [`system/overview.md`](system/overview.md) — Business context, domain glossary
→ [`modules/`](modules/) — Use cases, business rules per entity
→ [`project/sprints/`](../project/sprints/) — Sprint scope and objectives

### Phase 2 — Architecture
Defines the How at a high level.

→ [`system/architecture.md`](system/architecture.md) — Hexagonal Architecture, diagrams, auth flow
→ [`system/schema.md`](system/schema.md) — PostgreSQL schema, Flyway migrations V1–V5
→ [`system/stack.md`](system/stack.md) — Full tech stack with versions

### Phase 3 — Engineering
Defines implementation and execution.

→ [`system/api-conventions.md`](system/api-conventions.md) — REST design, versioning, RFC 7807 errors
→ [`modules/{module}.md`](modules/) — REST endpoints and DTO contracts per module
→ [`project/bugs/`](../project/bugs/) — Tracked issues and technical debt