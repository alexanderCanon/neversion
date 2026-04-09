# Neversion — Project Memory

> to avoid repeating context. Keep it updated as the project evolves.

---

## Who I am
My name is Alexander Canon, I'm from Guatemala and I'm a backend developer. Solo developer and business owner.
Building this as both a real business tool and a portfolio project.

My native Language is Spanish, but I can communicate in English.
All the project must be strictly in English. In some case I mix these languages.

## What I'm building
**Neversion** — e-commerce platform for reselling digital streaming service credentials.
Monorepo structure:
- `api/` — Spring Boot 3, Java 17, Hexagonal Architecture (Ports & Adapters)
- `panel/` — Angular 17 admin panel (standalone components, Signals, Bootstrap 5)
- `store/` — Angular 16 customer storefront (Sprint 2, SSR)
- `docs/` — Full project documentation (business rules, domain, API contracts, audits)

## Current sprint
**Sprint 1 — Admin Panel (backoffice).** No customer storefront yet.
Goal: digitize the admin's manual operations (previously managed in Excel/Notion/WhatsApp).

Admin workflow:
1. Create product → create inventory variant
2. Buy account from provider → register in system (slots auto-generated)
3. Register guest client
4. Create subscription (link client to account slot)
5. Deliver credentials manually via WhatsApp

## Key domain terminology
| Term | Meaning |
|---|---|
| Product | Conceptual service (e.g. Netflix). No price or duration. |
| Inventory | Sellable variant of a product (price, duration, type, slots) |
| Account | Master credential (email + password) bought from a provider |
| Profile | Profile subdivision of a FAMILY account. **Called "Perfil" in the UI** |
| Subscription | Active link between a client and an account/slot |
| UserGuest | Unregistered client (Sprint 1 only) |
| Reservation | Temporary hold while client pays (Sprint 2) |
| Order | Confirmed purchase after payment validation (Sprint 2) |

**Important:** `slot` = internal/code term. `Perfil` = what the UI shows. Never show "slot" to the user.

## Current active work
UI/UX refactor of the admin panel. Full audit documented in `docs/uiux-audit/`.

Priority bugs:
1. Occupied slots not reflected in Accounts module (data consistency)
2. Subscriptions module shows empty despite existing data (loading/query bug)

## Where context lives
| Location | Content |
|---|---|
| `docs/` | Business rules, domain model, API contracts, sprint plans |
| `docs/uiux-audit/` | UI/UX audit, scores, backlog, confirmed decisions |
| `.claude/agents/` | Specialized agents |
| `api/AGENTS.md` | Backend coding conventions |
| `panel/AGENTS.md` | Frontend coding conventions |

## Tech stack at a glance
- Backend: Spring Boot 3.5, Java 17, Maven, PostgreSQL (Supabase), Flyway, Spring Security 6, JWT
- Frontend (panel): Angular 17, Signals, Reactive Forms, Bootstrap 5, pnpm
- Frontend (store): Angular 16, NgModules, SSR, npm

## Sprint 2 preview (do not implement yet)
- Customer storefront (`store/`) goes live
- Authenticated profiles (Supabase Auth)
- S3 payment receipt upload flow
- Auto slot assignment
- Combo discount engine (2%+ for 2+ services)

---

*Last updated: 2026-03-27*
