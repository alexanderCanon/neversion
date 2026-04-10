# API Contract — Reservations

> A Reservation is a temporary hold placed by a customer while completing payment.
> It expires automatically after 3600 seconds (1 hour).
> All enums referenced here are defined and governed by `docs/enums.md`.

---

## Status Flow

```
PENDING → UPLOADED → VALIDATED → (order created)
PENDING → CANCELLED
UPLOADED → CANCELLED
PENDING / UPLOADED → EXPIRED (automatic, after 1 hour)
```

---

## Endpoints

### List reservations

```
GET /api/v1/reservations
```

| Param | Type | Required | Description |
|---|---|---|---|
| `status` | `ReservStatus` | No | Filter by status |

**Response `200 OK`** — Array of `ReservationResponse`

---

### Get reservation by ID

```
GET /api/v1/reservations/{id}
```

| Code | Description |
|---|---|
| `200` | Found |
| `404` | Not found |

---

### Create reservation

```
POST /api/v1/reservations
```

**Request Body**

```json
{
  "clientId": "uuid",
  "items": [
    { "inventoryId": 1, "qty": 1 }
  ]
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `clientId` | `string (uuid)` | No | Can be attached later via `/client` endpoint |
| `items` | `array` | Yes | One or more service items |

| Code | Description |
|---|---|
| `201` | Created |
| `400` | Invalid request or insufficient stock |

---

### Upload payment receipt

```
PUT /api/v1/reservations/{id}/receipt
```

Customer uploads proof of payment. Transitions `PENDING → UPLOADED`.

**Request Body**

```json
{ "receiptUrl": "https://s3.amazonaws.com/..." }
```

| Code | Description |
|---|---|
| `200` | Receipt uploaded |
| `400` | Invalid status or duplicate URL (BR-05) |
| `404` | Not found |

---

### Validate payment and create order

```
PUT /api/v1/reservations/{id}/validate
```

Admin validates payment. Transitions `UPLOADED → VALIDATED` and creates an Order.

**Request Body**

```json
{ "notes": "Payment confirmed via transfer" }
```

| Code | Description |
|---|---|
| `200` | Validated, order created |
| `400` | Status is not UPLOADED |
| `404` | Not found |

---

### Cancel reservation

```
PUT /api/v1/reservations/{id}/cancel
```

Only `PENDING` or `UPLOADED` reservations can be cancelled.

| Code | Description |
|---|---|
| `200` | Cancelled |
| `400` | Cannot cancel in current status |
| `404` | Not found |

---

### Attach client to reservation

```
PUT /api/v1/reservations/{id}/client?clientId={uuid}
```

Links an existing client to a reservation created without one.

| Code | Description |
|---|---|
| `200` | Client attached |
| `404` | Reservation or client not found |

---

## Models

### ReservationResponse

```json
{
  "id": "uuid",
  "clientId": "uuid",
  "status": "PENDING",
  "discount": 0.00,
  "total": 40.00,
  "receiptUrl": "https://...",
  "expirationDate": "2026-03-24T01:00:00Z",
  "createdAt": "2026-03-24T00:00:00Z",
  "details": [
    {
      "id": "uuid",
      "inventoryId": 1,
      "qty": 1,
      "unitPrice": 40.00,
      "subtotal": 40.00
    }
  ]
}
```

`status` values: `PENDING`, `UPLOADED`, `VALIDATED`, `EXPIRED`, `CANCELLED` — see `enums.md`.
All monetary values are in GTQ.
