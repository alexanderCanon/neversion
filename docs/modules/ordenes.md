# Órdenes

## Qué es

La **Orden** (`orders`) es un registro persistente y permanente que se crea exclusivamente cuando un Admin valida manualmente el comprobante de pago de una Reservación. Es el paso final del flujo de checkout.

Una orden no puede crearse directamente — depende estrictamente de la validación de una Reservación en estado `UPLOADED`.

> Ver el flujo completo de checkout en [`reservaciones.md`](reservaciones.md).

---

## Endpoints vigentes

Base path: `/api/v1/orders`

### Obtener orden por ID

```
GET /api/v1/orders/{id}
```

| Código | Descripción |
|---|---|
| `200` | Encontrada |
| `404` | No encontrada |

---

### Obtener orden por ID de reservación

```
GET /api/v1/orders/by-reservation/{reservationId}
```

Útil para verificar si una reservación ya generó una orden.

| Código | Descripción |
|---|---|
| `200` | Encontrada |
| `404` | No existe orden vinculada a esta reservación |

---

### Modelo de respuesta — `OrderResponse`

```json
{
  "id": "uuid",
  "reservationId": "uuid",
  "status": "PENDING",
  "notes": "Payment confirmed via transfer",
  "createdAt": "2026-03-24T00:00:00Z"
}
```

`status` valores: `PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED`

---

## Esquema de base de datos

Tabla: `orders`

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `UUID` PK DEFAULT `gen_random_uuid()` | Identificador único (no BIGINT — UUID directo) |
| `reservation_id` | `UUID` FK → `reservations.id` | Reservación que originó esta orden |
| `status` | `VARCHAR(20)` NOT NULL DEFAULT `'PENDING'` | `PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED` |
| `notes` | `TEXT` | Notas del admin al validar |
| `created_at` | `TIMESTAMPTZ` NOT NULL DEFAULT `now()` | |

> Ver esquema completo en [`../system/schema.md`](../system/schema.md)

---

## Reglas de negocio

- **BR-20 — Transición Reservación → Orden:** Una Orden solo puede generarse cuando un Admin valida manualmente una Reservación en estado `UPLOADED` (scope Sprint 1).
- La Orden es el único punto inmutable del flujo de checkout — no se puede modificar una vez creada, solo cambiar su `status`.
- Una Reservación genera máximo una Orden (`RESERVATIONS → ORDERS` es 1:1).
- Validar una Reservación crea la Orden **y** abre la puerta para activar o extender suscripciones vinculadas.

---

## Dependencias

| Módulo | Relación |
|---|---|
| [`reservaciones.md`](reservaciones.md) | Una reservación genera una orden al validarse (1:1) |
| [`subscripciones.md`](subscripciones.md) | Validar una orden habilita activar/extender suscripciones |

---

## ⚠️ Notas para Claude Code

El esquema de base de datos usa Flyway con campos varchar. No existen enums a nivel de base de datos. Los enums presentes en el código Java son de la v1 y están deprecados para el esquema actual — no usarlos como referencia para migraciones ni queries.

- `id` y `reservation_id` son `UUID` (PK directos, **sin** columna `uuid` separada — no siguen el patrón BIGINT+uuid que usan clients/accounts/profiles).
- `status` es `varchar` con DEFAULT `'PENDING'`. Valores válidos: `PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED` (en UPPERCASE en la BD).
- Las órdenes no tienen endpoint de creación directa — se crean internamente al ejecutar `PUT /api/v1/reservations/{id}/validate`.
- No existe endpoint de listado de órdenes en Sprint 1 — se consultan por ID o por reservación.
