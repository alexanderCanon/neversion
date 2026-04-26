# Project Agents — Single Source of Truth

This document defines the roles, responsibilities, and technical constraints for all AI agents participating in the Neversion platform development.

## 🤖 Agent Registry

| Name | Role | Primary Scope | Operational Protocol |
| :--- | :--- | :--- | :--- |
| **Claude** | Backend Architect | `/apps/api` | Hexagonal Architecture & DDD |
| **Gemini** | Frontend Engineer | `/apps/panel`, `/apps/store` | Angular (16/17) & Signals |
| **Jules** | Testing Specialist | `**/*.spec.ts`, `/apps/*/src/test` | Jasmine & Karma (Frontend) |

---

## 💎 Global Engineering Mandates (All Agents)

### 1. Absolute Type Safety
- **No `any` allowed.** Under any circumstance.
- Use `unknown` with Type Guards or explicit interfaces from `@neversion/models`.

### 2. Surgical Precision
- Follow the **Analysis -> Proposal -> Plan -> Act -> Validate** cycle.
- Do not refactor unrelated code. Focus strictly on the assigned task.

### 3. Source of Truth
- The `/docs` directory is the immutable source of truth for business rules and ubiquitous language.
- Agents must register every change in the corresponding bitácora in `/docs/implementation/`.

---

## 🛠 Command Constraints

### Allowed
- `pnpm install`
- `pnpm run build` / `pnpm run lint`
- `pnpm run test`
- `git add`, `git commit`, `git push`

### Forbidden
- Destructive commands: `rm -rf`, `git reset --hard`, `git push --force`.
- Global installations: `npm install -g`.
- Direct DB access: `psql`, `flyway migrate`.

---
*Last Updated: 2026-04-25*
