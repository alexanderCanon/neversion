# Clients Module

> **Note:** This documentation reflects the Sprint 1.5 schema unification. The legacy terms "Users" and "User Guests" have been deprecated fundamentally in favor of the universal "Clients".

## Domain Context
- **Client (`clients`)**: The end consumer utilizing and paying for the streaming profiles. This entity represents both unregistered guests during storefront checkout and managed entities in the Admin panel.
- Attributes: Identified by `name`, `email`, connected globally via `phone` (for WhatsApp notifications and automations), and trackable via internal provider `notes`.

## Workflow & Relationships
- `Client` → `Subscription` (1 to many): A client can hold multiple active subscriptions simultaneously (e.g., one for Netflix, one for Disney+), tied directly to their unified `client_id`.
- `Client` → `Reservation` (1 to many): Checkout carts initiated by a client.

## Scope & Implementation
- **Sprint 1.5 Structure**: By collapsing `users_guests` and potential future logged-in structures into a single `clients` table, the platform enables seamless integration with background automation tools (n8n). All async renewals pull directly from this table using the `phone` and `email` to deliver precise billing milestones.
- **Storefront**: The client model will still serve as the base for checkout, regardless of whether strict JWT login barriers are raised in the future.
