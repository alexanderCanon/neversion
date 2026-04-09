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
| `seller` | `string` | No | Filter by seller name |
| `accountType` | `AccountType` | No | Filter by type |
| `expirationBefore` | `string (date)` | No | Filter by expiration before `YYYY-MM-DD` |
| `isActive` | `boolean` | No | Filter by active status |

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
The system automatically generates `Profiles` based on `maxProfiles` in the linked `Inventory`.

**Request Body**

```json
{
  "email": "nexp1010+a100@gmail.com",
  "pass": "588netflix",
  "inventoryId": 1,
  "seller": "ProviderName",
  "priceSeller": 25.00,
  "accountType": "FAMILY",
  "status": "AVAILABLE",
  "expirationDate": "2026-05-01"
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `email` | `string` | No | Account email |
| `pass` | `string` | No | Account password |
| `inventoryId` | `integer` | Yes | Linked inventory variant |
| `seller` | `string` | No | External provider name |
| `priceSeller` | `number` | Yes | Cost paid to the provider (GTQ) |
| `accountType` | `AccountType` | Yes | `FAMILY` or `INDIVIDUAL` |
| `status` | `AccountStatus` | Yes | Initial status, typically `AVAILABLE` |
| `expirationDate` | `string (date)` | Yes | Provider cut-off date |

| Code | Description |
|---|---|
| `201` | Account created |
| `400` | Invalid request |

---

### Deactivate account

```
DELETE /api/v1/accounts/{id}
```

Soft-delete. The account record is preserved for historical reference.

| Code | Description |
|---|---|
| `204` | Deactivated |
| `404` | Not found |

---

## Models

### AccountResponse

```json
{
  "id": "uuid",
  "email": "nexp1010+a100@gmail.com",
  "pass": "588netflix",
  "inventoryId": 1,
  "seller": "ProviderName",
  "priceSeller": 25.00,
  "accountType": "FAMILY",
  "status": "ASSIGNED",
  "expirationDate": "2026-05-01"
}
```

`accountType` values: `FAMILY`, `INDIVIDUAL` — see `enums.md`.
`status` values: `AVAILABLE`, `ASSIGNED`, `EXPIRED` — see `enums.md`.

> **Note:** `pass` is intentionally returned in plaintext. These are resale credentials
> managed by the admin, not user authentication passwords.
