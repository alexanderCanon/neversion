# Arquitectura de Alto Nivel

Este diagrama describe la interacción entre los componentes del monorepo y los servicios externos.

```mermaid
graph TD
    subgraph Client_Layer [Capa de Frontends & Marketing]
        WWW[Landing Site - Astro]
        Store[Store App - React 19]
        Panel[Panel App - Angular 21]
    end

    subgraph Edge_Gateway [Capa Edge & Enrutamiento]
        Gateway[API Gateway - Cloudflare Worker]
    end

    subgraph Auth_Service [Identidad]
        SupabaseAuth[Supabase Auth - JWT]
    end

    subgraph Backend_Layer [Capa de Aplicación]
        API[Backend API - Spring Boot Desacoplado]
    end

    subgraph Worker_Layer [Workers Perimetrales]
        NotifWorker[Notification Worker - Cloudflare Worker]
        TelegramWorker[Telegram Reminder - Cloudflare Worker]
    end

    subgraph Persistence_Layer [Capa de Datos]
        DB[(PostgreSQL 17 - Supabase / apps/db)]
    end

    subgraph External_Services [Servicios Externos]
        Resend[Resend - Email API]
        Telegram[Telegram Bot API]
    end

    Store -->|REST| Gateway
    Panel -->|REST| Gateway
    Gateway -->|Forward / Auth Headers| API
    
    API -->|JPA / Flyway| DB
    
    DB -.->|Database Webhook| NotifWorker
    NotifWorker -->|Send Email| Resend
    
    TelegramWorker -.->|Scheduled Cron / Fetch| DB
    TelegramWorker -->|Send Bot Msg| Telegram
    TelegramWorker -->|Send Email| Resend
    
    Store -.->|Auth| SupabaseAuth
    Panel -.->|Auth| SupabaseAuth
```

## Componentes
1. **Landing Site (`apps/www`):** Portal comercial y landing pública de Neversion construida con Astro.
2. **Store App (`apps/store`):** Interfaz para el cliente final en React 19 (Catálogo de servicios, carrito, órdenes y accesos).
3. **Panel App (`apps/panel`):** Interfaz administrativa para Vendedores y Super Admin en Angular 21 (Inventario, cuentas, perfiles, clientes y asignaciones).
4. **API Gateway (`apps/api-gateway`):** Cloudflare Worker que gestiona CORS, pre-validación de tokens JWT y balanceo perimetral.
5. **Supabase Auth:** Proveedor de identidad externo que emite los JWTs de usuario.
6. **Backend API:** Núcleo de lógica de negocio y persistencia relacional (desacoplado).
7. **Notification Worker (`apps/notification-worker`):** Cloudflare Worker reactivo a eventos/webhooks de base de datos para envío de emails mediante Resend.
8. **Telegram Reminder (`apps/telegram-reminder`):** Cloudflare Worker para alertas interactivas de renovación vía Telegram Bot y recordatorios por email.
9. **PostgreSQL 17 (`apps/db` / Supabase):** Base de datos relacional multi-tenant.


