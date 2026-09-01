# CLAUDE.md

This file provides guidance to Claude Code and AI agents when working with code in this repository.

## Project Overview

**Neversion** is a multi-tenant SaaS platform for managing and reselling digital services (streaming subscriptions, profiles, and digital goods). Vendors manage accounts, profiles, and clients via an administrative panel; clients purchase and renew subscriptions through a high-performance storefront.

## Monorepo Structure

```
apps/
  panel/                # Angular 21 — Admin UI (standalone components + Signals + Tailwind)
  store/                # React 19 + Vite 8 + Bun — Client storefront SPA (TanStack Query + Tailwind v4)
  www/                  # Marketing landing site (Astro + Tailwind)
  api-gateway/          # Cloudflare Worker — Edge API Gateway, CORS & Auth router
  notification-worker/  # Cloudflare Worker — Transactional email worker (Resend / Supabase Webhooks)
  telegram-reminder/    # Cloudflare Worker — Telegram Bot & Subscription renewal reminder worker
  db/                   # PostgreSQL 17 configuration & local docker compose
packages/
  models/               # Shared TypeScript interfaces & types
  utils/                # Shared Angular utilities
docs/
  domain/               # Ubiquitous language, business rules, use cases (Spanish)
  architecture/         # ADRs, ER diagram, NFRs, deployment
  diagrams/             # Architectural & sequence diagrams (Mermaid)
  backlog/              # Epics + per-epic story files
  implementation/       # Historical implementation logs (read-only reference)
```

## Commands

### Root
```bash
pnpm install          # Install all workspace dependencies
pnpm -r build         # Build all packages
```

### Admin Panel (`apps/panel`)
```bash
cd apps/panel
pnpm start            # Dev server at http://localhost:4200
pnpm test             # Run all Karma tests
pnpm build            # Production build
```

### Storefront (`apps/store`)
```bash
cd apps/store
bun dev               # Dev server at http://localhost:4000
bun run test          # Run Vitest tests
bun run build         # SPA production build
```

### Marketing Landing (`apps/www`)
```bash
cd apps/www
pnpm dev              # Astro dev server
pnpm build            # Astro production build
```

### Edge Workers (`apps/api-gateway`, `apps/notification-worker`, `apps/telegram-reminder`)
```bash
cd apps/api-gateway && pnpm test             # Vitest worker tests
cd apps/notification-worker && pnpm test     # Vitest worker tests
cd apps/telegram-reminder && pnpm test       # Vitest worker tests
```

## Architecture

### Frontend Applications

- **Panel (Angular 21):** Standalone components, Signals for state management, Reactive Forms, Tailwind CSS. Focuses on vendor catalog, inventory management, and client operations.
- **Store (React 19):** React 19 + Vite 8 + Bun, TanStack Query, Lucide icons, Tailwind CSS v4. Focuses on fast, mobile-friendly consumer checkout and subscription access.
- **Marketing Site (Astro):** Modular components, high-speed static generation for SEO and conversion.

### Edge Workers & Backend Services

- **API Gateway (`apps/api-gateway`):** Cloudflare Worker handling edge routing, rate limiting, and JWT validation before proxying requests.
- **Notification Worker (`apps/notification-worker`):** Cloudflare Worker responding to Supabase Database Webhooks to dispatch emails via Resend.
- **Telegram Reminder (`apps/telegram-reminder`):** Cloudflare Worker managing bot interactions and scheduled renewal alerts via Telegram and Email.
- **Central Backend API:** Decoupled Spring Boot / Hexagonal backend providing core domain logic and REST API contracts.

### Language & Domain Standards

- **Code and comments:** English
- **UI labels, placeholders, user-facing messages:** Spanish
- **Domain terminology:** Must match `docs/domain/ubiquitous-language.md` exactly

## Critical Documentation

Before working on any feature, consult:

| File | Purpose |
|------|---------|
| `docs/domain/ubiquitous-language.md` | Canonical Spanish/English business terminology |
| `docs/domain/business-rules.md` | Authoritative system business rules |
| `docs/architecture/decisions.md` | Architectural Decision Records (ADRs 1–17) |
| `docs/architecture/deployment.md` | System deployment topology |
| `docs/diagrams/architecture.md` | High-level system architecture |
| `docs/backlog/stories/` | User stories per Epic |
| `docs/implementation/` | Read-only historical implementation logs |

## Isolation Rules

- Work strictly within the assigned application or package scope.
- Never make unrequested architectural changes or alter database schema without prior plan approval.

