# Neversion — Decisiones de Arquitectura y Stack Tecnológico

> Este documento resume los principios y decisiones arquitectónicas fundamentales que dan forma a la plataforma **Neversion**, diseñado para máxima especialización de capas, rendimiento en el Edge y velocidad de desarrollo.

---

## 1. Visión General de la Arquitectura

Neversion opera bajo una arquitectura desacoplada y orientada al rendimiento, donde cada capa tecnológica fue seleccionada para resolver un dominio específico:

```
                                  ┌──────────────────────────┐
                                  │   Cloudflare Edge / CDN  │
                                  └─────────────┬────────────┘
                                                │
       ┌────────────────────────┬───────────────┴───────────────┬────────────────────────┐
       │ (Marketing / SEO)      │ (B2C Storefront)              │ (B2B Admin Panel)      │ (Edge Workers)
       ▼                        ▼                               ▼                        ▼
┌──────────────┐         ┌──────────────┐                ┌──────────────┐         ┌──────────────┐
│   apps/www   │         │apps/new-store│                │  apps/panel  │         │ api-gateway  │
│   (Astro)    │         │ (React + Bun)│                │ (Angular 21) │         │telegram-rmndr│
└──────────────┘         └──────┬───────┘                └──────┬───────┘         └──────┬───────┘
                                │                               │                        │
                                └───────────────┬───────────────┴────────────────────────┘
                                                ▼
                               ┌────────────────────────────────┐
                               │   Edge API Gateway (Worker)    │
                               └────────┬──────────────┬────────┘
                                        │              │
                    ┌───────────────────┴──┐           └─── gRPC ───┐
                    ▼                                               ▼
     ┌────────────────────────────┐                  ┌────────────────────────────┐
     │  apps/api (Spring Boot 3)  │                  │  Microservicios en Rust    │
     │  Core DDD / Hexagonal      │                  │  (notification / reserv.)  │
     └──────────────┬─────────────┘                  └────────────────────────────┘
                    ▼
     ┌────────────────────────────┐
     │ PostgreSQL 17 / Supabase   │
     │ Auth + PostgREST Views     │
     └────────────────────────────┘
```

---

## 2. Decisiones Tecnológicas por Capa

### 🌐 1. Marketing & Cara Pública (`apps/www`)
* **Tecnología:** Astro
* **Justificación:** 
  - La cara pública debe cargar instantáneamente con cero JavaScript innecesario en el cliente (*Zero-JS baseline*).
  - Maximiza las métricas de **Core Web Vitals** y posicionamiento **SEO**.
  - Permite estructurar contenido modular con Markdown y componentes estáticos.

### 🛍️ 2. Storefront B2C (`apps/new-store`)
* **Tecnología:** React 19 + Vite 8 + Tailwind CSS v4 + **Bun Runtime** (Dev/Build)
* **Justificación:**
  - Es el canal directo de ventas B2C: requiere máxima agilidad de iteración visual, conversión fluida y un ecosistema de librerías UI maduro.
  - **Uso de Bun como Runtime:** Provee tiempos de arranque (cold start), recarga en caliente (HMR) y empaquetado de producción ultra rápidos.
  - **Despliegue Estático:** Se compila como SPA pura para servirse desde el Edge en Cloudflare Pages, con costo de servidor cero y latencia global mínima.

### 📊 3. Panel de Administración B2B (`apps/panel`)
* **Tecnología:** Angular 21 (Standalone Components + Signals)
* **Justificación:**
  - El backoffice empresarial requiere tipado rígido, formularios reactivos robustos, gestión estricta de estado y coherencia en tablas de datos complejas.
  - El modelo de reactividad nativo con Signals optimiza el renderizado de paneles de alta densidad de datos sin sobrecarga.

### ⚡ 4. Edge Layer & Automatización (`apps/api-gateway`, `apps/telegram-reminder`)
* **Tecnología:** Cloudflare Workers (TypeScript) + Wrangler CLI
* **Justificación:**
  - **api-gateway:** Enrutamiento de peticiones, validación de autenticación y balanceo en el Edge antes de llegar al backend.
  - **telegram-reminder:** Workers automatizados para notificaciones y recordatorios directos sin costo de servidores dedicados 24/7.

### ⚙️ 5. Core Backend & Microservicios
* **Core API (`apps/api`):** Spring Boot 3 (Java 17) con Arquitectura Hexagonal y Domain-Driven Design (DDD) para la lógica transaccional crítica, pasarelas de pago y consistencia bancaria.
* **Microservicios Rust (`notification-service`, `reservation-service`):** Servicios de baja latencia con gRPC para procesamiento de colas y emails transaccionales.
* **Persistencia:** PostgreSQL 17 gestionado con Supabase (Auth, Storage para comprobantes y vistas públicas PostgREST `v_store_*`).

---

## 3. Principios de Monorepo y Gobernanza

1. **Gestión Unificada con `pnpm` y Catálogos:**
   - La raíz utiliza `pnpm workspaces` con el bloque `catalog:` en `pnpm-workspace.yaml`.
   - Garantiza versiones idénticas de TypeScript, ESLint, Vitest, Wrangler y el SDK de Supabase en todas las aplicaciones.
2. **Contratos Fuertes y Compartidos:**
   - `packages/models`: Interfaces de TypeScript puras sin dependencias de frameworks, consumidas por todas las aplicaciones frontend.
   - Generación OpenAPI (`pnpm api:sync`): Sincronización automática de clientes API desde la documentación viva del backend.
3. **Aislamiento de Runtimes:**
   - `pnpm` gobierna la instalación de dependencias y symlinks a nivel monorepo.
   - `bun` acelera la ejecución de scripts y bundling local dentro de `apps/new-store` sin interferir con los paquetes globales.

---

## 4. Filosofía de Despliegue y Operación

* **Frontend:** Despliegues continuos mediante GitHub Actions hacia **Cloudflare Pages**, utilizando inyección de configuración en runtime (`window.__NEVERSION_CONFIG__`) para alternar entornos (desarrollo, staging, producción) sin recompilar el código fuente.
* **Backend:** Contenedores ligeros y arquitectura gRPC orientada a eventos.
