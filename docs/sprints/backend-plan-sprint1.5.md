# Backend Roadmap - Sprint 1.5 (Schema Refactor)

## Objective
Adapt the backend (`api` project) to the new unified database structure ("newversion"), which optimizes the business model by replacing technical names with natural nomenclatures (e.g., Account Slots to Profiles).

---

## Implementation Phases

### Phase 1: Database and Schema Sanitization
1. **Refactor Existing Migrations:** DO NOT delete the current `V*` files in `src/main/resources/db/migration`. The goal is to keep them but *refactor* them.
2. **Idempotency (IF NOT EXISTS):** Transform table creations to `CREATE TABLE IF NOT EXISTS`. The "prod" profile is configured with `baseline-version: 0`, therefore Flyway will execute all scripts. Since the Supabase DB *already has the tables created*, the scripts must pass without errors and without destroying anything.
3. **Live Schema Reading:** You should use your Supabase MCP to read the live structure of the production database ("newversion") and ensure that what is reflected in the `V*` scripts and your JPA Entities is identical to what you see.

### Phase 2: Domain and Infrastructure Layer (JPA)
1. **Entity Refactoring:** Rename and re-adapt attributes.
   * Modify `Product`/`Inventory` to `ServiceEntity`.
   * Modify `AccountSlotEntity` to `ProfileEntity`. Add `pin`, `is_owner`.
   * Modify `UserGuestEntity` to `ClientEntity`.
2. **Spring Data Repositories:** Adjust the interface names of `Repository` and custom JPQL/SQL queries to use the new relationships and IDs (caution: the new DB uses integers as primary keys and referential integrity already exists, let's add another UUID field to maintain compatibility with the frontend, so that the integer ID is only handled internally for relationships and the UUID is exposed in the frontend).
3. **Mappers:** Adjust the manual Mappers (Record DTOs to Entities).

### Phase 3: Application Layer and Ports (Use Cases)
1. **Inbound Ports:** Refactor UseCase interfaces (e.g., `CreateSlotUseCase` to `CreateProfileUseCase`).
2. **Outbound Ports:** Adjust the output contracts towards the DB.

### Phase 4: REST Controllers and Testing
1. **Endpoint Routes:** Migrate controllers. (E.g., `GET /api/account-slots` to `GET /api/profiles`).
2. **Testcontainers:** Update Unit and Integration Tests. Execute the full suite to certify that the Testcontainers mock loads `V1__init_unified_schema.sql` and all tests pass at 100%.

---
**Reading Restrictions:** The responsible agent MUST ONLY read the specific documentation of the altered modules in `docs/modules/` to avoid context contamination.