# Reservations & Orders Module

> **Note:** This documentation reflects the Sprint 1.5 schema unification. While these tables are not actively used by the core renewal notification automation, they remain the cornerstone for the future Phase 3 Storefront integration. Legacy `user_guest` dependencies have been completely replaced by the `clients` table.

## Domain Context
- **Reservation (`reservations`)**: A temporary hold placed by a `Client` while completing a payment. Expires in 60 minutes.
- **Order (`orders`)**: A persistent transactional record created once the client uploads an S3 payment receipt and an Admin validates it. 

## Workflow (Reservation to Order)
1. Client initiates a checkout, creating a `Reservation` holding the requested total and mapping to their `client_id`.
2. Client uploads a payment receipt. Reservation becomes `uploaded`.
3. Admin reviews the S3 receipt. If valid, Admin approves it and creates an `Order`.

## Relationships
- `Client` → `Reservation` (1 to many): A client can initiate multiple checkout attempts.
- `Reservation` → `Order` (1 to 1): An Order is only materialized on payment validation.
- `Order` → `Subscription` (1 to many): Validating an order opens the gateway to activate or extend multiple subscriptions.

## Business Rules
- **BR-12 — Reservation expires after 1 hour**: A Reservation transitions to `EXPIRED` if not completed within 3600 seconds. 
- **BR-13 — Cancellation constraints**: Only `PENDING` or `UPLOADED` reservations can be cancelled. `VALIDATED` or `EXPIRED` cannot.
- **BR-14 — Receipt URL must be unique**: Uploading a duplicate `receipt_url` to any reservation is rejected to prevent fraud.
- **BR-20 — Reservation to Order transition**: An `Order` can only be generated when an Admin manually validates an `uploaded` Reservation (Sprint 1 scope).
