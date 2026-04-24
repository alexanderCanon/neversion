# Bitácora de Construcción: Panel Administrativo

Este documento es el registro oficial de cambios y decisiones técnicas tomadas durante la implementación del panel administrativo (vendedores y super admin).

## Registro de Cambios

| Fecha | Épica / US | Descripción del Cambio | Decisión Técnica / Nota |
| :--- | :--- | :--- | :--- |
| 2026-04-23 | Setup | Estructura de documentación lista para implementación. | Esperando integración de EPIC-00 en backend. |
| 2026-04-23 | US-012 | Implementación de modelos compartidos en `packages/models`. | Se centralizan interfaces de Auth y Roles. |
| 2026-04-23 | US-014 | Lógica de redirección por rol en Login. | Se restringe acceso de clientes al panel administrativo. |
| 2026-04-23 | US-015 | Control de acceso por rol. | Rutas protegidas con `roleGuard` (super_admin vs vendedor). |
| 2026-04-23 | US-016 | Cierre de sesión seguro. | Implementado logout con Supabase en MainLayout. |
