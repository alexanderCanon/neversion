# Gap Analysis

Este documento compara las definiciones funcionales establecidas en el **Discovery** contra la implementación actual reflejada en el **Diagrama ER**.

## 📊 Comparativa de Capacidades

| Área | Categoría | Estado Actual (ER) | Necesidad (Discovery) |
| :--- | :--- | :--- | :--- |
| **Multi-tenancy** | **Aligned** | Tabla `VENDORS` y campo `vendor_id` presente en la mayoría de tablas operativas. | Aislamiento total por vendedor y marca propia. |
| **Inventario Técnico** | **Aligned** | Tablas `ACCOUNTS` y `PROFILES` con estados de disponibilidad. | Control de credenciales y "slots" de acceso. |
| **Venta por Combo** | **Aligned** | Campo `discount_cfg` (JSONB) en `VENDORS` almacena los tiers de descuento por cantidad total de ítems. | Descuentos incrementales por cantidad de servicios en el carrito. |
| **Regla de Renovación** | **Pending EPIC-07** | Regla documentada, aún pendiente de implementación completa. El esquema vigente usa `payment_due_date`/`end_date`; `due_date` es nombre conceptual. | Lógica de cálculo de vencimiento basada en días de mora (BR-07). |
| **Configuración de Descuentos** | **Aligned** | `discount_cfg` (JSONB) en `VENDORS` con estructura de tiers definida en BR-13. | Descuentos configurables por cantidad de ítems en carrito. Estructura documentada. |
| **Trazabilidad de Notificaciones** | **Aligned** | Tabla `NOTIFICATION_LOG` lista para registrar envíos. | Auditoría de correos de bienvenida, recordatorios y vencimientos. |
| **Flujo de Pago** | **Aligned** | Tabla `RESERVATIONS` y `RESERVATION_DETAILS` con soporte para `receipt_url`. | Proceso de subida y validación manual de comprobantes. |
| **Roles de Usuario** | **Aligned** | Campo `role` en `USERS` con 3 valores posibles (`super_admin`, `vendor`, `client`). RBAC implícito validado en el backend. | Permisos por rol documentados en actors.md. Sin necesidad de tabla de permisos para el MVP. |

## 🔍 Hallazgos y Observaciones

1.  **Combos resueltos**: La estructura `discount_cfg` en `VENDORS` cubre la necesidad de descuentos dinámicos. Los tiers se configuran por vendedor y se aplican sobre la cantidad total de ítems (incluyendo servicios repetidos).
2.  **Parámetros de Negocio**: La regla de renovación tardía (BR-07) está documentada para EPIC-07. Si se implementa como constante de aplicación, no requiere campos adicionales en la base de datos.
3.  **Permisos**: RBAC implícito por rol sin tabla de permisos (ADR-08). Los 3 roles están claramente separados.
4.  **Moneda**: El MVP opera solo con GTQ. El esquema ER utiliza campos `decimal` sin gestión de tipos de cambio, lo cual es correcto para el alcance actual.
5.  **Venta cuenta completa**: Se ha documentado como BR-21. Cuando `sale_mode = 'full_account'`, la suscripción se vincula al perfil dueño (`is_owner = true`) como ancla técnica; todos los perfiles de la cuenta quedan activos mientras la suscripción completa esté vigente. EPIC-07 debe liberar perfiles y cuenta al vencer o revocar.
