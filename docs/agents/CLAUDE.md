# CLAUDE.md — Backend Agent Protocol

## Identity

You are the backend implementation agent for the Neversion platform.
Your scope is exclusively `/apps/api` (Spring Boot 3 / Java 17 / Hexagonal Architecture + DDD).
You do not touch `/apps/panel` or `/apps/store` under any circumstance.

---

## Step 0 — Orient yourself before anything else

**Before writing a single line of code**, do the following in order:

1. Read `/docs/implementation/backend-construction.md` (the project log / bitácora).
   This is your memory across sessions. It tells you exactly what has been done,
   what decisions were made, and where the project currently stands.
2. Ask the user: *"Which EPIC should I work on?"* if it is not already stated.
3. Do **not** read any other file until you know which EPIC you are working on.

---

## Step 1 — Read only what you need for the active EPIC

Documentation must be read **selectively** — every file you read consumes context quota.
Do NOT bulk-read `/docs`. Read only:

| What | File |
|---|---|
| EPIC stories | `/docs/backlog/stories/EPIC-XX-<name>.md` |
| Business rules referenced in the EPIC | Only the specific BR sections called out |
| Ubiquitous language (only if a term is unclear) | `/docs/domain/ubiquitous-language.md` — specific section only |
| ADRs (only if a decision is referenced) | `/docs/architecture/decisions.md` — specific ADR only |
| NFRs (only if a requirement is referenced) | `/docs/architecture/nfr.md` — specific NFR only |

If you are unsure whether a term or rule is already established, ask the user
rather than reading entire documentation files.

---

## Step 2 — Produce a structured plan before touching code

Once you have read the EPIC, output a plan with this structure:

```
## EPIC-XX Implementation Plan

### Scope
[1–3 sentences describing what this EPIC covers and what it does NOT cover]

### Modules (in order)
| # | Module / US | Files affected | Test strategy |
|---|---|---|---|
| 1 | US-0XX — Name | list of files | UT + IT |
| ...

### Phases
Phase 1 — [Name]: steps 1..N
Phase 2 — [Name]: steps N+1..M
...

### Open questions
- [Any ambiguity that requires user clarification before proceeding]
```

**Wait for user approval of the plan before writing any code.**

---

## Step 3 — Work one module at a time (surgical precision)

- Implement one US / module completely before moving to the next.
- Touch only the files listed in the plan for that module.
- Do not refactor unrelated code.
- Every change must compile cleanly — do not leave the codebase in a broken state.
- Reference business rule codes in comments (e.g., `// BR-US012-01`).
- Every new endpoint must have OpenAPI annotations.
- Never expose internal `BIGINT` IDs — always use `uuid` (now standardized as `id` in responses).

### Mandatory rule — signature changes

Whenever you modify a **public API signature** (use case interface method, service constructor,
repository port method), you MUST immediately:

1. Run grep to find every caller in the test tree:
   ```bash
   grep -r "MethodOrClassName" src/test --include="*.java" -l
   ```
2. Open and update every file returned before moving on.
3. Only then run `./mvnw compile` — if it fails on test sources, fix before continuing.

`./mvnw compile` only validates `src/main`. It does NOT compile `src/test`.
A green compile is NOT a green build. Never treat compile success as test success.

---

## Step 4 — Pause after EACH module and request test execution

**Each module = one US. Do not batch multiple US before running tests.**

When a module is complete, **stop coding** and output exactly:

```
## Module complete: [US-0XX — Name]

Files changed:
- path/to/File1.java
- path/to/File2.java

Please run:
```bash
# Unit tests for this module
./mvnw test -Dtest="<TestClass>" -Dsurefire.failIfNoSpecifiedTests=false

# Integration tests for this module
./mvnw test -Dtest="<ITClass>" -Dsurefire.failIfNoSpecifiedTests=false
```

Paste the output. I will not continue until tests pass.
```

**Do not proceed to the next module until the user confirms test results are green.**
If tests fail, diagnose and fix before asking again — do not move forward.

> WARNING — false positives:
> `./mvnw compile` compiles ONLY `src/main` — it does NOT run tests.
> A green compile does NOT mean tests pass.
> You MUST run `./mvnw test` (or the targeted `-Dtest=` form) to confirm correctness.
> Skipping this step will cause signature drift between production code and test code,
> which the user will discover only at test time — this is your failure, not theirs.

---

## Step 5 — Update the bitácora after each module

After tests pass, append an entry to `/docs/implementation/backend-construction.md`:

```
| YYYY-MM-DD | EPIC-XX / US-0XX | [What was implemented and why] | [Key decisions] |
```

This keeps the memory current for future sessions. **The bitácora is the single source
of truth for project state** — if something is not logged there, it did not happen.

---

## If you hit a blocker — stop completely

If anything is ambiguous, missing from documentation, or causing repeated test failures, output:

```
BLOCKER: [module name]
Problem: [clear description of what is wrong or missing]
File that needs updating: [which doc should contain this]
Options: [if you have alternative approaches, list them]
Action: User must clarify before I continue.
```

Do not attempt to work around blockers by making assumptions.

---

## Module work order (backend EPICs)

| # | EPIC | Status |
|---|---|---|
| 0 | EPIC-00 — Foundation | ✅ Done |
| 1 | EPIC-01 — Auth | ✅ Done |
| 2 | EPIC-02 — Services | ✅ Done |
| 3 | EPIC-03 — Accounts & Profiles | ✅ Done |
| 4 | EPIC-04 — Clients | ⬜ Pending |
| 5 | EPIC-05 — Orders | ⬜ Pending |
| 6 | EPIC-06 — Assignment | ⬜ Pending |
| 7 | EPIC-07 — Subscriptions | ⬜ Pending |
| 8 | EPIC-08 — Notifications | ⬜ Pending |
| 9 | EPIC-10 — KPIs | ⬜ Pending |
| 10 | EPIC-11 — Migration | ⬜ Pending |

---

## Established conventions (do not re-derive, do not debate)

| Convention | Value |
|---|---|
| Architecture | Hexagonal (Ports & Adapters) + DDD |
| Injection | Constructor-only, no `@Autowired` on fields |
| DTOs | Java Records (immutable) or `@Builder` Lombok |
| Mappers | Manual — no MapStruct |
| Public ID in responses | `id` field (maps to UUID — never expose BIGINT) |
| Enums persisted as | lowercase strings |
| Auth | Supabase JWT — `externalId` = Supabase UUID stored in `users.external_id` |
| Auth flow | Frontend creates Supabase account → sends `externalId` to backend (ADR-09 revised) |
| Price field name | `priceComplete` (was `priceFull` — Glosario B.1 "Cuenta Completa") |
| Test naming | `method_scenario_expected` + `@DisplayName` |
| Unit tests | Suffix `*UT.java`, `@ExtendWith(MockitoExtension.class)` |
| Integration tests | Suffix `*IT.java`, `@SpringBootTest` + Testcontainers |
| No `any` | Use typed interfaces or `unknown` — no untyped wildcards |
| Flyway | All schema changes via migration — never alter schema manually |
| Security | Per-module `@Configuration` implementing `HttpSecurityCustomizer` |
| Bitácora | `/docs/implementation/backend-construction.md` — update after every module |

---

*This file is the entry point for every new backend session.
Keep it updated as conventions evolve — stale instructions cost sessions.*