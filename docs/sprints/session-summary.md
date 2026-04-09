# Session Summary — Dashboard Design & Documentation

**Date:** 2026-03-24
**Scope:** Master Dashboard design, domain documentation, API contracts, business rules, and Claude Code prompt.

---

## What we built

A complete `docs/` folder serving as the source of truth for the project,
plus a ready-to-use Claude Code prompt for the dashboard implementation.

```
docs/
├── domain.md
├── enums.md
├── business-rules.md
└── api/
    ├── dashboard.md
    ├── subscriptions.md
    ├── accounts.md
    ├── profiles.md
    ├── products.md
    ├── inventory.md
    ├── reservations.md
    ├── orders.md
    └── clients.md
```

---

## Key decisions made

### Domain & Architecture
- Dashboard is a **read-only projection** — no new domain entities needed
- Hierarchy confirmed: `Product → Account → Profile → Subscription → UserGuest`
- `Inventory` is the sellable variant of a Product (price, duration, account type)
- `Profile` (Sprint 2) is a Supabase Auth mirror with purchase history and roles — not in scope for Sprint 1

### Dashboard design
- Entry screen groups by **Product** (not Account) filtered by `category=STREAMING`
- Three-level navigation: Product list → Account list → Slot detail (lazy loaded on expand)
- Three dedicated endpoints replace the old `GET /api/v1/subscriptions/dashboard`
- Old endpoint marked `@Deprecated`, scheduled for removal **2026-04-30**

### Enums standardization
- All enum values in `UPPER_SNAKE_CASE` at the API level
- Frontend maps enum values to localized Spanish display labels independently
- `FAMILIAR` → `FAMILY`, `SUSCRIP4U` → `DIGITAL_SERVICE`, `GIFTCARD` → `GIFT_CARD`
- Two calculated-only values (never persisted): `EXPIRING_SOON` and `AccountAvailability`
- **Governance rule:** update `enums.md` first, then update API contracts

### Business rules highlights
- `INDIVIDUAL` accounts: max 1 active subscription at a time (BR-03)
- `BLOCKED` slots require explicit cancellation before reassignment (BR-04)
- Slot count is always auto-generated from `Inventory.maxProfiles` — never overridden (BR-01)
- Stock is referential only — admin manages it manually (BR-17)
- Subscription renewal always creates a new record — old records are immutable history (BR-09)
- Discount on reservations is applied manually by the admin — no automatic logic (BR-16)

### Documentation governance
- **Analysis:** business rules, use cases, user stories
- **Architecture:** system design, infrastructure, API contracts
- **Engineering:** stack, patterns, complex logic
- Docs are the source of truth — Claude Code prompts reference docs, not repeat them

### Frontend conventions confirmed
- Angular 17 standalone components, no NgModule
- Signals for all state (`signal`, `computed`, `effect`) — no BehaviorSubject
- `inject()` for dependencies — no constructor injection
- Smart/Dumb component pattern (pages manage state, components receive `@Input`)
- Bootstrap 5 + project's existing SCSS variables — no new colors introduced
- All user-facing text in Spanish, currency in GTQ

---

## What the Claude Code prompt covers
File: `prompt-dashboard.md`

- Backend: 3 new endpoints in hexagonal architecture + deprecation annotation
- Frontend: full refactor of `src/app/features/dashboard/`
- Deletes `dashboard.service.ts`, rewrites `master-dashboard.service.ts`
- Includes exact TypeScript models, file structure, and Definition of Done checklist
- References `docs/` as source of truth — no repeated context in the prompt

---

## Pending / future considerations
- Enum migration in backend (old values still present in codebase)
- `Profile` entity design — deferred to Sprint 2 (storefront)
- Server-side pagination — deferred to Sprint 2
- `GET /api/v1/subscriptions/dashboard` removal after **2026-04-30**
- Dashboard filter bar (status, search, availability toggle) — identified but
  descoped from the initial prompt to keep the first implementation focused
