# API Contract — Dashboard

> All enums referenced here are defined and governed by `docs/enums.md`.
> Enum values are always in `UPPER_SNAKE_CASE` at the API level.
> The frontend is responsible for mapping enum values to localized display labels.

---

## Endpoints

### 1. Get streaming services summary

```
GET /api/v1/dashboard?category=STREAMING
```

Returns the list of services filtered by category, with account count per service.
Used to render the dashboard entry screen.

**Query Parameters**

| Param | Type | Required | Description |
|---|---|---|---|
| `category` | `CategoryType` | Yes | Filter by service category. See `enums.md`. |

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
| `productId` | `string (uuid)` | Unique service identifier (use as `serviceId` in subsequent calls) |
| `productName` | `string` | Display name of the service |
| `category` | `CategoryType` | Service category |
| `totalAccounts` | `integer` | Total accounts registered for this service |

---

### 2. Get accounts for a service

```
GET /api/v1/dashboard/products/{productId}/accounts
```

Returns all accounts belonging to a service, including profile availability.
Profile details are **not** included — loaded separately on expand.

**Path Parameters**

| Param | Type | Required | Description |
|---|---|---|---|
| `productId` | `string (uuid)` | Yes | Service identifier (use `productId` from endpoint 1) |

**Response `200 OK`**

```json
[
  {
    "accountId": "uuid",
    "email": "nexp1010+a100@gmail.com",
    "password": "588netflix",
    "cutOffDate": "2026-05-01",
    "accountType": "FAMILY",
    "accountStatus": "ACTIVE",
    "maxProfiles": 5,
    "occupiedProfiles": 3,
    "availableProfiles": 2,
    "availability": "PARTIAL"
  }
]
```

| Field | Type | Description |
|---|---|---|
| `accountId` | `string (uuid)` | Unique account identifier |
| `email` | `string` | Account email credential |
| `password` | `string` | Account password credential |
| `cutOffDate` | `string (date)` | Provider renewal date for this account |
| `accountType` | `string` | `FAMILY` (BY_PROFILE sale mode) or `INDIVIDUAL` (FULL_ACCOUNT sale mode) |
| `accountStatus` | `string` | `ACTIVE` if renewal date has not passed, `EXPIRED` otherwise |
| `maxProfiles` | `integer` | Maximum profiles defined by the service |
| `occupiedProfiles` | `integer` | Profiles with ACTIVE or SUSPENDED subscriptions |
| `availableProfiles` | `integer` | `maxProfiles - occupiedProfiles` |
| `availability` | `AccountAvailability` | Calculated field — not persisted. See `enums.md`. |

---

### 3. Get profiles for an account

```
GET /api/v1/dashboard/accounts/{accountId}/profiles
```

Returns all profiles for a given account, including customer subscription data if occupied.
Called lazily when the user expands an account row in the dashboard.

**Path Parameters**

| Param | Type | Required | Description |
|---|---|---|---|
| `accountId` | `string (uuid)` | Yes | Account identifier |

**Response `200 OK`**

```json
[
  {
    "profileId": "uuid",
    "profileName": "Ariell Abrego",
    "pin": "1607",
    "profileStatus": "OCCUPIED",
    "subscription": {
      "subscriptionId": "uuid",
      "startDate": "2026-03-15",
      "endDate": "2026-04-15",
      "status": "ACTIVE",
      "customer": {
        "id": "uuid",
        "name": "Ariell Abrego",
        "phone": "36137857",
        "type": "CLIENT"
      }
    }
  },
  {
    "profileId": "uuid",
    "profileName": null,
    "pin": null,
    "profileStatus": "AVAILABLE",
    "subscription": null
  }
]
```

| Field | Type | Description |
|---|---|---|
| `profileId` | `string (uuid)` | Unique profile identifier |
| `profileName` | `string \| null` | Profile name inside the streaming service |
| `pin` | `string \| null` | Profile PIN inside the streaming service |
| `profileStatus` | `ProfileStatus` | `OCCUPIED`, `BLOCKED`, or `AVAILABLE`. See `enums.md`. |
| `subscription` | `ProfileSubscription \| null` | Null if the profile is unassigned |
| `subscription.subscriptionId` | `string (uuid)` | Subscription identifier |
| `subscription.startDate` | `string (date)` | Access start date |
| `subscription.endDate` | `string (date)` | Payment due / renewal date |
| `subscription.status` | `SubStatus` | Calculated by backend. `EXPIRING_SOON` is not persisted. See `enums.md`. |
| `subscription.customer.id` | `string (uuid)` | Client identifier |
| `subscription.customer.name` | `string` | Client full name |
| `subscription.customer.phone` | `string` | Client phone number |
| `subscription.customer.type` | `string` | Always `CLIENT` |

> **All profiles are always included in the response.**
> A profile with `subscription: null` represents a free, assignable profile.
> The frontend uses this to render available vs occupied profiles visually.

---

## Deprecated Endpoints

### ~~GET /api/v1/subscriptions/dashboard~~

> ⚠️ **Deprecated** — Scheduled for removal on **2026-04-30**
>
> Replaced by the three endpoints above, which provide a richer and correctly
> structured response including customer data, profile availability, and account grouping.
>
> **Do not use in new frontend features.**

---

## Governance Rules

- Enum values in API requests and responses are always `UPPER_SNAKE_CASE`.
- `availability` and `EXPIRING_SOON` are calculated at query time — never persisted.
- To add or modify any enum value: update `enums.md` first, then update this contract.
- Frontend maps enum values to localized Spanish display labels independently.
