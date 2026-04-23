# Bitácora de Construcción: Backend

Este documento es el registro oficial de cambios y decisiones técnicas tomadas durante la implementación del backend de la plataforma.

## Registro de Cambios

| Fecha | Épica / US | Descripción del Cambio | Decisión Técnica / Nota |
| :--- | :--- | :--- | :--- |
| 2026-04-23 | Setup | Finalización de fase de blindaje de documentación. | Estructura de base de datos y reglas de negocio confirmadas. |
| 2026-04-23 | EPIC-00 / US-001 | V7__create_users_table.sql — tabla `users` con `id` BIGINT IDENTITY, `uuid`, `external_id`, `role` VARCHAR lowercase. | `UserRole` enum, `User` domain model, `UserEntity`, `UserRoleConverter`, `JpaUserAdapter`, `UserPersistenceMapper`, `UserRepositoryPort`. Integration test `UserRepositoryIT` 8/8 PASS. Branch: `feature/backend`. |
| 2026-04-23 | EPIC-00 / US-002 | V8__create_vendors_table.sql — tabla `vendors` con FK a `users`, JSONB `bank_details` y `discount_cfg`. | `Vendor` domain, `VendorEntity` (userId como Long, sin @ManyToOne cross-module), `VendorRepositoryPort`, `JpaVendorAdapter` (bean: `vendorRepositoryAdapter`), `VendorPersistenceMapper`. `VendorRepositoryIT` 8/8 PASS. |
