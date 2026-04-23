# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 🔄 EPIC-07 — Suscripciones
Esta épica abarca la gestión del ciclo de vida de las suscripciones activas, incluyendo renovaciones, detecciones de vencimiento y revocaciones de acceso.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-043** | Listar suscripciones | Alta |
| **US-044** | Ver detalle de suscripción | Alta |
| **US-045** | Renovar suscripción | Alta |
| **US-046** | Revocar acceso | Media |
| **US-047** | Detectar suscripciones vencidas | Alta |
| **US-048** | Crear suscripción manual | Media |

---

### 🛠️ US-043 — Listar suscripciones
**Como** Vendedor, **necesito** ver el listado completo de suscripciones de mi negocio, **para** tener visibilidad del estado operativo general y los próximos vencimientos.

#### ✅ Criterios de Aceptación
- [ ] El listado muestra todas las suscripciones vinculadas al `vendor_id` del vendedor autenticado.
- [ ] Por cada suscripción se visualiza: cliente, servicio, perfil asignado, fecha de vencimiento y estado actual.
- [ ] El sistema permite filtrar la lista por `estado` y `servicio`.
- [ ] El orden predeterminado es ascendente por **fecha de vencimiento** (las más próximas a vencer arriba).

---

### 🛠️ US-044 — Ver detalle de suscripción
**Como** Vendedor, **necesito** ver el detalle completo de una suscripción específica, **para** conocer su historial, precios pactados y el origen comercial.

#### ✅ Criterios de Aceptación
- [ ] Se muestran todos los datos maestros de la suscripción, incluyendo `precio_vendido`, `descuento_aplicado` y la orden de origen.
- [ ] Se visualiza la información del cliente y del perfil técnico asignado.
- [ ] El sistema retorna un error `403 Forbidden` si la suscripción no pertenece al vendedor autenticado.

---

### 🛠️ US-045 — Renovar suscripción
**Como** Vendedor, **necesito** poder renovar una suscripción existente, **para** extender el acceso del cliente al servicio contratado.

#### ✅ Criterios de Aceptación
- [ ] **Lógica BR-07:** Si el pago se registra dentro de los 2 días post-vencimiento, la nueva `due_date` se calcula desde la fecha de vencimiento original.
- [ ] **Lógica BR-07:** Si el pago se registra 3 días o más después del vencimiento, la nueva `due_date` se calcula desde la fecha de pago actual.
- [ ] El campo `months_paid` se incrementa en una unidad.
- [ ] La suscripción transita o se mantiene en estado `active`.
- [ ] El perfil técnico vinculado se mantiene en estado `active`.
- [ ] El cliente recibe un correo electrónico confirmando la renovación exitosa.
- [ ] El envío de la notificación queda registrado en el `notification_log`.

---

### 🛠️ US-046 — Revocar acceso
**Como** Vendedor, **necesito** poder revocar manualmente el acceso de una suscripción no renovada, **para** liberar el perfil y mantener el inventario de la cuenta actualizado.

#### ✅ Criterios de Aceptación
- [ ] La suscripción transita al estado `cancelled`.
- [ ] El perfil vinculado transita automáticamente al estado `available`.
- [ ] El cliente recibe un correo electrónico notificando la revocación del acceso al servicio.
- [ ] El envío de la notificación queda registrado en el `notification_log`.
- [ ] El sistema retorna un error `400 Bad Request` si la suscripción ya se encuentra en estado `cancelled`.

---

### 🛠️ US-047 — Detectar suscripciones vencidas
**Como** sistema, **necesito** detectar automáticamente las suscripciones que han llegado a su fecha de corte sin renovarse, **para** actualizar sus estados y alertar al vendedor.

#### ✅ Criterios de Aceptación
- [ ] El proceso identifica suscripciones con `due_date` igual a la fecha actual y estado `active`.
- [ ] La suscripción transita automáticamente al estado `suspended`.
- [ ] El perfil técnico vinculado transita al estado `expired`.
- [ ] El vendedor recibe una notificación diaria con el resumen de las suscripciones vencidas del día.
- [ ] Este proceso se ejecuta de forma automática una vez al día (cron job).

---

### 🛠️ US-048 — Crear suscripción manual
**Como** Vendedor, **necesito** poder crear una suscripción manualmente desde el panel administrativo, **para** registrar ventas externas o completar la migración de datos activos.

#### ✅ Criterios de Aceptación
- [ ] El vendedor selecciona cliente, servicio, perfil, modalidad, precio, fechas y descuentos de forma manual.
- [ ] Se crea el registro en `subscriptions` con los datos proporcionados.
- [ ] El perfil seleccionado transita al estado `active`.
- [ ] Esta operación no requiere generar una orden ni reservación previa en el sistema.
- [ ] El sistema solo envía el correo con los accesos si el vendedor marca explícitamente la opción de notificación.