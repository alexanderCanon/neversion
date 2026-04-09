# API Contract — Subscriptions

> All enums referenced here are defined and governed by `docs/enums.md`.

---

## Endpoints

### List subscriptions

```
GET /api/v1/subscriptions
```

| Param | Type | Required | Description |
|---|---|---|---|
| `status` | `SubStatus` | No | Filter by status |
| `clientId` | `string (uuid)` | No | Filter by client |
| `accountId` | `string (uuid)` | No | Filter by account |

**Response `200 OK`** — Array of `SubscriptionResponse`

---

### Create subscription

```
POST /api/v1/subscriptions
```

Manually assigns an account or profile to a client.
Validates individual account exclusivity (BR-06) before saving.

**Request Body**

```json
{
  "profileId": "uuid",
  "clientId": "uuid",
  "accountId": "uuid",
  "purchaseDate": "2026-03-15",
  "paymentDueDate": "2026-04-15",
  "price": 10.00,
  "notes": "Premium plan"
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `profileId` | `string (uuid)` | Yes | Target profile |
| `clientId` | `string (uuid)` | Yes | Client being assigned |
| `accountId` | `string (uuid)` | Yes | Parent account |
| `purchaseDate` | `string (date)` | Yes | Start date |
| `paymentDueDate` | `string (date)` | Yes | End / renewal date |
| `price` | `number` | No | Subscription price |
| `notes` | `string` | No | Admin notes |

**Responses**

| Code | Description |
|---|---|
| `201` | Subscription created |
| `400` | Invalid request |
| `404` | Account or profile not found |
| `409` | Conflict or business rule violation |

---

### Terminate subscription

```
PUT /api/v1/subscriptions/{id}/terminate
```

Transitions a subscription to `CANCELLED`. Triggered by admin due to payment failure.

| Code | Description |
|---|---|
| `200` | Terminated |
| `400` | Already cancelled |
| `404` | Not found |

---

### Suspend subscription

```
PUT /api/v1/subscriptions/{id}/suspend
```

Transitions an `ACTIVE` subscription to `SUSPENDED`. Triggered by admin due to policy violation.

| Code | Description |
|---|---|
| `200` | Suspended |
| `400` | Not ACTIVE |
| `404` | Not found |

---

## Models

### SubscriptionResponse

```json
{
  "id": "uuid",
  "accountId": "uuid",
  "clientId": "uuid",
  "profileId": "uuid",
  "purchaseDate": "2026-03-15",
  "paymentDueDate": "2026-04-15",
  "status": "ACTIVE",
  "price": 10.00,
  "notes": "Premium plan"
}
```

`status` values: `ACTIVE`, `EXPIRED`, `CANCELLED`, `SUSPENDED` — see `enums.md`.
`EXPIRING_SOON` is calculated at query time, never persisted or returned here.

---

## Deprecated

### ~~GET /api/v1/subscriptions/dashboard~~

> ⚠️ **Deprecated** — Removal date: **2026-04-30**
> Replaced by `api/dashboard.md` endpoints. Do not use in new features.
