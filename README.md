# Neversion

Multi-tenant digital services management platform and client storefront monorepo — handling vendor operations, subscriptions, accounts and profiles, notifications, and client storefront.

---

## 🚀 Tech Stack

* **Frontend Admin (Panel):** Angular 21 (Standalone Components, Signals, Bootstrap 5)
* **Frontend Storefront (Store):** React 19 + Vite 8 + Tailwind CSS v4
* **Backend API Gateway:** Cloudflare Workers (TypeScript)
* **Website (www):** Astro
* **Observability:** Grafana, Grafana Alloy, Prometheus
* **Package Management & Tooling:** PNPM Workspaces, Bun, ESLint, TypeScript 5.9, OpenAPI Generator
* **Deployment & Hosting:** Cloudflare Network (Edge, Pages and Workers)

---

## 🏛️ Monorepo Structure

```text
neversion/
├── apps/
│   ├── panel/                # Admin Management UI
│   ├── store/                # Customer Storefront
│   ├── api-gateway/          # Cloudflare Worker API Gateway & Auth router
│   ├── db/                   # PostgreSQL database configuration for self-hosted
│   ├── monitoring/           # Observability stack (Grafana, Alloy, Prometheus)
│   └── www/                  # Marketing landing site
├── packages/
│   ├── models/               # Shared TypeScript domain models & interfaces
│   └── utils/                # Shared Angular utilities & helpers
└── docs/                     # Architecture Decision Records (ADR), domain models, and guides
```

---

## 📋 Prerequisites

* **Node.js 24+**
* **pnpm 11+** (don't use npm)
* **Docker & Docker Compose** (optional for DB)

---

## ⚙️ Getting Started

### 1. Install Dependencies

Install all workspace dependencies across packages and applications:

```bash
pnpm install
```

### 2. Build Workspace Packages

Build all shared packages (`@neversion/models`, `@neversion/utils`):

```bash
pnpm -r build
```
(This maybe require 2GB free RAM)

### 3. Run Applications in Development

#### Panel (Admin UI)
```bash
# Starts development server on http://localhost:4200
pnpm --filter panel start
```

#### Store (Customer Storefront)
```bash
# Starts development server on http://localhost:4000
pnpm --filter store dev
```

#### API Gateway (Cloudflare Worker)
```bash
# Starts local development worker via Wrangler
pnpm --filter neversion-api-gateway dev
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

## 🚢 Deployment & CI/CD

Applications and edge services in this monorepo are continuously deployed via GitHub Actions workflows:

* **Panel:** Deployed to Cloudflare Pages (`apps/panel/dist/panel/browser`)
* **Store:** Deployed to Cloudflare Pages (`apps/store/dist`)
* **API Gateway:** Deployed to Cloudflare Workers using wrangler(`apps/api-gateway`)

Runtime configuration is injected during the build step via `write-runtime-config.mjs` using GitHub Secrets and Cloudflare environmental variables.

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

And... thanks for reading! I always do my best to improve this.