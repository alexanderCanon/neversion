# API Contract — Clients

> A Client is an unregistered customer who makes a purchase without creating an account.
> Primary customer type for Sprint 1. Sprint 2 introduces `Profile` (registered users via Supabase Auth).

---

## Endpoints

### List clients

```
GET /api/v1/clients
```

| Param | Type | Required | Description |
|---|---|---|---|
| `name` | `string` | No | Filter by name |
| `phone` | `string` | No | Filter by phone number |

**Response `200 OK`** — Array of `ClientResponse`

---

### Get client by ID

```
GET /api/v1/clients/{id}
```

| Code | Description |
|---|---|
| `200` | Found |
| `404` | Not found |

---

### Create client

```
POST /api/v1/clients
```

**Request Body**

```json
{
  "name": "Ariell Abrego",
  "email": "ariell@example.com",
  "phone": "36137857"
}
```

| Code | Description |
|---|---|
| `201` | Created |
| `400` | Invalid request |

### Update client

```
PUT /api/v1/clients/{id}
```

**Request Body**

```json
{
  "name": "Ariell Abrego Modified",
  "email": "ariell.mod@example.com",
  "phone": "36137858"
}
```

| Code | Description |
|---|---|
| `200` | Updated successfully |
| `400` | Invalid request |
| `404` | Not found |

---

### Deactivate client

```
DELETE /api/v1/clients/{id}
```

Soft-delete. Record is preserved for historical reference.

| Code | Description |
|---|---|
| `204` | Deactivated |
| `404` | Not found |

---

## Models

### ClientResponse

```json
{
  "id": "uuid",
  "name": "Ariell Abrego",
  "email": "ariell@example.com",
  "phone": "36137857"
}
```
