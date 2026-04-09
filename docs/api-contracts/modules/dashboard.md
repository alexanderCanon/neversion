# API Contract — Dashboard

> All enums referenced here are defined and governed by `docs/enums.md`.
> Enum values are always in `UPPER_SNAKE_CASE` at the API level.
> The frontend is responsible for mapping enum values to localized display labels.

---

## Endpoints

### 1. Get streaming products summary

```
GET /api/v1/dashboard?category=STREAMING
```

Returns the list of products filtered by category, with account count per product.
Used to render the dashboard entry screen.

**Query Parameters**

| Param | Type | Required | Description |
|---|---|---|---|
| `category` | `CategoryType` | Yes | Filter by product category. See `enums.md`. |

**Response `200 OK`**

```json
[
  {
    "productId": "uuid",
    "productName": "Netflix",
    "category": "STREAMING",
    "totalAccounts": 30
  }
]
```

| Field | Type | Description |
|---|---|---|
| `productId` | `string (uuid)` | Unique product identifier |
| `productName` | `string` | Display name of the product |
| `category` | `CategoryType` | Product category |
| `totalAccounts` | `integer` | Total accounts registered for this product |

---

### 2. Get accounts for a product

```
GET /api/v1/dashboard/products/{productId}/accounts
```

Returns all accounts belonging to a product, including slot availability.
Slot details are **not** included — loaded separately on expand.

**Path Parameters**

| Param | Type | Required | Description |
|---|---|---|---|
| `productId` | `string (uuid)` | Yes | Product identifier |

**Response `200 OK`**

```json
[
  {
    "accountId": "uuid",
    "email": "nexp1010+a100@gmail.com",
    "password": "588netflix",
    "cutOffDate": "2026-05-01",
    "accountType": "FAMILY",
    "accountStatus": "ASSIGNED",
    "maxSlots": 5,
    "occupiedSlots": 3,
    "availableSlots": 2,
    "availability": "PARTIAL"
  }
]
```

| Field | Type | Description |
|---|---|---|
| `accountId` | `string (uuid)` | Unique account identifier |
| `email` | `string` | Account email credential |
| `password` | `string` | Account password credential |
| `cutOffDate` | `string (date)` | Account expiration date from the external provider |
| `accountType` | `AccountType` | `FAMILY` or `INDIVIDUAL`. See `enums.md`. |
| `accountStatus` | `AccountStatus` | Current account status. See `enums.md`. |
| `maxSlots` | `integer` | Maximum slots defined by the admin |
| `occupiedSlots` | `integer` | Number of slots currently occupied |
| `availableSlots` | `integer` | `maxSlots - occupiedSlots` |
| `availability` | `AccountAvailability` | Calculated field — not persisted. See `enums.md`. |

---

### 3. Get slots for an account

```
GET /api/v1/dashboard/accounts/{accountId}/slots
```

Returns all slots for a given account, including customer subscription data if occupied.
Called lazily when the user expands an account row in the dashboard.

**Path Parameters**

| Param | Type | Required | Description |
|---|---|---|---|
| `accountId` | `string (uuid)` | Yes | Account identifier |

**Response `200 OK`**

```json
[
  {
    "slotId": "uuid",
    "profileName": "Ariell Abrego",
    "pin": "1607",
    "slotStatus": "OCCUPIED",
    "subscription": {
      "subscriptionId": "uuid",
      "startDate": "2026-03-15",
      "endDate": "2026-04-15",
      "status": "ACTIVE",
      "customer": {
        "id": "uuid",
        "name": "Ariell Abrego",
        "phone": "36137857",
        "type": "USER_GUEST"
      }
    }
  },
  {
    "slotId": "uuid",
    "profileName": null,
    "pin": null,
    "slotStatus": "AVAILABLE",
    "subscription": null
  }
]
```

| Field | Type | Description |
|---|---|---|
| `slotId` | `string (uuid)` | Unique slot identifier |
| `profileName` | `string \| null` | Profile name inside the streaming service |
| `pin` | `string \| null` | Profile PIN inside the streaming service |
| `slotStatus` | `SlotStatus` | Current slot status. See `enums.md`. |
| `subscription` | `SlotSubscription \| null` | Null if the slot is unassigned |
| `subscription.subscriptionId` | `string (uuid)` | Subscription identifier |
| `subscription.startDate` | `string (date)` | Customer purchase date |
| `subscription.endDate` | `string (date)` | Customer renewal date |
| `subscription.status` | `SubStatus` | Calculated by backend. `EXPIRING_SOON` is not persisted. See `enums.md`. |
| `subscription.customer.id` | `string (uuid)` | Customer identifier |
| `subscription.customer.name` | `string` | Customer full name |
| `subscription.customer.phone` | `string` | Customer phone number |
| `subscription.customer.type` | `string` | `USER_GUEST` (Sprint 1) or `PROFILE` (Sprint 2) |

> **Empty slots are always included in the response.**
> A slot with `subscription: null` represents a free, assignable slot.
> The frontend uses this to render available vs occupied slots visually.

---

## Deprecated Endpoints

### ~~GET /api/v1/subscriptions/dashboard~~

> ⚠️ **Deprecated** — Scheduled for removal on **2026-04-30**
>
> Replaced by the three endpoints above, which provide a richer and correctly
> structured response including customer data, slot availability, and account grouping.
> The `SubscriptionDashboardDTO` response lacked: customer name, phone, account ID,
> slot status, and `availability` calculation.
>
> **Do not use in new frontend features.**

---

## Governance Rules

- Enum values in API requests and responses are always `UPPER_SNAKE_CASE`.
- `availability` and `EXPIRING_SOON` are calculated at query time — never persisted.
- To add or modify any enum value: update `enums.md` first, then update this contract.
- Frontend maps enum values to localized Spanish display labels independently.
