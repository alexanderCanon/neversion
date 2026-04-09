# 🗄️ Unified Database Schema & Domain Mapping (Sprint 1.5)

> **Note for Systems Analysts & Developers:** This schema reflects the literal database tables bridging the new streamlined base used for automations (`newversion` database) while retaining the critical transactional structure (`reservations` / `orders`) expected by the storefront.

---

## 📌 1. Domain Enums & State Constraints

- **`status` (Subscription):** `active`, `suspended`, `cancelled`. *(Driven by payment validity).*
- **`sale_mode` (Account):** `by_profile`, `full_account`.
- **`order_status`**: `PENDING`, `COMPLETED`, `REJECTED`, `CANCELLED`.
- **`stage` (Notification):** `7_days_prior`, `3_days_prior`, `overdue`, etc.

---

## 🛒 2. Catalog & Provider Inventory

### `services`
The specific streaming or digital platform.
- `id` (INT, PK) - Auto-incremented.
- `name` (VARCHAR) - e.g., "Netflix", "Spotify Family".
- **`max_profiles`** (INT) - Default logical amount of profiles per account (e.g., 5).

### `accounts`
The physical master credential purchased from a third-party wholesale provider.
- `id` (INT, PK) 
- `service_id` (INT, FK) - Links to base `services`.
- `email` (VARCHAR) & `password` (VARCHAR) - Provider Master Credentials.
- `renewal_date` (DATE) - The exact date Neversion must pay the wholesaler to keep the account alive.
- `plan` (VARCHAR) - Defines the quality tier (e.g., "4K Ultra HD").
- `sale_mode` (VARCHAR) - Indicates whether it's sold split by profiles or as a whole.
- `notes` (TEXT) - Private admin notes.

---

## 📂 3. Subdivision & Access

### `profiles`
The physical sub-divisions of an `Account` (formerly "Slots").
- `id` (INT, PK)
- `account_id` (INT, FK) - Parent Account.
- `name` (VARCHAR) - E.g., The screen name configured in the platform.
- `pin` (VARCHAR) - Security PIN assigned.
- **`is_owner`** (BOOLEAN) - Indicates if this profile has admin privileges within the streaming platform.

---

## 👥 4. End Consumers

### `clients`
The consumers utilizing the services.
- `id` (INT, PK)
- `name` (VARCHAR)
- `phone` (VARCHAR) - Used for WhatsApp outreach.
- `email` (VARCHAR)
- `notes` (TEXT)

---

## 🧾 5. Billing & Connectivity Lifecycle

### `subscriptions`
The absolute link between the Client, the specific Profile assigned to them, and their payment schedule.
- `id` (INT, PK)
- `client_id` (INT, FK) - Target customer.
- **`profile_id`** (INT, FK) - The specific slot they are occupying.
- `start_date` (DATE) - Beginning of their access lifecycle.
- **`payment_due_date`** (DATE) - Critical field monitored by background automations.
- `months_paid` (INT) - How deep into their payment cycle they are.
- `status` (VARCHAR) - Active or Suspended.

---

## 🤖 6. Asynchronous Automations

### `notification_log`
Tracking point for independent cronjobs and async events.
- `id` (INT, PK)
- `entity_type` (VARCHAR) - Defines what is being alerted (e.g., 'subscription', 'account').
- `entity_id` (INT) - Ties to the specific record in question.
- `stage` (VARCHAR) - Which message in the drip sequence was sent.
- `sent_at` (TIMESTAMPTZ) - Audit timestamp.

---

## 🏬 7. Storefront Transactions (Legacy Bridging)

These tables allow the future Phase 3 "Tienda Online" to operate safely without immediately altering the core automation engine.

### `reservations`
Temporary holding state created when a client checks out.
- `id` (UUID, PK)
- `client_id` (INT, FK) - Links to the new unified client table.
- `discount` (NUMERIC 10,2)
- `total` (NUMERIC 10,2)
- `receipt_url` (TEXT) - S3 Link to the payment voucher.
- `expiration_date` (TIMESTAMPTZ) - Auto-expires if not validated manually.

### `orders`
Created exclusively upon Admin manual validation of the Reservation receipt.
- `id` (UUID, PK)
- `reservation_id` (UUID, FK)
- `status` (VARCHAR)
- `notes` (TEXT)
