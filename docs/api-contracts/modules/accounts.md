# API Contract — Accounts

> All enums referenced here are defined and governed by `docs/enums.md`.

---

## Endpoints

### List accounts

```
GET /api/v1/accounts
```

| Param | Type | Required | Description |
|---|---|---|---|
| `serviceId` | `integer` | No | Filter by service (internal ID) |

**Response `200 OK`** — Array of `AccountResponse`

---

### Get account by ID

```
GET /api/v1/accounts/{id}
```

| Code | Description |
|---|---|
| `200` | Account found |
| `404` | Not found |

---

### Create account

```
POST /api/v1/accounts
```

Registers a new master credential purchased from an external provider.
The system automatically generates `Profiles` based on `maxProfiles` in the linked `Service` when `saleMode=BY_PROFILE`.

**Request Body**

```json
{
  "email": "nexp1010+a100@gmail.com",
  "pass": "588netflix",
  "serviceId": 1,
  "saleMode": "BY_PROFILE",
  "renewalDate": "2026-05-01",
  "notes": "Purchased from ProviderX"
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `email` | `string` | Yes | Account email credential |
| `pass` | `string` | Yes | Account password credential |
| `serviceId` | `integer` | Yes | Linked service (internal numeric ID) |
| `saleMode` | `SaleMode` | Yes | `BY_PROFILE` or `FULL_ACCOUNT` |
| `renewalDate` | `string (date)` | Yes | Date Neversion must renew with the provider |
| `notes` | `string` | No | Internal admin notes |

| Code | Description |
|---|---|
| `201` | Account created |
| `400` | Invalid request |

---

### Delete account

```
DELETE /api/v1/accounts/{id}
```

Hard-delete. Cascades to profiles.

| Code | Description |
|---|---|
| `204` | Deleted |
| `404` | Not found |

---

## Models

### AccountResponse

```json
{
  "id": "uuid",
  "email": "nexp1010+a100@gmail.com",
  "pass": "588netflix",
  "serviceId": 1,
  "saleMode": "BY_PROFILE",
  "renewalDate": "2026-05-01",
  "notes": "Purchased from ProviderX",
  "createdAt": "2026-03-15T10:00:00"
}
```

`saleMode` values: `BY_PROFILE`, `FULL_ACCOUNT` — see `enums.md`.

> **Note:** `pass` is intentionally returned in plaintext. These are resale credentials
> managed by the admin, not user authentication passwords.
