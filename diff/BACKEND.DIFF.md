# Neversion Backend — Resumen de Refactorización REST Layer
**Fecha:** 2026-04-02 | **Branch:** `refactor`
**Resultado:** ✅ BUILD SUCCESS — 250 fuentes compiladas, 0 errores

---

## 📊 Estadísticas Globales (git diff HEAD)

| Métrica | Valor |
|---|---|
| Archivos modificados | 55 |
| Líneas insertadas | +537 |
| Líneas eliminadas | -1,395 |
| **Saldo neto** | **-858 líneas** (limpieza masiva) |

---

## 🏗️ Contexto: ¿Por qué se refactorizó?

El esquema de base de datos fue reemplazado por `V1__init_unified_schema.sql`. El esquema anterior usaba:
- PKs por UUID en tablas (`id UUID PRIMARY KEY`)
- Tablas separadas: `products`, `inventory`, `reservations`, `orders`, `account_slots`

El nuevo esquema unificado usa:
- PKs numéricas internas (`id SERIAL PRIMARY KEY`) + UUID externo (`uuid UUID DEFAULT gen_random_uuid()`)
- Tablas consolidadas: `services`, `profiles`, `clients` (antes `user_guests`)
- Sin tablas de reservas ni órdenes en Sprint 1

---

## 📁 MÓDULO: `account`

### Dominio — `Account.java`
| Campo | Antes | Después |
|---|---|---|
| `accountType` | `AccountType` enum | ❌ Eliminado (delegado a `services`) |
| `inventoryId` | UUID | ❌ Eliminado |
| `seller` | `String` | ❌ Eliminado |
| `isActive` | `Boolean` | ❌ Eliminado |
| `expirationDate` | `LocalDate` | ❌ Eliminado |
| `serviceId` | — | ✅ `Long` (FK interna a `services.id`) |
| `saleMode` | — | ✅ `SaleMode` enum (`BY_PROFILE` / `FULL_ACCOUNT`) |
| `renewalDate` | — | ✅ `LocalDate` |
| `plan` | — | ✅ `String` |
| `uuid` | — | ✅ `UUID` (identificador externo) |
| `password` | `pass` (campo renombrado) | ✅ `password` |
| Lombok | `@Getter @Setter @Builder` + constructores manuales | ✅ Agregado `@NoArgsConstructor` |

### Entidad JPA — `AccountEntity.java`
| Aspecto | Antes | Después |
|---|---|---|
| Tabla | `accounts` (UUID PK) | `accounts` (SERIAL PK + uuid externo) |
| `@Id` tipo | `UUID` | ✅ `Long` con `IDENTITY` |
| Columnas nuevas | — | `uuid`, `service_id`, `sale_mode`, `plan`, `renewal_date`, `notes`, `created_at` |
| Columnas removidas | `account_type`, `inventory_id`, `seller`, `is_active`, `expiration_date` | ❌ Eliminadas |

### REST — DTOs
| Archivo | Antes | Después |
|---|---|---|
| `AccountRequest.java` | `inventoryId`, `AccountType`, `expirationDate`, `seller` | ✅ `serviceId` (Long), `saleMode`, `renewalDate`, `notes` |
| `AccountResponse.java` | `slotCount`, `availableSlots`, `AccountType` | ✅ `id` (UUID), `serviceId`, `saleMode`, `renewalDate`, `createdAt` |

### REST — Mapper
| Archivo | Antes | Después |
|---|---|---|
| `AccountMapper.java` | Inyectaba `AccountSlotRepositoryPort` para contar slots | ✅ Mapper simple, mapea campos del dominio actual |

### REST — Controladores
| Archivo | Estado |
|---|---|
| `AccountGetController.java` | ❌ **ELIMINADO** (legacy, métodos `getBySeller`, `getByAccountType`, `getByIsActive` inexistentes) |
| `AccountPostController.java` | ❌ **ELIMINADO** (legacy) |
| `AccountDeleteController.java` | ❌ **ELIMINADO** (legacy) |
| `AccountController.java` | ✅ **NUEVO** — CRUD unificado vía `AccountUseCase`, operaciones por UUID |

### Application — Servicios
| Archivo | Estado |
|---|---|
| `CreateAccountService.java` | ✅ **ACTUALIZADO** — resuelve `serviceId` por FK, auto-genera perfiles (BR-01), corregido conflicto de nombre `Service` |
| `DeactivateAccountService.java` | ❌ **ELIMINADO** (llamaba `deactivate()` inexistente en `AccountRepositoryPort`) |

---

## 📁 MÓDULO: `accountslot` → renombrado conceptualmente a `profile`

> **Cambio de nombre de dominio:** `AccountSlot` → `Profile` (lenguaje de negocio más natural)

### Dominio — `Profile.java` *(nuevo)*
| Campo | Descripción |
|---|---|
| `id` (Long) | PK interna |
| `uuid` (UUID) | Identificador externo REST |
| `accountId` (Long) | FK a `accounts.id` |
| `name` | Nombre del perfil en la plataforma |
| `pin` | PIN de 4 dígitos |
| `isOwner` | Perfil administrador del account |
| `createdAt` | Timestamp |
| Lombok | `@Getter @Setter @Builder @NoArgsConstructor` + constructor manual |

### Dominio — `AccountSlot.java` *(legacy, mantenido temporalmente)*
- Corregido Lombok: `@NoArgsConstructor @AllArgsConstructor` reemplazaron constructores manuales

### Entidad JPA — `ProfileEntity.java` *(nueva)*
- Tabla: `profiles` (antes `account_slots`)
- PK: `Long` con `IDENTITY`
- UUID externo: `gen_random_uuid()`
- `is_owner` como columna booleana

### Entidad JPA — `AccountSlotEntity.java` *(legacy, corregida)*
- Corregido Lombok: `@NoArgsConstructor @AllArgsConstructor`

### REST — Archivos nuevos
| Archivo | Descripción |
|---|---|
| `ProfileRequest.java` | `accountId`, `name`, `pin`, `isOwner` |
| `ProfileResponse.java` | `id` (UUID), `accountId`, `name`, `pin`, `isOwner`, `createdAt` |
| `ProfileMapper.java` | `toDomain()` + `toResponse()` |
| `ProfileController.java` | CRUD: `GET /profiles`, `GET /profiles/{uuid}`, `GET /profiles/account/{accountId}`, `POST`, `PUT`, `DELETE` |

### REST — Eliminados
| Archivo | Razón |
|---|---|
| `AccountSlotController.java` | ❌ **ELIMINADO** — usaba `setProfileName()`, `setPin()` inexistentes en nuevo `AccountSlot` |
| `AccountSlotMapper.java` | ❌ **ELIMINADO** — referenciaba campos obsoletos |
| `AccountSlotService.java` | ❌ **ELIMINADO** — `Profile.builder()` fallaba, reemplazado por `ProfileService` |

---

## 📁 MÓDULO: `subscription`

### Dominio — `Subscription.java`
| Campo | Antes | Después |
|---|---|---|
| `accountSlotId` (UUID) | FK al slot | ❌ Eliminado |
| `userGuestId` (UUID) | FK al guest | ❌ Eliminado |
| `orderId` | UUID | ❌ Eliminado |
| `renewalDate` | `LocalDate` | ❌ Renombrado |
| `profileId` (Long) | — | ✅ FK interna a `profiles.id` |
| `clientId` (Long) | — | ✅ FK interna a `clients.id` |
| `paymentDueDate` | — | ✅ `LocalDate` (= antes `renewalDate`) |
| `price` | — | ✅ `BigDecimal` |
| `purchaseDate` | — | ✅ `LocalDate` |
| `profileUuid` (transient) | — | ✅ Campo transient para resolución REST→Long |
| `clientUuid` (transient) | — | ✅ Campo transient para resolución REST→Long |

### Entidad JPA — `SubscriptionEntity.java`
| Aspecto | Antes | Después |
|---|---|---|
| `account_slot_id` | UUID FK | ❌ Eliminado |
| `user_guest_id` | UUID FK | ❌ Eliminado |
| `order_id` | UUID FK | ❌ Eliminado |
| `start_date` | `LocalDate` | ❌ Renombrado |
| `renewal_date` | `LocalDate` | ❌ Renombrado |
| `months_paid` | `Integer` | ❌ Eliminado |
| `profile_id` (Long) | — | ✅ FK a `profiles.id` |
| `client_id` (Long) | — | ✅ FK a `clients.id` |
| `payment_due_date` | — | ✅ `LocalDate` |
| `price` | — | ✅ `BigDecimal` |
| `purchase_date` | — | ✅ `LocalDate` |
| `@PrePersist` | — | ✅ Auto-fills `purchaseDate` y `uuid` |

### REST — DTOs
| Archivo | Antes | Después |
|---|---|---|
| `CreateSubscriptionRequest.java` | `accountSlotId`, `userGuestId`, `renewalDate` | ✅ `profileId` (UUID externo), `clientId` (UUID externo), `paymentDueDate`, `price`, `notes` |
| `SubscriptionResponse.java` | `accountSlotId`, `userGuestId`, `orderId` | ✅ `profileId` (UUID), `clientId` (UUID), `paymentDueDate`, `price`, `purchaseDate` |

### REST — Controladores
| Archivo | Estado |
|---|---|
| `SubscriptionGetController.java` | ❌ **ELIMINADO** (usaba `findByUserGuestId`, `findByAccountId` inexistentes) |
| `SubscriptionPostController.java` | ❌ **ELIMINADO** (legacy) |
| `SubscriptionPutController.java` | ❌ **ELIMINADO** (legacy) |
| `SubscriptionController.java` | ✅ **NUEVO** — CRUD unificado con resolución UUID→Long, filtros por `clientId`/`profileId`/`status` |

### Application — Servicios
| Archivo | Estado |
|---|---|
| `SubscriptionService.java` | ✅ **ACTUALIZADO** — implementa `AssignSubscriptionUseCase`, resuelve UUID→Long para `profile` y `client`, valida anti-overbooking (BR-04) |
| `UpdateSubscriptionService.java` | ✅ Sin cambios — `suspend()` y `terminate()` funcionan igual |
| `GetSubscriptionDashboardService.java` | ❌ **ELIMINADO** (llamaba `findDashboard()` inexistente) |

### Application — Ports
| Archivo | Estado |
|---|---|
| `AssignSubscriptionUseCase.java` | ✅ Recreado (fue eliminado por error) |
| `AssignAccountUseCase.java` | ❌ **ELIMINADO** (legacy) |
| `GetSubscriptionDashboardUseCase.java` | ❌ **ELIMINADO** (legacy) |

### Domain Port — `SubscriptionRepositoryPort.java`
- ✅ Archivo corregido (tenía contenido duplicado/corrupto)
- `findByClientId(Long)`, `findByProfileId(Long)` (antes `findByUserGuestId`, `findByAccountId`)
- Agregados: `findOverdue(LocalDate)`, `existsActiveByProfileId(Long)`

---

## 📁 MÓDULO: `userguest` → `client`

### Archivos nuevos
| Archivo | Descripción |
|---|---|
| `ClientRequest.java` | `name`, `phone`, `email`, `notes` |
| `ClientResponse.java` | `id` (UUID), `name`, `phone`, `email`, `notes`, `createdAt` |
| `ClientMapper.java` | `toDomain()` + `toResponse()` |
| `ClientController.java` | CRUD: `GET /clients`, búsqueda por nombre/teléfono, `POST`, `PUT`, `DELETE` |

> El dominio `Client.java` y el use case `ClientUseCase.java` ya existían — solo se añadió la capa REST.

---

## 📁 MÓDULO: `inventory` → `service`

### Dominio — `Service.java`
| Campo | Antes | Después |
|---|---|---|
| `id` (Long) | ✅ Existía | Sin cambio |
| `uuid` (UUID) | ✅ Existía | Sin cambio |
| `name` | ✅ Existía | Sin cambio |
| `maxProfiles` | ✅ Existía | Sin cambio |
| `details` (JsonNode JSONB) | ✅ Existía | Sin cambio |
| `createdAt` | — | ✅ **AGREGADO** |

### Entidad JPA — `ServiceEntity.java`
| Aspecto | Antes | Después |
|---|---|---|
| `maxProfiles` | `nullable = false` | ✅ Ahora nullable (alineado con schema real) |
| `createdAt` | — | ✅ Agregado con `@PrePersist` |
| `uuid` auto-fill | — | ✅ `@PrePersist` genera UUID si es null |

### Persistence Mapper — `ServicePersistenceMapper.java`
- ✅ Agregado mapeo de `createdAt`

### REST — Archivos nuevos
| Archivo | Descripción |
|---|---|
| `ServiceRequest.java` | `name`, `maxProfiles`, `details` (JSONB) |
| `ServiceResponse.java` | `id` (UUID), `name`, `maxProfiles`, `details`, `createdAt` |
| `ServiceMapper.java` | Con soporte JSONB via `ObjectMapper` |
| `ServiceController.java` | CRUD: `GET /services`, `GET /services/{uuid}`, `POST`, `PUT`, `DELETE` |

---

## 📁 Migraciones Flyway — ❌ ELIMINADAS (todas)

Las siguientes migraciones fueron eliminadas porque ya **no aplican** al nuevo esquema base `V1__init_unified_schema.sql`:

| Migración | Contenido |
|---|---|
| `V2__create_table_products.sql` | Tabla `products` (reemplazada por `services`) |
| `V3__create_table_inventory.sql` | Tabla `inventory` (reemplazada por JSONB en `services`) |
| `V4__create_table_users_guest.sql` | Tabla `users_guest` (renombrada a `clients`) |
| `V5__create_table_accounts.sql` | Esquema de accounts antiguo |
| `V6__create_table_subscriptions.sql` | Esquema de subscriptions antiguo |
| `V7__create_table_reservations.sql` | Módulo de reservas (fuera de Sprint 1) |
| `V8__create_table_orders.sql` | Módulo de órdenes (fuera de Sprint 1) |
| `V9` a `V18` | Parches de enums, casts, correcciones al esquema antiguo |

> **Nota importante:** La migración activa es solo `V1__init_unified_schema.sql` con `baseline-version: 0` en `application-prod.yaml`.

---

## 🔄 Flujo de resolución UUID → Long ID

Este es el patrón central introducido en esta refactorización:

```
REST Request (UUID externo)
         │
         ▼
  Controller / SubscriptionService
         │  profileRepositoryPort.findById(profileUuid) → Profile
         │  clientRepositoryPort.findById(clientUuid)  → Client
         ▼
  Domain Model (Long interno)
         │  subscription.setProfileId(profile.getId())
         │  subscription.setClientId(client.getId())
         ▼
  SubscriptionRepositoryPort.save() → JPA Entity → DB
         ▼
  Response con UUID externo (nunca el Long)
```

---

## ✅ Checklist de alineación con esquema unificado

| Tabla DB (`V1`) | Entidad JPA | Domain Model | Controller REST |
|---|---|---|---|
| `services` | `ServiceEntity` ✅ | `Service` ✅ | `ServiceController` ✅ |
| `accounts` | `AccountEntity` ✅ | `Account` ✅ | `AccountController` ✅ |
| `profiles` | `ProfileEntity` ✅ | `Profile` ✅ | `ProfileController` ✅ |
| `clients` | `ClientEntity` ✅ | `Client` ✅ | `ClientController` ✅ |
| `subscriptions` | `SubscriptionEntity` ✅ | `Subscription` ✅ | `SubscriptionController` ✅ |

---

## ⚠️ Items pendientes post-refactor

1. **Tests** — Los tests de integración (`BaseIntegrationTest`, builders) aún referencian el esquema antiguo. Deben actualizarse antes de poder ejecutar `mvn test`.
2. **`AccountSlot` / `AccountSlotEntity`** — Clases legacy aún presentes. Evaluar si se eliminan completamente o se mantienen para compatibilidad temporal.
3. **`account_slots` tabla** — Ya no existe en el esquema unificado. Si `AccountSlotEntity` se sigue usando, generará error en runtime.
4. **`GetAccountUseCase`** — Hay advertencias de line endings (LF→CRLF). No afecta compilación.
