# Services Module

> **Note:** This documentation reflects the Sprint 1.5 schema unification. The legacy dual system of "Products" and "Inventory" has been merged and simplified into "Services".

## Domain Context
- **Service (`services`)**: The central platform or digital entity being offered (e.g., "Netflix", "Spotify Family"). It acts as the anchor point for the physical accounts.
- Attributes: Defines the `name` and the structural limit `max_profiles` (which dictates how many physical profiles an account of this service can logically support).

## Relationships
- `Service` → `Account` (1 to many): A base Service acts as the umbrella for multiple physical master accounts purchased from wholesalers.

## Use Cases
- **CU-A01: Manage Ecosystem (Admin)**. The admin creates the base services outlining exactly what platforms the business offers, defining standard rules like `max_profiles`.

## Business Rules
- **BR-17 — Unified Base**: Unlike the legacy structure which fractured services into pricing blocks prematurely, `services` acts strictly as the categorized platform. Pricing strategies are moved closer to the actual transactional level or Account capabilities.
- **BR-18 — Administrative Sharing**: There are no service-level ownership restrictions. Every Admin shares global access to modify and maintain the `services` catalog.
