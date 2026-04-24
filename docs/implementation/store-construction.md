# Bitácora de Construcción: Tienda y Cliente

Este documento es el registro oficial de cambios y decisiones técnicas tomadas durante la implementación de la tienda pública y el panel del cliente.

## Registro de Cambios

| Fecha | Épica / US | Descripción del Cambio | Decisión Técnica / Nota |
| :--- | :--- | :--- | :--- |
| 2026-04-23 | Setup | Estructura de documentación lista para implementación. | Esperando integración de EPIC-00 en backend. |
| 2026-04-23 | US-013 | Implementación de `SupabaseService` y refactorización de `AuthService`. | Se elimina el mock de login/registro y se conecta a Supabase Auth. |
| 2026-04-23 | US-014 | Lógica de redirección por rol en Login. | Redirección a `/customer-panel` para clientes. |
| 2026-04-23 | US-015 | Control de acceso por rol. | Implementado `roleGuard` para panel de clientes. |
| 2026-04-23 | US-016 | Cierre de sesión seguro. | Implementado logout con Supabase en Toolbar. |
