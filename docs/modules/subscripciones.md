# Suscripciones

## Qué es

La **Suscripción** (`subscriptions`) es el vínculo activo entre un Cliente y un Perfil específico, controlando su ventana de acceso y su ciclo de pago. Es la entidad central del modelo de negocio de Neversion.

La suscripción define:
- **Quién** accede (`client_id`)
- **A qué perfil** (`profile_id`)
- **Desde cuándo** (`start_date`)
- **Hasta cuándo / cuándo debe pagar** (`payment_due_date`)
- **Cuántos meses lleva pagados** (`months_paid`)

Los trabajos de automatización en background (n8n) monitorean `payment_due_date` continuamente para enviar recordatorios de renovación.

---

## Endpoints vigentes

Base path: `/api/v1/subscriptions`

### Listar suscripciones

```
GET /api/v1/subscriptions
```

| Param | Tipo | Req. | Descripción |
|---|---|---|---|
| `status` | `string` | No | Filtrar por estado (`ACTIVE`, `EXPIRED`, `CANCELLED`, `SUSPENDED`) |
| `clientId` | `string (uuid)` | No | Filtrar por cliente |
| `profileId` | `string (uuid)` | No | Filtrar por perfil |

**Response `200 OK`** — Array de `SubscriptionResponse`

---

### Obtener suscripción por ID

```
GET /api/v1/subscriptions/{id}
```

| Código | Descripción |
|---|---|
| `200` | Encontrada |
| `404` | No encontrada |

---

### Crear suscripción

```
POST /api/v1/subscriptions
```

El Admin asigna un Perfil a un Cliente. Valida exclusividad del perfil (BR-04) antes de guardar.

**Request Body:**

```json
{
  "profileId": "uuid",
  "clientId": "uuid",
  "accountId": "uuid",
  "startDate": "2026-03-15",
  "paymentDueDate": "2026-04-15",
  "notes": "Premium plan"
}
```

| Campo | Tipo | Req. | Descripción |
|---|---|---|---|
| `profileId` | `string (uuid)` | Sí | Perfil objetivo |
| `clientId` | `string (uuid)` | Sí | Cliente a asignar |
| `accountId` | `string (uuid)` | Sí | Cuenta padre del perfil |
| `startDate` | `string (date)` | No | Inicio de acceso (default: hoy) |
| `paymentDueDate` | `string (date)` | Sí | Fecha de renovación / vencimiento de pago |
| `notes` | `string` | No | Notas del admin |

| Código | Descripción |
|---|---|
| `201` | Suscripción creada |
| `400` | Request inválido |
| `404` | Cuenta o perfil no encontrado |
| `409` | Conflicto — el perfil ya tiene una suscripción activa (BR-04) |

---

### Cancelar suscripción

```
PUT /api/v1/subscriptions/{id}/cancel
```

Transiciona a `CANCELLED`. Activado por el Admin. Fin de la relación comercial.

| Código | Descripción |
|---|---|
| `200` | Cancelada |
| `400` | Ya estaba cancelada |
| `404` | No encontrada |

---

### Suspender suscripción

```
PUT /api/v1/subscriptions/{id}/suspend
```

Transiciona de `ACTIVE` a `SUSPENDED`. Activado por el Admin por violación de políticas de uso. Puede reactivarse.

| Código | Descripción |
|---|---|
| `200` | Suspendida |
| `400` | No está en estado ACTIVE |
| `404` | No encontrada |

---

### Modelo de respuesta — `SubscriptionResponse`

```json
{
  "id": "uuid",
  "accountId": "uuid",
  "clientId": "uuid",
  "profileId": "uuid",
  "status": "ACTIVE",
  "startDate": "2026-03-15",
  "paymentDueDate": "2026-04-15",
  "monthsPaid": 1,
  "notes": "Premium plan",
  "createdAt": "2026-03-15T10:00:00"
}
```

`status` valores: `ACTIVE`, `EXPIRED`, `CANCELLED`, `SUSPENDED`

> **Nota:** En el API los valores se retornan en `UPPER_SNAKE_CASE`. En la BD se almacenan en `lowercase`. El Application Service realiza la conversión. `EXPIRING_SOON` es calculado en tiempo de consulta y **nunca se persiste** en la BD ni se retorna por este endpoint. Solo aparece en respuestas del Dashboard.

---

### Endpoint deprecado

```
~~GET /api/v1/subscriptions/dashboard~~
```

> ⚠️ **Deprecado** — Fecha de remoción: **2026-04-30**
> Reemplazado por los endpoints de `GET /api/v1/dashboard`. No usar en nuevas funcionalidades.

---

## Esquema de base de datos

Tabla: `subscriptions`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `BIGINT` PK (secuencia) | Identificador interno |
| `uuid` | `UUID` UK | Identificador externo expuesto en el API |
| `client_id` | `BIGINT` FK → `clients.id` | Cliente suscriptor |
| `profile_id` | `BIGINT` FK → `profiles.id` | Perfil asignado |
| `start_date` | `DATE` NOT NULL DEFAULT `CURRENT_DATE` | Inicio del acceso |
| `payment_due_date` | `DATE` NOT NULL | Fecha crítica monitoreada por automatizaciones |
| `months_paid` | `BIGINT` NOT NULL DEFAULT `1` | Profundidad en el ciclo de pagos |
| `status` | `VARCHAR(20)` DEFAULT `'active'` | `active`, `pending`, `suspended`, `cancelled` |
| `notes` | `TEXT` | Notas del admin |
| `created_at` | `TIMESTAMPTZ` NOT NULL | |

> Ver esquema completo en [`../system/schema.md`](../system/schema.md)

---

## Reglas de negocio

- **BR-04 — Exclusividad de perfil:** Un Perfil solo puede tener una suscripción activa a la vez. Intentar asignar otro cliente al mismo perfil activo retorna `409 Conflict`. Enforced via `UNIQUE(profile_id, status) DEFERRABLE` en la BD.
- **BR-08 — Múltiples suscripciones activas:** Un Cliente puede tener múltiples suscripciones activas simultáneas en distintos servicios.
- **BR-10 — Gestión de vencidos:** Si `payment_due_date` vence sin renovación, el cronjob registra `overdue` en `notification_log`. El Admin puede cambiar el estado a `SUSPENDED` manualmente.
- **BR-11 — Terminación vs. suspensión:**
  - `CANCELLED`: El cliente no pagó o el pago fue inválido. Fin de la relación comercial.
  - `SUSPENDED`: El cliente violó las políticas de uso (ej. compartir acceso). Puede reactivarse.
- **`pending`:** Estado transitorio cuando el cliente debe renovar pero aún no ha pagado. Monitoreado por n8n junto con `active`.

---

## Dependencias

| Módulo | Relación |
|---|---|
| [`clientes.md`](clientes.md) | Un cliente tiene muchas suscripciones (1:N) |
| [`perfiles.md`](perfiles.md) | Un perfil tiene una suscripción activa máxima (1:1 activa) |
| [`ordenes.md`](ordenes.md) | Validar una orden abre la puerta a activar/extender suscripciones |

---

## ⚠️ Notas para Claude Code

El esquema de base de datos usa Flyway con campos varchar. No existen enums a nivel de base de datos. Los enums presentes en el código Java son de la v1 y están deprecados para el esquema actual — no usarlos como referencia para migraciones ni queries.

- `status` es `varchar`. Los valores almacenados en BD son **`lowercase`**: `active`, `pending`, `suspended`, `cancelled`. El API los expone en `UPPERCASE`. El Application Service hace la conversión. `EXPIRING_SOON` **no persiste** y nunca se escribe en la tabla.
- `payment_due_date` es el campo crítico para las automatizaciones de n8n. No renombrar ni eliminar.
- El cálculo de `EXPIRING_SOON` se hace en el Application Service comparando `payment_due_date` con la fecha actual + 7 días.
- `client_id` y `profile_id` son `BIGINT` en la BD. El API los expone como UUID via la columna `uuid` de cada tabla.
- La BD tiene la columna `notes TEXT` — incluir en Request/Response DTOs si se requiere.
- El constraint `UNIQUE(profile_id, status) DEFERRABLE` permite hacer reasignaciones dentro de una sola transacción (deferred), pero bloquea duplicados al commit.
- El endpoint `GET /api/v1/subscriptions/dashboard` está marcado para remoción el **2026-04-30**. No implementar lógica nueva sobre él.
