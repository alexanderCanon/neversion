# D.2 — User Stories

Este documento detalla las historias de usuario que componen el backlog del proyecto **Neversion**, organizadas por épicas.

## 🏗️ EPIC-00 — Foundation
Esta épica se centra en la normalización de la base de datos y la creación de las estructuras fundamentales para soportar el aislamiento entre vendedores (Multitenancy).

### 📋 Resumen de Historias
| ID | Título | Prioridad |
| :--- | :--- | :--- |
| **US-001** | Crear tabla `users` con roles | Alta |
| **US-002** | Crear tabla `vendors` | Alta |
| **US-003** | Vincular `clients` a users y vendors | Alta |
| **US-004** | Vincular `services` a vendors | Alta |
| **US-005** | Normalizar `services` (Pricing) | Media |
| **US-006** | Completar tabla `accounts` | Media |
| **US-007** | Agregar status a `profiles` | Media |
| **US-008** | Completar tabla `subscriptions` | Media |
| **US-009** | Completar tabla `orders` | Media |
| **US-010** | Vincular `reservations` a vendors | Media |
| **US-011** | Vincular `reservation_details` a services | Media |

---

### 🛠️ US-001 — Crear tabla users con roles
**Como** sistema, **necesito** una tabla `users` que vincule el usuario autenticado externamente con su rol interno, **para** que el control de acceso funcione correctamente.

#### ✅ Criterios de Aceptación
- [x] Existe la tabla `users` con los campos: `id`, `uuid`, `external_id`, `role`, `created_at`.
- [x] El campo `role` acepta los valores: `super_admin`, `vendedor`, `cliente`.
- [x] El campo `external_id` permite la vinculación con el proveedor de autenticación externo.
- [x] Migración Flyway creada y aplicada satisfactoriamente.

---

### 🛠️ US-002 — Crear tabla vendors
**Como** sistema, **necesito** una tabla `vendors` vinculada a un usuario con rol vendedor, **para** que cada negocio opere de forma independiente dentro del sistema.

#### ✅ Criterios de Aceptación
- [x] Existe la tabla `vendors` con los campos: `id`, `uuid`, `user_id`, `store_name`, `logo_url`, `bank_details`, `discount_cfg`, `created_at`.
- [x] El campo `user_id` referencia correctamente a la tabla `users`.
- [x] Migración Flyway creada y aplicada satisfactoriamente.

---

### 🛠️ US-003 — Vincular clients a users y vendors
**Como** sistema, **necesito** que cada cliente esté vinculado a un usuario autenticado y a un vendedor específico, **para** mantener el aislamiento entre negocios.

> [!NOTE]
> Decisión vigente tras ADR-09: `clients.user_id` puede ser nulo para clientes creados manualmente desde el panel del vendedor. La identidad de acceso se gestiona con Supabase Auth y se vincula al backend mediante `users.external_id` cuando existe cuenta autenticable.

#### ✅ Criterios de Aceptación
- [x] La tabla `clients` incluye la columna `user_id` referenciando a `users`.
- [x] La tabla `clients` incluye la columna `vendor_id` referenciando a `vendors`.
- [x] Migración Flyway creada y aplicada satisfactoriamente.
- [x] Los datos existentes no se pierden durante la migración.

---

### 🛠️ US-004 — Vincular services a vendors
**Como** sistema, **necesito** que cada servicio pertenezca a un vendedor específico, **para** que cada negocio gestione su propio catálogo de forma independiente.

#### ✅ Criterios de Aceptación
- [x] La tabla `services` incluye la columna `vendor_id` referenciando a `vendors`.
- [x] Migración Flyway creada y aplicada satisfactoriamente.
- [x] Los datos existentes no se pierden durante la migración.

---

### 🛠️ US-005 — Normalizar services (Pricing)
**Como** sistema, **necesito** que el pricing de los servicios esté en columnas explícitas, **para** poder realizar consultas, validaciones y cálculos sin depender de `JSONB`.

#### ✅ Criterios de Aceptación
- [x] La tabla `services` incluye las columnas: `price_profile`, `price_full`, `duration_days`, `is_active`, `description`, `image_url`.
- [x] El campo `details` (JSONB) se mantiene únicamente para metadata no estructurada.
- [x] Migración Flyway creada y aplicada satisfactoriamente.
- [x] Los datos existentes se migran correctamente desde el anterior campo `JSONB`.

---

### 🛠️ US-006 — Completar tabla accounts
**Como** sistema, **necesito** que las cuentas registren costo de adquisición, fuente, fecha de compra y estado, **para** tener trazabilidad operativa completa.

#### ✅ Criterios de Aceptación
- [x] La tabla `accounts` incluye las columnas: `cost`, `source`, `purchased_at`, `status`, `vendor_id`.
- [x] El campo `status` acepta los valores: `available`, `partial`, `full`, `expired`.
- [x] Migración Flyway creada y aplicada satisfactoriamente.
- [x] Los datos existentes no se pierden durante la migración.

---

### 🛠️ US-007 — Agregar status a profiles
**Como** sistema, **necesito** que cada perfil tenga un estado operativo explícito, **para** controlar la disponibilidad y asignación correctamente.

#### ✅ Criterios de Aceptación
- [x] La tabla `profiles` incluye la columna `status`.
- [x] El campo `status` acepta los valores: `available`, `reserved`, `active`, `expired`, `blocked`.
- [x] Migración Flyway creada y aplicada satisfactoriamente.
- [x] Los perfiles existentes se inicializan por defecto en el estado `available`.

---

### 🛠️ US-008 — Completar tabla subscriptions
**Como** sistema, **necesito** que las suscripciones registren servicio, tipo de venta, precio y orden de origen, **para** tener una trazabilidad financiera completa.

> [!IMPORTANT]
> Estado backend vigente hasta EPIC-06: `subscriptions` conserva `payment_due_date` y agrega `order_id` + `end_date`. Los campos financieros denormalizados (`service_id`, `sale_mode`, `price_sold`, `discount_applied`) quedan pendientes para EPIC-07 o un saneamiento de esquema posterior. No se renombra `payment_due_date` a `due_date` para evitar una migración destructiva innecesaria antes de cerrar el ciclo de suscripciones.

#### ✅ Criterios de Aceptación
- [x] La tabla `subscriptions` incluye: `vendor_id`, `order_id`, `start_date`, `end_date`, `payment_due_date`, `months_paid`, `status`, `notes`.
- [ ] Pendiente EPIC-07: evaluar y agregar `service_id`, `sale_mode`, `price_sold`, `discount_applied` si el módulo de renovaciones/KPIs los requiere como snapshot financiero.
- [x] Migraciones Flyway creadas y aplicadas satisfactoriamente.
- [x] Los datos existentes no se pierden durante la migración.

---

### 🛠️ US-009 — Completar tabla orders
**Como** sistema, **necesito** que las órdenes registren cliente, vendedor, método de pago y fecha de aprobación, **para** completar la trazabilidad del ciclo comercial.

#### ✅ Criterios de Aceptación
- [x] La tabla `orders` incluye las columnas: `client_id`, `vendor_id`, `payment_method`, `approved_at`.
- [x] Migración Flyway creada y aplicada satisfactoriamente.
- [x] Los datos existentes no se pierden durante la migración.

---

### 🛠️ US-010 — Vincular reservations a vendors
**Como** sistema, **necesito** que cada reserva esté vinculada a un vendedor, **para** mantener el aislamiento entre negocios desde el inicio del flujo de compra.

#### ✅ Criterios de Aceptación
- [x] La tabla `reservations` incluye la columna `vendor_id` referenciando a `vendors`.
- [x] Migración Flyway creada y aplicada satisfactoriamente.
- [x] Los datos existentes no se pierden durante la migración.

---

### 🛠️ US-011 — Vincular reservation_details a services
**Como** sistema, **necesito** que cada línea de detalle de reserva referencie explícitamente un servicio, **para** calcular precios y disponibilidad correctamente.

#### ✅ Criterios de Aceptación
- [x] La tabla `reservation_details` incluye la columna `service_id` referenciando a `services`.
- [x] Migración Flyway creada y aplicada satisfactoriamente.
- [x] Los datos existentes no se pierden durante la migración.
