# API Contract — Profiles

> All enums referenced here are defined and governed by `docs/enums.md`.
> Profiles are sub-divisions of an Account. Auto-generation happens when an Account is created,
> based on the service's `maxProfiles`.

---

## Endpoints

### List profiles for an account

```
GET /api/v1/profiles?accountId={accountId}
```

Returns all profiles for a given account.

| Param | Type | Required | Description |
|---|---|---|---|
| `accountId` | `long` | **Yes** | Internal numeric ID of the account |
| `available` | `boolean` | No | Filter only available profiles (default: false) |

**Response `200 OK`** — Array of `ProfileResponse`

---

### Get profile by UUID

```
GET /api/v1/profiles/{id}
```

| Code | Description |
|---|---|
| `200` | Profile found |
| `404` | Not found |

---

### Create profile

```
POST /api/v1/profiles
```

Manual profile creation.

**Request Body**

```json
{
  "accountId": 1,
  "name": "Ariell Abrego",
  "pin": "1607",
  "isOwner": false
}
```

| Code | Description |
|---|---|
| `201` | Created |
| `400` | Invalid request |

---

### Update profile details

```
PUT /api/v1/profiles/{id}
```

Updates the profile name, PIN or owner status.

**Request Body**

```json
{
  "name": "Ariell Abrego Modified",
  "pin": "1608",
  "isOwner": true
}
```

| Code | Description |
|---|---|
| `200` | Profile updated |
| `404` | Not found |

---

### Delete profile

```
DELETE /api/v1/profiles/{id}
```

| Code | Description |
|---|---|
| `240` | Deleted |
| `404` | Not found |

---

## Models

### ProfileResponse

```json
{
  "id": "uuid",
  "accountId": 1,
  "name": "Ariell Abrego",
  "pin": "1607",
  "isOwner": false,
  "createdAt": "2026-04-07T20:00:00"
}
```
