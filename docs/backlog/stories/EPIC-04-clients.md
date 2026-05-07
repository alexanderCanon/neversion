# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 👥 EPIC-04 — Gestión de Clientes
Esta épica centraliza todas las funciones necesarias para que el vendedor administre su base de clientes, tanto los registrados vía tienda como los cargados manualmente.

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-029** | Listar clientes | Alta |
| **US-030** | Ver detalle de cliente | Alta |
| **US-031** | Crear cliente manualmente | Media |
| **US-032** | Editar datos de cliente | Media |

---

### 🛠️ US-029 — Listar clientes
**Como** Vendedor, **necesito** ver el listado de mis clientes, **para** tener visibilidad de quiénes operan en mi negocio y facilitar su gestión.

#### ✅ Criterios de Aceptación
- [ ] El listado muestra únicamente los clientes vinculados al `vendor_id` del vendedor autenticado.
- [ ] Por cada cliente se visualiza: nombre, teléfono, correo, fecha de registro y cantidad de suscripciones activas.
- [ ] El sistema permite realizar búsquedas rápidas por `nombre`, `teléfono` o `correo`.

---

### 🛠️ US-030 — Ver detalle de cliente
**Como** Vendedor, **necesito** ver el detalle completo de un cliente específico, **para** conocer su historial de compras y el estado de sus suscripciones actuales.

#### ✅ Criterios de Aceptación
- [ ] Se muestran todos los datos básicos del perfil del cliente.
- [ ] Se listan sus suscripciones activas, incluyendo servicio, perfil asignado y fecha de vencimiento.
- [ ] Se incluye el historial completo de órdenes con su estado y fecha de creación.
- [ ] El sistema retorna un error `403 Forbidden` si el vendedor intenta acceder a un cliente que no le pertenece.

---

### 🛠️ US-031 — Crear cliente manualmente
**Como** Vendedor, **necesito** poder registrar un cliente manualmente desde el panel, **para** incorporar a aquellos que llegan por canales externos (ej: WhatsApp).

> [!IMPORTANT]
> Decisión vigente ADR-09: crear un cliente manual no crea obligatoriamente una identidad autenticable en `users` ni una cuenta Supabase. El backend persiste `clients`, lo vincula al vendedor y registra `CLIENT_WELCOME` en `notification_log`; la creación de acceso autenticable queda para el flujo de registro/Supabase cuando aplique.

#### ✅ Criterios de Aceptación
- [ ] Se crea un registro en la tabla `clients` vinculado al `vendor_id` del vendedor autenticado.
- [ ] Validación de campos requeridos: `nombre`, `correo`.
- [ ] Soporte para campos opcionales: `teléfono`, `notas`.
- [ ] Se registra una notificación `CLIENT_WELCOME` para procesamiento posterior.
- [ ] No se generan contraseñas desde el backend.
- [ ] El sistema retorna un error `400 Bad Request` si el correo electrónico ya existe en la plataforma.

---

### 🛠️ US-032 — Editar datos básicos de cliente
**Como** Vendedor, **necesito** poder editar la información de contacto de mis clientes, **para** mantener mi base de datos actualizada.

#### ✅ Criterios de Aceptación
- [ ] Los campos editables son: `nombre`, `teléfono`, `notas`.
- [ ] El campo `correo` **no es editable** una vez creado el registro (por su vinculación con la identidad de acceso).
- [ ] El sistema retorna un error `403 Forbidden` si el vendedor intenta editar un cliente ajeno.
