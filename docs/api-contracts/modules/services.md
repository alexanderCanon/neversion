# API Contract — Services

> The `services` entity is the unified replacement for the legacy `products` + `inventory` dual-table model.
> It represents a digital platform offered by Neversion (Netflix, Spotify, etc.) and stores pricing metadata as JSONB.
> All enums referenced here are defined and governed by `docs/enums/enums.md`.

---

## Endpoints

### List all services

```
GET /api/v1/services
```

**Auth:** Public — no token required.

**Response `200 OK`** — Array of `ServiceResponse`

---

### Get service by ID

```
GET /api/v1/services/{id}
```

`{id}` is a UUID (external identifier).

**Auth:** Public — no token required.

| Code | Description |
|---|---|
| `200` | Service found |
| `404` | Not found |

---

### Create service

```
POST /api/v1/services
```

**Auth:** `ADMIN` role required.

**Request Body**

```json
{
  "name": "Netflix",
  "maxProfiles": 5,
  "details": "{\"pricing\": [{\"duration_days\": 30, \"price\": 50.00, \"currency\": \"GTQ\"}]}"
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `name` | `string` | Yes | Platform name — must be unique |
| `maxProfiles` | `integer` | No | Default profile cap per account (BR-01) |
| `details` | `string (JSON)` | No | JSONB blob — pricing tiers, currency, metadata |

| Code | Description |
|---|---|
| `201` | Created |
| `400` | Validation error or duplicate name |

---

### Update service

```
PUT /api/v1/services/{id}
```

**Auth:** `ADMIN` role required.

Same request body as create.

| Code | Description |
|---|---|
| `200` | Updated |
| `400` | Validation error |
| `404` | Not found |

---

### Delete service

```
DELETE /api/v1/services/{id}
```

**Auth:** `ADMIN` role required.

| Code | Description |
|---|---|
| `204` | Deleted |
| `404` | Not found |

---

## Models

### ServiceResponse

```json
{
  "id": "uuid",
  "name": "Netflix",
  "maxProfiles": 5,
  "details": "{\"pricing\": [{\"duration_days\": 30, \"price\": 50.00, \"currency\": \"GTQ\"}]}",
  "createdAt": "2026-04-07T10:00:00"
}
```

| Field | Type | Description |
|---|---|---|
| `id` | `UUID` | External identifier — use this in all frontend references |
| `name` | `string` | Platform display name |
| `maxProfiles` | `integer` | Default profile cap |
| `details` | `string (JSON)` | JSONB metadata serialized as string |
| `createdAt` | `datetime` | Creation timestamp |

---

## Business Rules

- **BR-17**: Service `name` must be unique across the catalog.
- **BR-18**: All admins share global access to modify services.
- **BR-01**: `maxProfiles` governs the auto-generated profile count when an Account is created with `saleMode = BY_PROFILE`.
