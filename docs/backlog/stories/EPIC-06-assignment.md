# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 🔑 EPIC-06 — Asignación y Entrega de Accesos
Esta épica cubre el flujo operativo donde el vendedor asigna los perfiles o cuentas adquiridas por el cliente y se notifican las credenciales de acceso.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-039** | Sugerir perfil o cuenta disponible | Alta |
| **US-040** | Confirmar asignación | Alta |
| **US-041** | Entregar accesos al cliente | Alta |
| **US-042** | Asignación manual desde el panel | Media |

---

### 🛠️ US-039 — Sugerir perfil o cuenta disponible
**Como** sistema, **necesito** sugerir automáticamente el perfil o cuenta disponible más adecuado para una orden aprobada, **para** reducir el trabajo manual y la carga cognitiva del vendedor.

#### ✅ Criterios de Aceptación
- [ ] El sistema identifica y sugiere un perfil en estado `available` del servicio correspondiente.
- [ ] Si la modalidad de compra es `cuenta completa`, se sugiere una cuenta maestra completa en estado `available`.
- [ ] Si no hay disponibilidad inmediata en el inventario, el sistema notifica al vendedor sin bloquear el flujo de la orden.
- [ ] La sugerencia es solo informativa; el vendedor debe validarla antes de que se realice cualquier cambio real.

---

### 🛠️ US-040 — Confirmar asignación
**Como** Vendedor, **necesito** poder confirmar o modificar la sugerencia de asignación del sistema, **para** mantener el control final sobre los recursos entregados a cada cliente.

#### ✅ Criterios de Aceptación
- [ ] El vendedor puede aceptar la sugerencia automática o elegir manualmente otro recurso disponible del inventario.
- [ ] Al confirmar la asignación, el perfil transita automáticamente al estado `active`.
- [ ] Se crea un registro en la tabla `subscriptions` vinculando cliente, perfil, servicio y la orden de origen.
- [ ] La **fecha de inicio** de la suscripción se establece como la fecha de aprobación del pago.
- [ ] La **fecha de vencimiento** se calcula automáticamente basándose en la duración configurada para el servicio.
- [ ] La orden de venta transita al estado final `completed`.
- [ ] El sistema retorna un error `400 Bad Request` si el perfil seleccionado manualmente no se encuentra en estado `available`.

---

### 🛠️ US-041 — Entregar accesos al cliente
**Como** sistema, **necesito** enviar automáticamente las credenciales asignadas al cliente por correo electrónico, **para** que pueda disfrutar del servicio sin demoras adicionales.

#### ✅ Criterios de Aceptación
- [ ] El cliente recibe un correo electrónico con: nombre del servicio, email de la cuenta, contraseña, nombre del perfil y PIN (si aplica), y la fecha de vencimiento.
- [ ] Los accesos se vuelven visibles y consultables permanentemente desde el panel del cliente.
- [ ] Cada envío de accesos queda registrado cronológicamente en el `notification_log`.
- [ ] Si el envío de correo falla, el sistema lanza una alerta controlada pero no revierte el estado `completed` de la orden.

---

### 🛠️ US-042 — Asignación manual desde el panel
**Como** Vendedor, **necesito** poder realizar una asignación directa de accesos desde mi panel administrativo, **para** dar soporte a ventas externas o migraciones rápidas.

#### ✅ Criterios de Aceptación
- [ ] El vendedor puede seleccionar manualmente un cliente, un servicio, un perfil disponible y definir las fechas de vigencia.
- [ ] El sistema crea la suscripción correspondiente omitiendo los pasos de reservación y orden.
- [ ] El perfil seleccionado cambia su estado a `active`.
- [ ] El cliente recibe sus credenciales de acceso por correo de forma automática tras la confirmación.
- [ ] Esta operación es independiente del flujo comercial de la tienda pública.