# GEMINI.md — Project Overview & Instructions

Welcome to the **Neversion** repository. This file serves as the primary instructional context for Gemini CLI agents. It provides a high-level overview of the project, architecture, and development standards.

## Project Overview
**Neversion** is a SaaS platform designed for the management and resale of digital services. It enables vendors to manage accounts, profiles (perfiles), and subscriptions, while providing clients with a seamless store interface for purchasing and managing their digital access.

### Core Technologies
- **Monorepo Manager:** `pnpm` workspaces.
- **Backend:** Java 17, Spring Boot, Maven, PostgreSQL.
- **Frontend:** Angular 17 (Typescript, RxJS, Signals).
- **Infrastructure:** Docker, Docker Compose.
- **API Integration:** OpenAPI / Swagger with generated TypeScript clients.

---

## Project Structure
```text
.
├── apps/
│   ├── api/          # Backend (Spring Boot)
│   ├── panel/        # Admin & Vendor UI (Angular)
│   └── store/        # Client Store UI (Angular)
├── packages/
│   ├── api-client/   # Generated API client (Angular services)
│   ├── models/       # Shared TypeScript models
│   └── utils/        # Shared utilities
├── docs/             # Comprehensive documentation (Spanish)
│   ├── agents/       # Agent-specific protocols (CRITICAL)
│   ├── domain/       # Ubiquitous language and business rules
│   └── implementation/# Progress logs (Bitácoras)
├── infra/            # Docker and deployment configurations
└── Makefile          # Root command hub
```

---

## Getting Started: Agent Protocols
Before performing any task, you **MUST** read the relevant protocol in `docs/agents/`:

1.  **Global Entry Point:** `docs/agents/AGENTS.md` - Rules for all agents.
2.  **Backend Tasks:** `docs/agents/CLAUDE.md` - Protocol for `/apps/api`.
3.  **Frontend Tasks:** `docs/agents/GEMINI.md` - Protocol for `/apps/panel` and `/apps/store`.

### Mandatory Rules for Agents
- **Isolation:** Work only in your assigned app directory. Do not modify other apps.
- **API-First:** Frontend agents must consume the API and never access the database.
- **Logging:** Every change must be recorded in the implementation logs (bitácoras) in `docs/implementation/`.
- **Language:** Code and comments in **English**; UI labels and user messages in **Spanish**.
- **Zero Guesswork:** If a requirement is unclear, stop and report a **BLOCKER** as defined in `AGENTS.md`.

---

## Common Commands

### Root Level
- `make help`: Show all available commands in the root Hub.
- `pnpm install`: Install all dependencies for the monorepo.
- `pnpm -r build`: Build all projects in the workspace.
- `pnpm api:sync`: Regenerate the `api-client` package from the backend OpenAPI spec.

### Docker Orchestration
- `make up-local`: Start the full stack (API, DB, Panel) locally.
- `make down-local`: Stop the local stack.

### Backend (`apps/api`)
- `./mvnw clean install`: Build the backend.
- `./mvnw test`: Run backend unit tests.
- `./mvnw verify`: Run backend integration tests.

### Frontend (`apps/panel` or `apps/store`)
- `pnpm run build`: Build the specific frontend app.
- `pnpm run test`: Run unit tests (Karma).
- `pnpm run lint`: Run ESLint checks.

---

## Development Workflow
1.  **Research:** Read the documentation in `docs/` (Domain, Epics, Stories).
2.  **Plan:** Create a strategy and update the implementation bitácora.
3.  **Implement:** Write surgical, idiomatic code.
4.  **Validate:** Run tests and linting. A module is only done when tests are green.
5.  **Sync:** If the API changed, run `pnpm api:sync` to update the frontend client.

---
*Last Updated: April 2026*
