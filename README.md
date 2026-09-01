# Neversion

Multi-tenant digital services management platform and client storefront monorepo — handling vendor operations, subscriptions, accounts and profiles, notifications, and client storefront.

---

## 🌐 Ecosistema & Repositorios Relacionados

La plataforma **Neversion** está dividida en los siguientes repositorios especializados:

| Repositorio | Descripción | Tecnologías Clave |
| :--- | :--- | :--- |
| [**neversion**](https://github.com/alexanderCanon/neversion) *(este repo)* | Frontends (Panel Admin, Storefront, Landing), Edge Workers y DB local | Angular 21, React 19, Astro, Cloudflare Workers, PostgreSQL 17 |
| [**neversion-api**](https://github.com/alexanderCanon/neversion-api) | Backend API Central (Lógica de dominio, contratos REST y persistencia) | Spring Boot 4, Java 21, Arquitectura Hexagonal, DDD, Flyway |
| [**neversion-infra**](https://github.com/alexanderCanon/neversion-infra) | Aprovisionamiento de infraestructura en la nube y entornos de despliegue | Terraform, AWS |

---

## 🚀 Tech Stack

* **Frontend Admin (Panel):** Angular 21 (Standalone Components, Signals)
* **Frontend Storefront (Store):** React 19 + Vite 8 + Bun + Tailwind CSS v4
* **Website (www):** Astro + Tailwind CSS
* **Edge Workers & Services:** Cloudflare Workers (TypeScript, Wrangler, Vitest)
  * `api-gateway`: Enrutamiento y validación JWT perimetral.
  * `notification-worker`: Despacho de emails transaccionales con Resend vía Supabase Webhooks.
  * `telegram-reminder`: Bot interactivo y cron de recordatorios de renovación por Telegram/Email.
* **Database & Persistence:** PostgreSQL 17 (Supabase / Docker compose)
* **Package Management & Tooling:** PNPM Workspaces, Bun, ESLint, TypeScript 5.9
* **Deployment & Hosting:** Cloudflare Network (Cloudflare Pages y Cloudflare Workers)

---

## 🏛️ Monorepo Structure

```text
neversion/
├── apps/
│   ├── panel/                # Admin Management UI (Angular 21)
│   ├── store/                # Customer Storefront (React 19)
│   ├── www/                  # Marketing landing site (Astro)
│   ├── api-gateway/          # Cloudflare Worker API Gateway & Auth router
│   ├── notification-worker/  # Cloudflare Worker for Resend transactional emails
│   ├── telegram-reminder/    # Cloudflare Worker for Telegram bot & renewal alerts
│   └── db/                   # PostgreSQL database configuration for self-hosted
├── packages/
│   ├── models/               # Shared TypeScript domain models & interfaces
│   └── utils/                # Shared Angular utilities & helpers
└── docs/                     # Architecture Decision Records (ADRs), domain models, and guides
```

---

## 📋 Prerequisites

* **Node.js 24+**
* **pnpm 10+** (or Bun for `apps/store`)
* **Docker & Docker Compose** (optional for local DB)

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

#### Marketing Landing (Website)
```bash
# Starts development server on http://localhost:4321
pnpm --filter neversion-www dev
```

#### API Gateway & Workers
```bash
# Starts local development worker via Wrangler
pnpm --filter neversion-api-gateway dev
pnpm --filter notification-worker dev
pnpm --filter neversion-telegram-reminder dev
```

---

## 🧪 Testing & Linting

Run automated test suites and linting across the monorepo:

```bash
# Run lint across all workspace packages
pnpm lint

# Run API Gateway and Workers tests (Vitest)
pnpm --filter neversion-api-gateway test
pnpm --filter notification-worker test
pnpm --filter neversion-telegram-reminder test

# Run Panel tests (Karma / Jasmine)
pnpm --filter panel test

# Run Store tests (Vitest)
pnpm --filter store test
```

---

## 🚢 Deployment & CI/CD

Applications and edge services in this monorepo are continuously deployed via GitHub Actions workflows:

* **Panel:** Deployed to Cloudflare Pages (`apps/panel/dist/panel/browser`)
* **Store:** Deployed to Cloudflare Pages (`apps/store/dist`)
* **Website:** Deployed to Cloudflare Pages (`apps/www/dist`)
* **Workers:** Deployed to Cloudflare Workers using Wrangler (`api-gateway`, `notification-worker`, `telegram-reminder`)

Runtime configuration is injected during the build step via `write-runtime-config.mjs` using GitHub Secrets and Cloudflare environment variables.


---

## 📄 License

This project is licensed under the [MIT License](LICENSE).

And... thanks for reading! I always do my best to improve this.