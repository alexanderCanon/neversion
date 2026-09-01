# Deployment

## Topología del sistema

```mermaid
graph TD
    Client[Usuarios / Clientes]
    
    subgraph Edge["Cloudflare Network"]
        WWW["apps/www (Astro / Cloudflare Pages)"]
        Store["apps/store (React 19 / Cloudflare Pages)"]
        Panel["apps/panel (Angular 21 / Cloudflare Pages)"]
        Gateway["apps/api-gateway (Cloudflare Worker)"]
        NotifWorker["apps/notification-worker (Cloudflare Worker)"]
        TelegramWorker["apps/telegram-reminder (Cloudflare Worker)"]
    end

    subgraph BackendServices["Backend & External Services"]
        API["Backend API (Spring Boot / Servicio Desacoplado)"]
        DB[(PostgreSQL 17 - Supabase / apps/db)]
        Auth[Supabase Auth (JWT)]
        ResendMail[Resend Email API]
        TelegramAPI[Telegram Bot API]
    end

    Client --> WWW
    Client --> Store
    Client --> Panel
    
    Store --> Gateway
    Panel --> Gateway
    Gateway --> API
    
    API --> DB
    API --> Auth
    
    DB -.->|Database Webhook| NotifWorker
    NotifWorker --> ResendMail
    
    TelegramWorker -.->|Cron / Webhook| DB
    TelegramWorker --> TelegramAPI
    TelegramWorker --> ResendMail
```

---

## Componentes

### 1. Aplicaciones Web (Frontends en Cloudflare Pages)
- **`apps/www` (Landing & Marketing):** Construido con **Astro** y Tailwind CSS. Alta optimización SEO y velocidad para captación de clientes.
- **`apps/store` (Storefront del Cliente):** SPA en **React 19 + Vite 8 + Bun + Tailwind CSS v4**. Catálogo público de servicios, compras y checkout asistido.
- **`apps/panel` (Panel Administrativo):** SPA en **Angular 21 (Standalone Components + Signals)** para gestión de inventario, cuentas, perfiles, clientes y órdenes por parte de los vendedores y super admins.

### 2. Edge Workers (Cloudflare Workers)
- **`apps/api-gateway`:** Enrutamiento perimetral, gestión CORS y validación temprana de JWTs.
- **`apps/notification-worker`:** Worker reactivo a Webhooks de Supabase Database para despacho de correos transaccionales vía **Resend**.
- **`apps/telegram-reminder`:** Bot y cron interactivo para alertas de renovación y recordatorios automáticos hacia Telegram y Email.

### 3. Backend Central & Persistencia
- **API Backend:** Servicio central desacoplado que implementa lógica de dominio hexagonal, reglas de negocio y endpoints REST `/api/v1/**`.
- **PostgreSQL 17 (`apps/db` / Supabase):** Base de datos relacional multi-tenant con partición lógica por `vendor_id`.
- **Supabase Auth:** Proveedor de identidad y emisión de JWTs.
- **Resend:** Proveedor de emails transaccionales.
- **Telegram Bot API:** Canal interactivo de notificaciones operativas.

---

## Ambientes

| Ambiente | Propósito | Infraestructura |
| :--- | :--- | :--- |
| **local** | Desarrollo individual | `apps/db` Compose local / Wrangler Dev / Vite & Angular Dev servers |
| **preview / staging** | Integración continua y PRs | Cloudflare Pages Preview / Supabase Staging |
| **production** | Operación real | Cloudflare Pages + Workers Prod / Supabase Prod |

---

## Flujo de despliegue
`feature/<scope>` → `develop` → `main`

- Cada agente y contribuidor trabaja en su rama de feature aislada.
- Builds y pruebas automatizadas en GitHub Actions (Vite, Angular, Vitest, Wrangler).
- Despliegue continuo hacia Cloudflare Pages y Workers.