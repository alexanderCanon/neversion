# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 📱 EPIC-09 — Panel del Cliente
Esta épica define la experiencia de autoservicio para el cliente final, permitiéndole gestionar sus suscripciones, accesos e historial de compras.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-057** | Ver suscripciones activas | Alta |
| **US-058** | Ver accesos asignados | Alta |
| **US-059** | Ver historial de órdenes | Media |
| **US-060** | Ver estado de comprobante | Media |
| **US-061** | Iniciar renovación desde panel | Alta |
| **US-062** | Editar datos de perfil | Baja |

---

### 🛠️ US-057 — Ver suscripciones activas
**Como** Cliente, **necesito** ver mis suscripciones activas desde mi panel, **para** saber qué servicios tengo contratados y cuándo es su próxima fecha de vencimiento.

#### ✅ Criterios de Aceptación
- [ ] El listado muestra únicamente las suscripciones vinculadas al cliente autenticado.
- [ ] Se visualiza: nombre del servicio, perfil asignado, fecha de vencimiento y estado.
- [ ] Las suscripciones aparecen ordenadas de forma ascendente por fecha de vencimiento.
- [ ] El sistema retorna un error `403 Forbidden` si se intenta acceder a suscripciones de terceros.

---

### 🛠️ US-058 — Ver accesos asignados
**Como** Cliente, **necesito** poder consultar mis credenciales de acceso desde mi panel en cualquier momento, **para** tener autonomía y no depender exclusivamente del correo electrónico.

#### ✅ Criterios de Aceptación
- [ ] Por cada suscripción activa, el sistema muestra: nombre del servicio, email de la cuenta, contraseña, nombre del perfil y PIN (si aplica).
- [ ] La información es visible únicamente para el propietario de la suscripción.
- [ ] El sistema retorna un error `403 Forbidden` en caso de intento de acceso no autorizado.

---

### 🛠️ US-059 — Ver historial de órdenes
**Como** Cliente, **necesito** ver el historial completo de mis transacciones, **para** tener trazabilidad de mis compras y pagos realizados.

#### ✅ Criterios de Aceptación
- [ ] El listado muestra todas las órdenes históricas del cliente autenticado.
- [ ] Se visualiza: fecha de creación, servicios solicitados, monto total y estado de la orden.
- [ ] Las órdenes aparecen ordenadas por fecha de creación descendente (las más recientes primero).
- [ ] El acceso está restringido únicamente al cliente propietario.

---

### 🛠️ US-060 — Ver estado de comprobante
**Como** Cliente, **necesito** poder consultar el estado de validación de mi comprobante subido, **para** saber si mi pago ya fue revisado y aprobado por el vendedor.

#### ✅ Criterios de Aceptación
- [ ] El sistema muestra el estado actual de la reservación vinculada a la orden en curso.
- [ ] Los estados visibles son: `pending`, `uploaded`, `validated`, `rejected`, `expired`, `cancelled`.
- [ ] En caso de que el comprobante sea rechazado (`rejected`), se muestran las observaciones o motivos ingresados por el vendedor.

---

### 🛠️ US-061 — Iniciar renovación de suscripción
**Como** Cliente, **necesito** poder iniciar el proceso de renovación de una suscripción desde mi propio panel, **para** extender mi acceso de forma rápida y autónoma.

#### ✅ Criterios de Aceptación
- [ ] El cliente puede generar una nueva reservación vinculada a una suscripción existente (`active` o `suspended`).
- [ ] El flujo de renovación sigue el mismo estándar que una compra nueva: reservación ➔ subida de comprobante ➔ aprobación del vendedor ➔ actualización de suscripción.
- [ ] El sistema retorna un error `400 Bad Request` si ya existe una reservación activa o pendiente para la misma suscripción.

---

### 🛠️ US-062 — Editar datos básicos del perfil
**Como** Cliente, **necesito** poder editar mis datos personales básicos, **para** mantener mi información de contacto actualizada en el sistema.

#### ✅ Criterios de Aceptación
- [ ] Los campos editables son: `nombre` y `teléfono`.
- [ ] El campo `correo` **no es editable** (está vinculado a la identidad de autenticación externa).
- [ ] Los cambios se reflejan de forma inmediata en el perfil del cliente y en los registros del vendedor.
