# Contracts Index

## Endpoint Modules

Navigate to the specific module inside modules folder to view its available operations, expected Request Bodys, and Response DTOs:

* **[Services](./modules/services.md)** - Digital service catalog endpoints (`/api/v1/services`).
* **[Accounts](./modules/accounts.md)** - Master credentials management endpoints (`/api/v1/accounts`).
* **[Profiles](./modules/profiles.md)** - Profile slot management endpoints (`/api/v1/profiles`).
* **[Clients](./modules/clients.md)** - Customer profiling endpoints (`/api/v1/clients`).
* **[Subscriptions](./modules/subscriptions.md)** - Fulfillment and assignment endpoints (`/api/v1/subscriptions`).
* **[Reservations](./modules/reservations.md)** - Checkout flow and receipt validation endpoints (`/api/v1/reservations`).
* **[Orders](./modules/orders.md)** - Finalized financial transactions endpoints (`/api/v1/orders`).
* **[Dashboard](./modules/dashboard.md)** - Admin analytics and complex join views (`/api/v1/dashboard`).

### Deprecated (removed in Sprint 1.5)
* **[Products](./modules/products.md)** ~~`/api/v1/products`~~ — replaced by Services
* **[Inventory](./modules/inventory.md)** ~~`/api/v1/inventory`~~ — replaced by Services

---

## Standards & Architecture
Before extending or interacting with the API, please ensure adherence to the overarching protocols:

* [**API Architecture Guidelines**](./api-architecture.md) - Standard patterns, structures, and routing conventions.
* [**HTTP Responses Standard**](./http-responses.md) - Uniform response codes and error wrapper standards.
