# Enums — Status & Types

This document defines the standardized enum terms used across the business domains.

> 🛠️ **ARCHITECTURAL WARNING (The Lifecycle of an Enum)**
> The system implements a strict bidirectional conversion mechanism between the Java backend and the PostgreSQL database:
> 
> 1. **Application Layer (Java/Frontend):** All code dictates these values purely in **`UPPER_SNAKE_CASE`** (e.g. `DIGITAL_SERVICE`, `EXPIRING_SOON`).
> 2. **Persistence Layer (Hibernate):** The abstract `@Converter(autoApply = true)` class (`EnumConverter.java`) intercepts the Java Enums and explicitly converts them to `lowercase_snake_case` Strings via `.toLowerCase()` *before* hitting JDBC. It resolves strings back to uppercase via `toUpperCase()` when querying.
> 3. **Database Layer (PostgreSQL):** PostgreSQL stores them genuinely as `TYPE AS ENUM` holding lowercase string values (e.g.`'active'`).
> 4. **Translation Casting:** To allow Hibernate's lowercase `varchar` parameters to insert securely into PostgreSQL`ENUM` types, the Flyway migrations (e.g., `V9`) define native implicit casts (`CREATE CAST (varchar AS category_type) WITH INOUT AS IMPLICIT`).

---

## 1. `category_type`

Classifies the type of digital product sold.

| Value | Description |
|---|---|
| `STREAMING` | Streaming platforms (Netflix, Prime Video, Disney+) |
| `SOFTWARE` | SaaS tools if applicable |
| `GIFT_CARD` | Gift cards (Amazon, Spotify, etc.) |
| `RECHARGE` | Game recharges (Free Fire en PagoStore, etc.) |
| `DIGITAL_SERVICE` | Digital subscriptions (ChatGPT, Canva Pro, etc.) |

> **Note:** `DIGITAL_SERVICE` replaces `suscrip4u` to avoid collision with the
> `subscriptions` table and use neutral technical terminology.

---

## 2. `account_type`

Defines how the account was acquired by the external distributor.

| Value | Description |
|---|---|
| `FAMILY` | Complete family account. Can be sold whole or divided into slots per profile. |
| `INDIVIDUAL` | Single profile account. Can only be assigned to one customer. |

---

## 3. `account_status`

Status of the main account (email + password). Persists in database.

| Value | When applies |
|---|---|
| `AVAILABLE` | Newly registered account, no slots assigned |
| `ASSIGNED` | At least one slot is occupied |
| `EXPIRED` | The distributor's `cut_off_date` was reached |

---

## 4. `slot_status`

Status of each individual slot within an account. Persists in database.

| Value | When applies |
|---|---|
| `AVAILABLE` | The slot has no active subscription, it is free to assign |
| `OCCUPIED` | The slot has an active subscription assigned to a customer |
| `BLOCKED` | The customer's subscription expired; the slot cannot be reassigned without admin intervention |

---

## 5. `account_availability`

**Calculated in real time — does not persist in database.**

Summarizes the occupancy status of an account for the dashboard.
It is derived by combining `account_type`, `slot_status` of its slots and the sales logic.

| Value | Description | Example |
|---|---|---|
| `PARTIAL` | Family account with at least one free slot and one occupied slot | 3 out of 5 slots occupied |
| `NO_AVAILABILITY` | Family account with all slots occupied | 5 out of 5 slots occupied |
| `INDIVIDUAL` | Account of type `INDIVIDUAL`, has no divisible slots | 1 profile, 1 customer |
| `COMPLETE` | Family account sold complete to a single customer | 1 customer occupies all 5 slots |

> **Calculation rule:**
> - If `account_type = INDIVIDUAL` → `INDIVIDUAL`
> - If `account_type = FAMILY` and there is 1 subscription that occupies the entire account → `COMPLETE`
> - If `account_type = FAMILY` and `available_slots = 0` → `NO_AVAILABILITY`
> - If `account_type = FAMILY` and `available_slots > 0` and `occupied_slots > 0` → `PARTIAL`

---

## 6. `sub_status`

Status of a subscription. Most persist in database,
except `EXPIRING_SOON` which is calculated when responding.

| Value | Persists | When applies | Who sets it |
|---|---|---|---|
| `ACTIVE` | ✅ | Subscription valid (`end_date` > today) | System |
| `EXPIRING_SOON` | ❌ | `end_date` within 7 days or less | System (calculated) |
| `EXPIRED` | ✅ | `end_date` was reached | System |
| `CANCELLED` | ✅ | Cancelled by admin due to non-payment | Admin |
| `SUSPENDED` | ✅ | Suspended due to violation of customer policies | Admin |

> **Difference between `CANCELLED` and `SUSPENDED`:**
> - `CANCELLED`: the customer did not pay or the payment was invalid. End of the commercial relationship.
> - `SUSPENDED`: the customer violated the usage policies (e.g., sharing access). Can be reactivated.

---

## 7. `reserv_status`

Status of the reservation flow, from when the customer reserves a product until they pay.

| Value | Description |
|---|---|
| `PENDING` | Reservation created, waiting for the customer to upload the proof of payment |
| `UPLOADED` | Proof of payment uploaded, pending admin validation |
| `VALIDATED` | Payment valid, admin must deliver the product |
| `EXPIRED` | The reservation exceeded 3600 seconds (1 hour) without being completed |
| `CANCELLED` | Cancelled by admin (invalid payment, system failure, etc.) |

---

## 8. `order_status`

Status of the persistent order, created once the payment is validated.

| Value | Description |
|---|---|
| `PENDING` | Order created, waiting for admin action |
| `COMPLETED` | Product successfully delivered to the customer |
| `REJECTED` | Order rejected by the admin |
| `CANCELLED` | Order cancelled due to multiple possible factors |

---

## Quick summary

```
CategoryType:        STREAMING, SOFTWARE, GIFT_CARD, RECHARGE, DIGITAL_SERVICE
AccountType:         FAMILY, INDIVIDUAL
AccountStatus:       AVAILABLE, ASSIGNED, EXPIRED
SlotStatus:          AVAILABLE, OCCUPIED, BLOCKED
AccountAvailability: PARTIAL, NO_AVAILABILITY, INDIVIDUAL, COMPLETE  ← calculated
SubStatus:           ACTIVE, EXPIRING_SOON*, EXPIRED, CANCELLED, SUSPENDED
ReservStatus:        PENDING, UPLOADED, VALIDATED, EXPIRED, CANCELLED
OrderStatus:         PENDING, COMPLETED, REJECTED, CANCELLED

* EXPIRING_SOON does not persist in DB, it is calculated when responding.
```
