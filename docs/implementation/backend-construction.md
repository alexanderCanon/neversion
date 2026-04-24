# Bitácora de Construcción: Backend

Este documento es el registro oficial de cambios y decisiones técnicas tomadas durante la implementación del backend de la plataforma.

## Registro de Cambios

| Fecha | Épica / US | Descripción del Cambio | Decisión Técnica / Nota |
| :--- | :--- | :--- | :--- |
| 2026-04-23 | Setup | Finalización de fase de blindaje de documentación. | Estructura de base de datos y reglas de negocio confirmadas. |
| 2026-04-23 | EPIC-00 / US-001 | V7__create_users_table.sql — tabla `users` con `id` BIGINT IDENTITY, `uuid`, `external_id`, `role` VARCHAR lowercase. | `UserRole` enum, `User` domain model, `UserEntity`, `UserRoleConverter`, `JpaUserAdapter`, `UserPersistenceMapper`, `UserRepositoryPort`. Integration test `UserRepositoryIT` 8/8 PASS. Branch: `feature/backend`. |
| 2026-04-23 | EPIC-00 / US-002 | V8__create_vendors_table.sql — tabla `vendors` con FK a `users`, JSONB `bank_details` y `discount_cfg`. | `Vendor` domain, `VendorEntity` (userId como Long, sin @ManyToOne cross-module), `VendorRepositoryPort`, `JpaVendorAdapter` (bean: `vendorRepositoryAdapter`), `VendorPersistenceMapper`. `VendorRepositoryIT` 8/8 PASS. |
| 2026-04-23 | EPIC-00 / US-003 | V9__link_clients_to_users_vendors.sql — ALTER TABLE `clients` ADD `user_id`, `vendor_id` (nullable). | Update `Client` domain, `ClientEntity`, `ClientPersistenceMapper`. Nuevo `ClientRepositoryIT` 4/4 PASS. FKs nullable para backward compat. |
| 2026-04-23 | EPIC-00 / US-004 | V10__link_services_to_vendors.sql — ALTER TABLE `services` ADD `vendor_id` (nullable). | Update `Service` domain, `ServiceEntity`, `ServicePersistenceMapper`. Tests existentes PASS (backward compat). |
| 2026-04-23 | EPIC-00 / US-005 | V11__normalize_services_pricing.sql — ADD `description`, `image_url`, `price_profile`, `price_full`, `duration_days`, `is_active`. | Update domain + entity + mapper. `@Builder.Default isActive = true` en domain y entity. Tests PASS. |
| 2026-04-23 | EPIC-00 / US-006 | V12__complete_accounts_table.sql — ADD `cost`, `source`, `purchased_at`, `status`, `vendor_id`. | Fix `AccountStatus` (available/partial/full/expired). Update domain + entity + mapper. `@Builder.Default status = AVAILABLE`. Tests PASS. |
| 2026-04-23 | EPIC-00 / US-007 | V13__link_subscriptions_to_vendors.sql — ADD `vendor_id` to subscriptions. | Update domain + entity + mapper. `SubscriptionRepositoryIT` PASS. |
| 2026-04-23 | EPIC-00 / US-008 | V14__normalize_orders_pk_bigint.sql — DROP+recreate orders con BIGINT PK, uuid, vendor_id. | Rewrite completo: domain, entity, port (findByUuid), adapter, mapper, use case, controller, response DTO. ITs PASS. |
| 2026-04-23 | EPIC-00 / US-009+010+011 | V15__normalize_reservations_bigint.sql — DROP+recreate reservations + reservation_details con BIGINT PK, uuid, vendor_id, service_id. | Rewrite completo: 2 domain models, 2 entities, 2 repos, adapter, mapper, port, 3 services, controller, 4 DTOs. Fix 5 test files (4 UT + 1 IT). 23/23 PASS. |
| 2026-04-23 | EPIC-00 / Profiles | V16__link_profiles_to_vendors.sql — ADD `vendor_id` to profiles. | Update domain + entity + mapper. ITs PASS. |
