# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 📧 EPIC-08 — Notificaciones por Correo
Esta épica centraliza todas las comunicaciones automáticas vía email que el sistema envía a los clientes en los diferentes hitos de su ciclo de vida y proceso de compra.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-049** | Notificación de registro exitoso | Baja |
| **US-050** | Notificación de orden creada | Baja |
| **US-051** | Notificación de comprobante recibido | Media |
| **US-052** | Notificación de pago aprobado | Media |
| **US-053** | Notificación de accesos enviados | Alta |
| **US-054** | Recordatorio de renovación (7d, 3d, 1d) | Alta |
| **US-055** | Notificación de suscripción vencida | Media |
| **US-056** | Notificación de renovación confirmada | Media |

---

### 🛠️ US-049 — Notificación de registro exitoso
**Como** sistema, **necesito** enviar un correo de bienvenida al cliente cuando se registra satisfactoriamente, **para** confirmar su acceso a la plataforma.

#### ✅ Criterios de Aceptación
- [ ] El cliente recibe un correo con su nombre y la confirmación de registro.
- [ ] El envío queda registrado en `notification_log` con `entity_type: 'client'` y `stage: 'welcome'`.
- [ ] Un error en el envío del correo **no debe bloquear** el proceso de registro del cliente.

---

### 🛠️ US-050 — Notificación de orden creada
**Como** sistema, **necesito** enviar un correo al cliente cuando su orden es generada, **para** confirmar que su solicitud ha sido recibida correctamente.

#### ✅ Criterios de Aceptación
- [ ] El cliente recibe un correo con el resumen de servicios solicitados y el total a pagar.
- [ ] El envío queda registrado en `notification_log` con `entity_type: 'order'` y `stage: 'created'`.
- [ ] Un error en el envío del correo **no debe bloquear** la creación de la orden.

---

### 🛠️ US-051 — Notificación de comprobante recibido
**Como** sistema, **necesito** enviar un correo al cliente tras la subida de su comprobante, **para** confirmar que la transacción está en proceso de revisión.

#### ✅ Criterios de Aceptación
- [ ] El cliente recibe un correo confirmando que el comprobante fue recibido y está pendiente de validación por el vendedor.
- [ ] El envío queda registrado en `notification_log` con `entity_type: 'order'` y `stage: 'receipt_uploaded'`.
- [ ] Un error en el envío del correo **no debe bloquear** la actualización del estado de la reservación.

---

### 🛠️ US-052 — Notificación de pago aprobado
**Como** sistema, **necesito** enviar un correo al cliente cuando su pago es validado, **para** mantenerlo informado sobre el progreso de su pedido.

#### ✅ Criterios de Aceptación
- [ ] El cliente recibe un correo confirmando la aprobación satisfactoria de su pago.
- [ ] El envío queda registrado en `notification_log` con `entity_type: 'order'` y `stage: 'approved'`.
- [ ] Un error en el envío del correo **no debe bloquear** la generación de la orden interna.

---

### 🛠️ US-053 — Notificación de accesos enviados
**Como** sistema, **necesito** enviar un correo con las credenciales asignadas una vez confirmada la asignación, **para** que el cliente pueda utilizar el servicio de inmediato.

#### ✅ Criterios de Aceptación
- [ ] El correo incluye: nombre del servicio, email de la cuenta, contraseña, nombre del perfil, PIN (si aplica) y fecha de vencimiento.
- [ ] El envío queda registrado en `notification_log` con `entity_type: 'subscription'` y `stage: 'access_delivered'`.
- [ ] Un error en el envío del correo **no debe bloquear** el cambio de estado de la orden ni de la suscripción.

---

### 🛠️ US-054 — Recordatorio de renovación (7d, 3d, 1d)
**Como** sistema, **necesito** enviar recordatorios automáticos antes del vencimiento de la suscripción, **para** incentivar la renovación y reducir la pérdida de clientes.

#### ✅ Criterios de Aceptación
- [ ] El sistema envía correos automáticos 7 días, 3 días y 1 día antes del vencimiento.
- [ ] Cada envío queda registrado en `notification_log` con `entity_type: 'subscription'` y los stages correspondientes (`7d`, `3d`, `1d`).
- [ ] No se envía ningún recordatorio si la suscripción ya ha sido renovada satisfactoriamente.
- [ ] El sistema evita envíos duplicados verificando si el stage ya existe en el `notification_log` para el periodo actual.

---

### 🛠️ US-055 — Notificación de suscripción vencida
**Como** sistema, **necesito** enviar un correo al cliente el día que su suscripción expira sin renovación, **para** informarle sobre la suspensión de su acceso.

#### ✅ Criterios de Aceptación
- [ ] El cliente recibe un correo el mismo día del vencimiento si no se ha detectado una renovación.
- [ ] El envío queda registrado en `notification_log` con `entity_type: 'subscription'` y `stage: 'due'`.
- [ ] No se envía el correo si la suscripción fue renovada el mismo día del vencimiento antes de la ejecución del proceso.

---

### 🛠️ US-056 — Notificación de renovación confirmada
**Como** sistema, **necesito** enviar un correo al cliente tras una renovación exitosa, **para** confirmar la nueva fecha de vigencia del servicio.

#### ✅ Criterios de Aceptación
- [ ] El cliente recibe un correo con la nueva fecha de vencimiento y el resumen de su renovación.
- [ ] El envío queda registrado en `notification_log` con `entity_type: 'subscription'` y `stage: 'renewed'`.
- [ ] Un error en el envío del correo **no debe bloquear** el proceso de renovación de la suscripción en la base de datos.