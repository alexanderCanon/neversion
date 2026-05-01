# Bitácora de Construcción: Panel Administrativo

Este documento es el registro oficial de cambios y decisiones técnicas tomadas durante la implementación del panel administrativo (vendedores y super admin).

## Registro de Cambios

| Fecha | Épica / US | Descripción del Cambio | Decisión Técnica / Nota |
| :--- | :--- | :--- | :--- |
| 2026-04-23 | Setup | Estructura de documentación lista para implementación. | Esperando integración de EPIC-00 en backend. |
| 2026-04-25 | Infra | Fix de dependencias en monorepo. | Se añaden deps faltantes en `packages/utils` y se flexibilizan versiones en `api-client`. |
| 2026-04-27 | EPIC-01 / US-012 | Integración real de Registro de Vendedores. | El front crea el usuario en Supabase y envía el `externalId` al backend. Eliminado flujo de claves temporales. |
| 2026-04-27 | EPIC-01 / US-014 | Lógica de redirección por rol en Login. | Se restringe acceso de clientes al panel administrativo. |
| 2026-04-27 | EPIC-01 / US-015 | Control de acceso por rol. | Rutas protegidas con `roleGuard` (super_admin vs vendedor). |
| 2026-04-27 | EPIC-01 / US-016 | Cierre de sesión seguro. | Implementado logout con Supabase en MainLayout. |
| 2026-04-27 | EPIC-02 / US-017 | Formulario de creación de Servicios. | Implementado con validaciones y nuevos campos de precio/duración alineados con API real. |
| 2026-04-27 | EPIC-02 / US-018 | Edición atómica de Servicios. | Implementado `PUT` hacia la API real con patch de valores. |
| 2026-04-27 | EPIC-02 / US-019 | Activación/Desactivación de Servicios. | Implementado `PATCH /status` para toggle de visibilidad (US-019). |
| 2026-04-27 | EPIC-02 / US-020 | Listado de servicios del vendedor. | Refactorizado para usar `listByVendor` con filtrado por categoría y estado. |
| 2026-04-27 | Lenguaje | Alineación con Glosario B.1. | Renombrado `priceFull` -> `priceComplete`. Labels en UI actualizados a "Cuenta Completa" y "Precio Perfil". |
| 2026-04-27 | EPIC-03 / US-022 | Gestión de Cuentas Maestras. | Formulario actualizado con campos financieros (`cost`, `source`, `purchasedAt`). |
| 2026-04-27 | EPIC-03 / US-025 | Generación Masiva de Perfiles. | Integrado botón de acción rápida para bulk generation desde el detalle de cuenta. |
| 2026-04-27 | EPIC-03 / US-027 | Control Manual de Perfiles. | Implementado bloqueo/desbloqueo quirúrgico con `PATCH /status`. |
| 2026-04-27 | EPIC-03 / US-028 | Detalle de Cuenta (Acordeón). | Implementada carga bajo demanda de perfiles al expandir una cuenta maestra. |
| 2026-04-27 | Infra | Aislamiento Estricto (npmrc). | Configurado `shamefully-hoist=false` para separar físicamente los node_modules de Angular 16 y 17. |
| 2026-04-28 | EPIC-04 / US-029 | Listado de Clientes con Suscripciones | Añadida columna con la cuenta de suscripciones activas (US-029). |
| 2026-04-28 | EPIC-04 / US-030 | Detalle de Cliente Completo | Creación de ClientDetailComponent con Tabs para Info, Suscripciones y Órdenes (US-030). |
| 2026-04-28 | EPIC-04 / US-031 | Creación de Cliente (Validaciones) | Modificado ClientFormComponent para hacer correo requerido y teléfono opcional (US-031). |
| 2026-04-28 | EPIC-04 / US-032 | Edición de Cliente (Email Bloqueado)| Añadida validación de readonly para campo email en modo EDIT (US-032). |
| 2026-04-28 | EPIC-05 / US-035 | Validación de Reservas y Creación de Órdenes. | Implementado flujo de aprobación/rechazo en `ReservationDetailComponent`. |
| 2026-04-28 | EPIC-05 / US-037 | Listado de Órdenes del Vendedor. | Integrado `getOrdersByVendor` con filtrado por estado y búsqueda. |
| 2026-04-28 | EPIC-05 / US-038 | Detalle de Orden con Historial. | Implementado `OrderDetailComponent` incluyendo `statusHistory` y visualización de comprobante. |

| 2026-04-29 | EPIC-06 / US-039 & US-040 | Suggest and Confirm Assignment in OrderDetail. | Calls API to suggest and confirm. Implemented dropdown to override. |
| 2026-04-29 | EPIC-06 / US-030 Fix | Resolved Service and Profile names in ClientDetail. | Updated `ClientService.getDetail()` to perform joins (sub → profile → account → service) for readable names in Panel detail view. |
| 2026-04-29 | EPIC-06 / US-042 | Manual Assignment Modal in Subscriptions. | Opens a modal to select Client, Service, Account and Profile directly. |

| 2026-04-29 | EPIC-07 / US-043 to US-048 | **Full Subscription Life-cycle**: Refactored `SubscriptionsService` to use `SubscriptionsApiService`. Implemented `SubscriptionDetailComponent` (`/subscriptions/:id`) with Renewal (US-045) and Revocation (US-046) capabilities. Updated list view with friendly names and service filtering. Refactored `SubscriptionFormComponent` for manual creation with financial snapshots and notification control (US-048). Added "Detect Expired" manual trigger (US-047). | Full lifecycle management implemented via API-client. Financial data and notifications integrated into manual flows. |
| 2026-04-30 | EPIC-10 / US-063 to US-067 | Implemented vendor KPI dashboard in `apps/panel`: real KPI calls for expiring subscriptions, inventory availability, active clients, successful renewals and gross profit. Dashboard now renders summary cards, expiring subscription groups and inventory by service. | Uses generated `DashboardApiService` after `pnpm api:sync`. Dashboard routes are vendor-only; `super_admin` fallback redirects to `/vendors`. Removed remote Google Fonts import so production build does not depend on network. Verification by Alex: frontend tests/build OK. |
