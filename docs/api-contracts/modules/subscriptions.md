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
| `profileId` | `string (uuid)` | No | Filter by profile |

**Response `200 OK`** — Array of `SubscriptionResponse`

---

### Get subscription by ID

```
GET /api/v1/subscriptions/{id}
```

| Code | Description |
|---|---|
| `200` | Subscription found |
| `404` | Not found |

---

### Create subscription

```
POST /api/v1/subscriptions
```

Admin assigns a Profile to a Client.
Validates profile exclusivity (BR-04) before saving.

**Request Body**

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

| Field | Type | Required | Description |
|---|---|---|---|
| `profileId` | `string (uuid)` | Yes | Target profile |
| `clientId` | `string (uuid)` | Yes | Client being assigned |
| `accountId` | `string (uuid)` | Yes | Parent account |
| `startDate` | `string (date)` | No | Access start date (defaults to today) |
| `paymentDueDate` | `string (date)` | Yes | Renewal / payment due date |
| `notes` | `string` | No | Admin notes |

**Responses**

| Code | Description |
|---|---|
| `201` | Subscription created |
| `400` | Invalid request |
| `404` | Account or profile not found |
| `409` | Conflict — profile already has an active subscription (BR-04) |

---

### Cancel subscription

```
PUT /api/v1/subscriptions/{id}/cancel
```

Transitions a subscription to `CANCELLED`. Triggered by admin.

| Code | Description |
|---|---|
| `200` | Cancelled |
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
  "status": "ACTIVE",
  "startDate": "2026-03-15",
  "paymentDueDate": "2026-04-15",
  "monthsPaid": 1,
  "notes": "Premium plan",
  "createdAt": "2026-03-15T10:00:00"
}
```

`status` values: `ACTIVE`, `EXPIRED`, `CANCELLED`, `SUSPENDED` — see `enums.md`.
`EXPIRING_SOON` is calculated at query time, never persisted or returned here.

---

## Deprecated

### ~~GET /api/v1/subscriptions/dashboard~~

> ⚠️ **Deprecated** — Removal date: **2026-04-30**
> Replaced by `api/dashboard.md` endpoints. Do not use in new features.
