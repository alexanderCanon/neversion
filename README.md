# Neversion

Multi-tenant digital services management platform and client storefront monorepo — handling vendor operations, subscriptions, accounts, profiles, reservations, notifications, and client storefront.

---

## 🚀 Tech Stack

* **Frontend Admin (Panel):** Angular 21 (Standalone Components, Signals, Bootstrap 5, SCSS)
* **Frontend Storefront (Store):** React 19 + Vite 8 + Tailwind CSS v4 + Bun Runtime (SPA)
* **Backend API Gateway:** Cloudflare Workers (TypeScript, Vitest, Wrangler)
* **Microservices:** Rust (gRPC, Tonic, Prost, SQLite)
* **Website:** Astro
* **Observability:** Grafana, Grafana Alloy, Prometheus
* **Package Management & Tooling:** PNPM Workspaces, Bun, ESLint, TypeScript 5.9, OpenAPI Generator
* **Deployment & Hosting:** Cloudflare Pages, Cloudflare Workers, Docker Compose

---

## 🏛️ Monorepo Structure

```text
neversion/
├── apps/
│   ├── panel/                # Admin Management UI (Angular 21 SPA)
│   ├── store/                # Customer Storefront (React 19 + Vite 8 + Bun SPA)
│   ├── api-gateway/          # Cloudflare Worker API Gateway & Auth router
│   ├── notification-service/ # Rust gRPC transactional notification service (Resend)
│   ├── reservation-service/  # Rust gRPC temporary reservation service
│   ├── db/                   # PostgreSQL database configuration and compose definitions
│   ├── monitoring/           # Observability stack (Grafana, Alloy, Prometheus)
│   └── www/                  # Marketing landing site (Astro)
├── packages/
│   ├── api-client/           # Generated TypeScript Angular API client (OpenAPI)
│   ├── models/               # Shared TypeScript domain models & interfaces
│   └── utils/                # Shared Angular utilities & helpers
└── docs/                     # Architecture Decision Records (ADR), domain models, and guides
```

---

## 📋 Prerequisites

* **Node.js 24+**
* **pnpm 11+** (`corepack enable` or `npm install -g pnpm`)
* **Rust & Cargo** (for building notification and reservation gRPC microservices)
* **Docker & Docker Compose** (for database and local observability workflows)

---

## ⚙️ Getting Started

### 1. Install Dependencies

Install all workspace dependencies across packages and applications:

```bash
pnpm install
```

### 2. Build Workspace Packages

Build all shared packages (`@neversion/models`, `@neversion/utils`, `@neversion/api-client`):

```bash
pnpm -r build
```

### 3. Run Applications in Development

#### Panel (Admin UI)
```bash
# Starts development server on http://localhost:4200
pnpm --filter panel start
```

#### Store (Customer Storefront)
```bash
# Starts development server on http://localhost:4000
cd apps/store && bun dev
# or from root: pnpm --filter store dev
```

#### API Gateway (Cloudflare Worker)
```bash
# Starts local development worker via Wrangler
pnpm --filter neversion-api-gateway dev
```

#### Notification Service (Rust gRPC)
```bash
cd apps/notification-service
cargo run
```

---

## 🧪 Testing & Linting

Run automated test suites and linting across the monorepo:

```bash
# Run lint across all workspace packages
pnpm lint

# Run API Gateway tests
pnpm --filter neversion-api-gateway test

# Run Panel tests (Karma / Jasmine)
pnpm --filter panel test

# Run Store tests (Karma / Jasmine)
pnpm --filter store test
```

---

## 📖 OpenAPI Client Synchronization

To regenerate the Angular TypeScript API client from the running Spring Boot API docs:

```bash
# Ensure API is running on localhost:8080 or pass custom API_URL
pnpm api:sync
```

---

## 🚢 Deployment & CI/CD

Applications and edge services in this monorepo are continuously deployed via GitHub Actions workflows:

* **Panel:** Deployed to Cloudflare Pages (`apps/panel/dist/panel/browser`)
* **Store:** Deployed to Cloudflare Pages (`apps/store/dist`)
* **API Gateway:** Deployed to Cloudflare Workers (`apps/api-gateway`)

Runtime configuration is injected during the build step via `write-runtime-config.mjs` using GitHub Secrets and Cloudflare environmental variables.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
