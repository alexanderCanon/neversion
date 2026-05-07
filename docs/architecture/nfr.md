# NFR

Este documento detalla los Requerimientos No Funcionales (NFR) que garantizan la calidad, seguridad y escalabilidad del sistema.

---

### NFR-01 — Seguridad
- Todos los endpoints protegidos por autenticación, excepto los públicos de la tienda.
- El control de acceso por rol es responsabilidad del sistema, no del proveedor de auth externo.
- Los IDs internos (**BIGINT**) nunca se exponen en APIs públicas — solo se utiliza **UUID**.
- Las credenciales de cuentas (email, contraseña del servicio) deben almacenarse con acceso restringido.

### NFR-02 — Disponibilidad
- El sistema debe estar disponible para la operación diaria del vendedor sin interrupciones planificadas en horario comercial.
- Las notificaciones por correo no deben depender de la disponibilidad del panel.

### NFR-03 — Escalabilidad
- El modelo de datos debe soportar múltiples vendedores desde el inicio sin necesidad de refactorización.
- El pricing y los descuentos no deben estar hardcodeados — deben ser configurables por vendedor.

### NFR-04 — Mantenibilidad
- La arquitectura **hexagonal** del backend debe mantenerse — lógica de negocio aislada de frameworks e infraestructura.
- Cada módulo debe tener una cobertura mínima de pruebas unitarias e integración.
- Las migraciones de base de datos deben gestionarse exclusivamente con **Flyway** — sin cambios manuales al schema.

### NFR-05 — Trazabilidad
- Toda notificación enviada debe quedar registrada en `notification_log`.
- Las órdenes deben registrar fecha de creación y fecha de aprobación.
- Las suscripciones deben registrar la orden de origen para trazabilidad completa del ciclo de venta.

### NFR-06 — Consistencia de datos
- Los valores de **enum** se persisten en lowercase en toda la base de datos.
- Un perfil activo no puede estar vinculado a más de una suscripción activa simultáneamente.
- Una reserva expira automáticamente después de **1 hora** si no se completa.

### NFR-07 — Operabilidad
- El sistema debe soportar la carga manual de datos desde el panel del vendedor.
- La migración inicial de datos existentes debe poder realizarse sin intervención técnica directa.
- Los KPIs básicos deben calcularse en tiempo real sin procesos batch complejos.

### NFR-08 — Alcance geográfico y monetario
- El MVP opera únicamente en **Guatemala** con moneda **GTQ**.
- El modelo no debe cerrar conceptualmente la puerta a otros países o monedas en versiones futuras.