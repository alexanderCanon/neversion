# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 📑 EPIC-05 — Órdenes y Comprobantes
Esta épica cubre el flujo transaccional desde que el cliente reserva un servicio hasta que el vendedor valida el pago y genera la orden definitiva.

> [!IMPORTANT]
> Estado backend vigente: checkout valida disponibilidad, pero no bloquea perfiles específicos. La asignación real del perfil o cuenta completa ocurre en EPIC-06. Al aprobar un comprobante, la reservación pasa a `VALIDATED` y la orden nace en `VALIDATED`, lista para asignación.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-033** | Crear reservación | Alta |
| **US-034** | Subir comprobante de pago | Alta |
| **US-035** | Aprobar comprobante y crear orden | Alta |
| **US-036** | Rechazar comprobante | Media |
| **US-037** | Listar órdenes pendientes | Alta |
| **US-038** | Ver detalle de orden | Alta |

---

### 🛠️ US-033 — Crear reservación
**Como** Cliente, **necesito** poder crear una reservación al seleccionar servicios en la tienda, **para** asegurar la disponibilidad del inventario durante mi proceso de pago y subida de comprobante.

#### ✅ Criterios de Aceptación
- [ ] Se crea un registro en la tabla `reservations` vinculado al `client_id` y `vendor_id` correspondiente.
- [ ] Se crean registros en `reservation_details` por cada servicio seleccionado en el carrito.
- [ ] El descuento por combo se calcula automáticamente basándose en la configuración del vendedor.
- [ ] La reservación inicia en estado `pending`.
- [ ] El sistema expira automáticamente la reservación después de **1 hora** si no se ha subido un comprobante.
- [ ] El sistema retorna un error `400 Bad Request` si no hay perfiles disponibles para alguno de los servicios seleccionados al momento de intentar reservar.
- [ ] No se asignan ni bloquean perfiles concretos durante checkout; la selección del recurso ocurre en EPIC-06.

---

### 🛠️ US-034 — Subir comprobante de pago
**Como** Cliente, **necesito** poder subir mi comprobante de pago vinculado a una reservación activa, **para** que el vendedor pueda validarlo y procesar mi orden.

#### ✅ Criterios de Aceptación
- [ ] Se actualiza el campo `receipt_url` en la reservación con la URL del archivo subido.
- [ ] La reservación transita al estado `uploaded`.
- [ ] El vendedor recibe una notificación por correo electrónico indicando que hay un nuevo comprobante pendiente de revisión.
- [ ] El sistema retorna un error `400 Bad Request` si la reservación ya expiró o se encuentra en un estado terminal (`cancelled`, `validated`).

---

### 🛠️ US-035 — Aprobar comprobante y crear orden
**Como** Vendedor, **necesito** poder aprobar el comprobante de una reservación, **para** generar la orden de venta persistente y proceder con la entrega de accesos.

#### ✅ Criterios de Aceptación
- [ ] Se crea un registro en la tabla `orders` vinculado a la `reservation_id`, `client_id` y `vendor_id`.
- [ ] La nueva orden inicia en estado `validated`.
- [ ] La reservación origen transita al estado `validated`.
- [ ] El cliente recibe un correo electrónico confirmando que su pago fue aprobado satisfactoriamente.
- [ ] El campo `approved_at` queda registrado en la orden con la fecha y hora actual.
- [ ] El sistema retorna un error `400 Bad Request` si la reservación no se encuentra en estado `uploaded`.

---

### 🛠️ US-036 — Rechazar comprobante
**Como** Vendedor, **necesito** poder rechazar un comprobante inválido o erróneo, **para** notificar al cliente y liberar el inventario reservado.

#### ✅ Criterios de Aceptación
- [ ] La reservación transita al estado `rejected`.
- [ ] El cliente recibe un correo electrónico notificando el rechazo junto con las observaciones o motivos proporcionados por el vendedor.
- [ ] No se liberan perfiles concretos porque checkout no bloquea inventario específico.
- [ ] El sistema retorna un error `400 Bad Request` si la reservación no se encuentra en estado `uploaded`.

---

### 🛠️ US-037 — Listar órdenes pendientes
**Como** Vendedor, **necesito** ver el listado de órdenes que requieren mi gestión, **para** priorizar la entrega de accesos y mantener la eficiencia operativa.

#### ✅ Criterios de Aceptación
- [ ] El listado permite filtrar las órdenes del vendedor autenticado por estado; para asignación, el estado operativo pendiente es `validated`.
- [ ] Se visualiza: información del cliente, servicios solicitados, monto total, método de pago y fecha de creación.
- [ ] Las órdenes aparecen ordenadas por fecha de creación ascendente (las más antiguas primero para asegurar el orden de llegada).

---

### 🛠️ US-038 — Ver detalle de orden
**Como** Vendedor, **necesito** ver el detalle completo de una orden específica, **para** revisar los pormenores de la transacción antes de completar la entrega.

#### ✅ Criterios de Aceptación
- [ ] Se muestran todos los datos maestros de la orden y su reservación de origen.
- [ ] Se visualiza el comprobante de pago adjunto para una revisión final si fuera necesario.
- [ ] Se muestra el historial cronológico de cambios de estado de la orden.
- [ ] El sistema retorna un error `403 Forbidden` si el vendedor intenta acceder a una orden que no le pertenece.
