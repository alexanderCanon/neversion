# Bitácora de Construcción: Tienda y Cliente

Este documento es el registro oficial de cambios y decisiones técnicas tomadas durante la implementación de la tienda pública y el panel del cliente.

## Registro de Cambios

| Fecha | Épica / US | Descripción del Cambio | Decisión Técnica / Nota |
| :--- | :--- | :--- | :--- |
| 2026-04-23 | Setup | Estructura de documentación lista para implementación. | Esperando integración de EPIC-00 en backend. |
| 2026-04-25 | Infra | Sincronización de dependencias del workspace. | Corregido error de módulos no encontrados en `packages/utils` para el build de la tienda. |
| 2026-04-27 | EPIC-01 / US-013 | Integración real de Registro de Clientes. | El flujo crea el usuario en Supabase y luego invoca al backend para crear el registro de cliente enviando el `externalId`. |
| 2026-04-27 | EPIC-01 / US-014 | Lógica de redirección por rol en Login. | Redirección a `/customer-panel` para clientes. |
| 2026-04-27 | EPIC-01 / US-015 | Control de acceso por rol. | Implementado `roleGuard` para panel de clientes. |
| 2026-04-27 | EPIC-01 / US-016 | Cierre de sesión seguro. | Implementado logout con Supabase en Toolbar. |
| 2026-04-27 | EPIC-02 / US-021 | Catálogo público real. | Refactorizado `PlatformService` para consumir `listActive` de la API real. Eliminadas consultas directas a Supabase. |
| 2026-04-27 | Lenguaje | Alineación con Glosario B.1. | Los items del catálogo ahora muestran "Precio Perfil" y "Cuenta Completa" según el estándar del proyecto. |
| 2026-04-27 | Infra | Resolución de conflictos v16/v17. | Se forzó el uso de librerías locales en `tsconfig.json` y se bloqueó el hoisting cruzado en `.npmrc`. |
| 2026-04-28 | EPIC-05 / US-033 | Implementación de `CartService` y `CheckoutComponent`. | Manejo de estado de carrito con RxJS `BehaviorSubject`. Integración con `createReservation`. |
| 2026-04-28 | EPIC-05 / US-034 | Implementación de `PaymentPageComponent` y subida de comprobante. | Flujo de subida de archivos (simulado) con integración a `uploadReceipt`. |

| 2026-04-29 | EPIC-06 / US-041 | Client Accesses View. | Added `app-customer-accesses` inside `CustomerPanelComponent` to list active subscriptions. |
