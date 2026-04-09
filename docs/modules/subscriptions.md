# Subscriptions Module

> **Note:** This documentation reflects the Sprint 1.5 schema unification. The focus of the subscription is now tightly coupled to background notification architectures (n8n). 

## Domain Context
- **Subscription (`subscriptions`)**: The final, active link between a `Client`, a specific `Profile`, and their strict payment timeline (`start_date` and `payment_due_date`).
- Attributes: Computes depth via `months_paid` and dictates access using `status` (active, suspended, cancelled).

## Workflow (Fulfillment & Automation)
1. **Assignment**: After order validation, Admin creates or updates a `Subscription`, linking the `Client` to a specific `Profile` inside an Account.
2. **Milestone Tracking**: Background jobs (linked to the `notification_log` table) continuously query the `payment_due_date`. 
3. **Automated Reminders**: Emails and WhatsApp messages are sent at precise intervals (7 days, 3 days, overdue).
4. **Renewal**: When a client pays, the `payment_due_date` is extended by 30/60/90 days, incrementing the `months_paid` field.

## Relationships
- `Profile` → `Subscription` (1 to 1 active): One active subscription per profile at a time.
- `Client` → `Subscription` (1 to many): A client can hold multiple active subscriptions simultaneously (across different services).

## Business Rules
- **BR-08 — Multiple active subscriptions**: A client can hold multiple active subscriptions across different services.
- **BR-10 — Overdue Management**: If `payment_due_date` is breached without renewal, the async job logs an `overdue` stage, and the Admin can manually change the status to `suspended` (or future automatons can cut access).
- **BR-11 — Termination vs suspension**: `CANCELLED` is triggered due to total abandonment. `SUSPENDED` is triggered due to a missed payment window, leaving room for reactivation.
