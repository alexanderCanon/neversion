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
