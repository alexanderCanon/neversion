# Users Module

> [!WARNING]
> **DEPRECATED (Sprint 1.5)**
> This document and the terms "Users" and "User Guests" have been functionally deprecated in favor of a single unified entity.
> Please refer to [clients.md](clients.md) for the active documentation.


## Domain Context
- **User Guest (`user_guests`)**: An unregistered customer who makes a purchase (Primary focus for Sprint 1). Identified by name, email, and phone number.
- **Profile (`profiles`)**: A fully registered customer linked to Supabase Auth. Public schema mirror table (Focus for Sprint 2).

## Use Cases
- Customers acting as "guests" currently check out and upload payment manually.

## Sprint Scope Boundaries
### Sprint 1 (Current)
- Customers operate purely as `user_guests`. They do not have access to an account portal to see past orders or subscriptions.

### Sprint 2 (Future)
- Customer login is implemented via `profiles`. This opens up access to purchase history and automated assignments.
- Automated payment gateways become available.
