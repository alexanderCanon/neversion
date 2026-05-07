# Sprint 1.5 - Schema Refactor & Unification

## Context
Recently, a more fluid and natural database designed originally for a separate function (asynchronous renewal management) was implemented.
By directly analyzing the schema of this new database using the Supabase MCP, a semantically superior design has been verified. The goal of this "Sprint 1.5" is to **adapt the documentary base and the code already implemented towards this new model**, combining the solidity of the existing architecture (such as sales and order management) with the commercial simplicity of the new schema.

---

## Entity Comparison (Current vs. New)

| Domain | Current Documentation / Spring Boot | New Database | Refactoring Strategy |
| :--- | :--- | :--- | :--- |
| **Catalog** | `products` + `inventory` | `services` | **Simplify and Rename**. We will move from the dual Product/Inventory concept to a single central entity `services` (e.g., Netflix, Disney+). |
| **Provider Accounts**| `accounts` | `accounts` | **Enrich**. The concept is maintained, but the new native fields identified in Supabase are adopted: `plan`, `renewal_date` and `sale_mode`. |
| **Sub-Divisions** | `account_slots` | `profiles` | **Nomenclature Change**. Abandon the technical term "slots" for `profiles`. Include the new fields identified (`name`, `pin`, `is_owner`). |
| **Consumers** | `users_guests` | `clients` | **Standardize**. Migrate all references of `user_guest` or `users` to `clients` in the sales architecture. |
| **Subscriptions** | `subscriptions` | `subscriptions` | **Relational Fusion**. The table is maintained, but now logically links `client_id` with `profile_id`. Includes native control of `start_date`, `payment_due_date` and `months_paid`. |
| **Audit/Notices** | *(Calculated logic in API)* | `notification_log` | **Adopt**. This new table is incorporated as a standard to record any asynchronous event or status notice. |
| **Transactions** | `reservations` + `orders` | *(Absent in the new DB)* | **Keep (Merge).** These tables are vital for Phase 3 "Online Store". They will be maintained, but their Foreign Keys will be repointed (e.g., Instead of pointing to `inventory` and `user_guest`, they will point to `services`/`profiles` and `clients`). |

---

## Action Plan by Phases

The goal of this plan is to avoid rewriting everything from scratch. We are going gradually re-adapt the repository using methodical *refactoring*.

### Phase 1: Alignment of Sources of Truth (Documentation) <- *[Active Phase]*
*Step focused strictly on aligning the documents before touching a single line of Java or Angular code.*
1. **Update `database-schema.md`:** Overwrite the current schema in `docs/schema/` detailing the new tables, their data types (like the current serial `integer` vs the past UUID), and document what merges with reservations/orders.
2. **Update Diagrams (ERD):** Restructure the Mermaid diagrams (in `artifacts/mermaid/`) verifying that the new Foreign Keys make commercial sense.
3. **Adjust Business Modules:** Go to the `docs/modules/` folder and refactor based on the new terminology (change references from "Slot" to "Profile", and "Guest" to "Client").

### Phase 2: Backend Adaptation (Spring Boot & PostgreSQL)
*Transform the Hexagonal Architecture to reflect the new domain safely.*
1. **Migrations with Idempotency (Flyway):** Instead of deleting past migrations, we will refactor them. The Production environment uses Supabase (`newversion`) which already has the table created and is configured with `baseline-version: 0`. This will cause Flyway to load and execute the `V1, V2...` migrations without error and without deleting anything, modifying all SQL scripts with `CREATE TABLE IF NOT EXISTS`. In the local environment (`dev`), everything will be created smoothly from scratch.
2. **Java Entities and DTOs Refactor:** Change names of classes and interfaces. `AccountSlotEntity` -> `ProfileEntity`, `Product` -> `Service`.
3. **REST Routes Mapping:** Adjust controllers. (E.g., `GET /api/account-slots` -> `GET /api/profiles`).
4. **Testcontainers Validation:** Fix the Unit and Integration Tests.

### Phase 3: Frontend Alignment (Angular 17 Panel)
*Reconstruction of the administrator interaction to match the adjusted API.*
1. **TypeScript Interfaces:** Rewrite the data models in `panel/src/app/core/models/`.
2. **UI/UX Labels:** Update side menus, buttons, and titles in the graphical interface ("Slot Management -> Profiles").
3. **Route Consumption:** Ensure that the Signals-oriented components invoke the new endpoints under the reformed format and properties (e.g., process validations by `is_owner`).

---

**Note:** Once this documentation with the planned flow is approved, the next ordered steps consist of starting to devour **Phase 1** (Updating every file inside `/docs/schema` and `/docs/modules`).
