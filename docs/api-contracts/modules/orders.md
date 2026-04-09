# API Contract — Orders

> An Order is a persistent record created once a Reservation payment is validated.
> All enums referenced here are defined and governed by `docs/enums.md`.

---

## Endpoints

### Get order by ID

```
GET /api/v1/orders/{id}
```

| Code | Description |
|---|---|
| `200` | Found |
| `404` | Not found |

---

### Get order by reservation ID

```
GET /api/v1/orders/by-reservation/{reservationId}
```

| Code | Description |
|---|---|
| `200` | Found |
| `404` | No order linked to this reservation |

---

## Models

### OrderResponse

```json
{
  "id": "uuid",
  "reservationId": "uuid",
  "status": "PENDING",
  "notes": "Payment confirmed via transfer",
  "createdAt": "2026-03-24T00:00:00Z"
}
```

`status` values: `PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED` — see `enums.md`.
