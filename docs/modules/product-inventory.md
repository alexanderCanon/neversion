# Product & Inventory Module

> [!WARNING]
> **DEPRECATED (Sprint 1.5)**
> This document and the fractured Product/Inventory concepts have been functionally deprecated in favor of a single unified entity.
> Please refer to [services.md](services.md) for the active documentation.


## Domain Context
- **Product (`products`)**: The conceptual or general item being sold (e.g., "Netflix", "Disney+"). It does not hold price or duration. Belongs to a CategoryType.
- **Inventory (`inventory`)**: The specific, sellable variant of a Product (e.g., "Netflix - Individual Profile - 30 Days - Q40.00"). Defines price (GTQ), duration in days, account type, stock, and max profiles per account.

## Relationships
- `Product` → `Inventory` (1 to many): One product has multiple pricing variants.

## Use Cases
- **CU-A01: Manage Catalog (Admin)**. The admin can create, read, update, or deactivate products and their various inventory variants.
- **CU-C01: Explore Catalog (Customer)**. The customer can view all active products and select variants, seeing the real-time price.

## Business Rules
- **BR-17 — Stock is managed manually**: The `stock` field in Inventory is referential only. It is not automatically decremented when a Subscription is created. The admin is responsible for keeping it up to date.
- **BR-18 — Any admin can manage any product**: There are no product-level ownership or permission restrictions between admins. All admins share full access to all products.
- **Scope**: Products and Inventory are fully implemented in Sprint 1 and Sprint 2.
