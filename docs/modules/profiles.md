# Profiles & Accounts Module

> **Note:** This documentation reflects the Sprint 1.5 schema unification. The legacy term "Account Slots" has been entirely replaced by "Profiles" to align with natural business terminology.

## Domain Context
- **Account (`accounts`)**: The master credential purchased by the Admin from a wholesale provider to operate a specific Service. Contains provider login details (`email`, `password`), quality `plan`, `sale_mode` (by profile or full account), and a critical `renewal_date` governing the lifespan of the master account.
- **Profile (`profiles`)**: The physical sub-divisions of an Account (formerly "Slots"). Represents one localized viewer profile inside the streaming platform. Holds a `name`, numeric `pin`, and an `is_owner` flag (useful for identifying the main admin profile within the streaming account).

## Workflow (Admin Procurement)
1. **Validation & Procurement**: After confirming a client's request, the Admin procures the master credential from a wholesaler or uses available physical stock.
2. **Registration**: Admin registers the new `Account` and associates it with a `Service`.
3. **Partitioning**: Admin generates the specific `Profiles` within the account based on the `max_profiles` allowed by the service.

## Relationships
- `Service` → `Account` (1 to many): An account strictly belongs to a single categorized service (e.g., Netflix).
- `Account` → `Profile` (1 to many): Accounts are divided into localized profiles.

## Business Rules
- **BR-01 — Partitioning limitation**: The amount of Profiles generated for an Account relies heavily on the `max_profiles` defined by its parent Service.
- **BR-03 — Exclusivity of Sale Mode**: An Account sold entirely under the `full_account` sale mode assumes direct control over all its logical profiles.
- **BR-04 — Status derivation**: Whether an Account or Profile is "Available" is calculated organically by querying if there are active `Subscriptions` tied to them. 
