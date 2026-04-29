# Plan: EPIC-06 — Asignación y Entrega de Accesos (Backend)

## Context

EPIC-06 cubre el flujo operativo post-pago: el vendedor asigna un perfil o cuenta a una orden aprobada, se crea la suscripción y se notifica al cliente. Depende de EPIC-05 (órdenes) que ya está completado. La implementación vive en un nuevo módulo `assignment/` que orquesta ports existentes sin crear su propia capa de persistencia.

**Modelo recomendado:** Claude Sonnet 4.6 — implementación estándar con lógica de orquestación moderada.

**Prerequisitos verificados:**
- `Service.durationDays` (Integer, nullable) ya existe desde V11
- `Vendor` no tiene email — las alertas de sin-inventario se registran en `notification_log` con `vendorId` en el payload (agente externo resuelve destinatario)
- Migración siguiente: **V24**

---

## Fases de implementación

### Fase 1 — Migración V24

**Archivo nuevo:** `apps/api/src/main/resources/db/migration/V24__enrich_subscriptions_for_epic06.sql`

```sql
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS order_id BIGINT REFERENCES orders(id),
    ADD COLUMN IF NOT EXISTS end_date DATE;

CREATE INDEX IF NOT EXISTS idx_subscriptions_order_id ON subscriptions(order_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_end_date  ON subscriptions(end_date);
```

---

### Fase 2 — Enriquecimiento de dominio existente

Archivos a **modificar**:

| Archivo | Cambio |
|---------|--------|
| `subscription/domain/model/Subscription.java` | + `Long orderId`, `LocalDate endDate` |
| `subscription/infrastructure/adapters/out/SubscriptionEntity.java` | + `@Column order_id`, `end_date` |
| `subscription/infrastructure/adapters/out/SubscriptionPersistenceMapper.java` | mapear ambos campos |
| `subscription/domain/port/out/SubscriptionRepositoryPort.java` | + `Optional<Subscription> findByOrderId(Long orderId)` |
| `subscription/infrastructure/adapters/out/SpringDataSubscriptionRepository.java` | + derived query `findByOrderId` |
| `subscription/infrastructure/adapters/out/JpaSubscriptionAdapter.java` | implementar `findByOrderId` |
| `subscription/infrastructure/adapters/in/rest/dto/SubscriptionResponse.java` | + `LocalDate endDate` |
| `subscription/infrastructure/adapters/in/rest/mapper/SubscriptionMapper.java` | mapear `endDate` en `toResponse()` |
| `account/domain/port/out/AccountRepositoryPort.java` | + `List<Account> findByServiceIdAndVendorId(Long, Long)` |
| `account/infrastructure/adapters/out/SpringDataAccountRepository.java` | + derived query |
| `account/infrastructure/adapters/out/JpaAccountAdapter.java` | implementar el nuevo método |

---

### Fase 3 — Módulo `assignment/` (nuevo)

Ruta base: `apps/api/src/main/java/com/neversion/api/assignment/`

#### Puertos (interfaces use case)

**`application/port/in/SuggestAssignmentUseCase.java`**
```java
AssignmentSuggestion suggest(UUID orderUuid, String callerExternalId);
```

**`application/port/in/ConfirmAssignmentUseCase.java`**
```java
AssignmentResult confirm(UUID orderUuid, UUID profileUuid, String callerExternalId);
```

**`application/port/in/DeliverAccessUseCase.java`** (puerto interno, no expuesto via REST)
```java
void deliver(Subscription subscription);
```

**`application/port/in/ManualAssignmentUseCase.java`**
```java
AssignmentResult assign(UUID clientUuid, UUID serviceUuid, UUID profileUuid,
                        LocalDate startDate, LocalDate endDate, String callerExternalId);
```

**DTOs de puerto** (`application/port/in/dto/`):

```java
// AssignmentSuggestion.java
record AssignmentSuggestion(
    boolean hasSuggestion,
    SaleMode saleMode,
    UUID suggestedProfileUuid,   // perfil disponible; en FULL_ACCOUNT es el perfil dueño
    UUID suggestedAccountUuid,
    String serviceName,
    String accountEmail,
    String noInventoryReason     // non-null solo si hasSuggestion=false
) {}

// AssignmentResult.java
record AssignmentResult(
    UUID subscriptionUuid,
    UUID orderUuid,              // null en asignación manual
    UUID profileUuid,
    UUID clientUuid,
    String serviceName,
    LocalDate startDate,
    LocalDate endDate,
    boolean notificationQueued
) {}
```

---

#### Servicios

**`DeliverAccessService`** (US-041 — hoja, implementa `DeliverAccessUseCase`)
- `@Transactional(propagation = REQUIRES_NEW)` — transacción propia para no revertir la orden si falla
- Carga: profile → account → service → client
- Registra `notification_log` con type `ACCESS_DELIVERED` y payload JSON:
  ```json
  { "subscriptionId","serviceName","accountEmail","accountPassword",
    "profileName","pin","endDate","clientName" }
  ```
- `pin` se omite del payload si `profile.pin == null`

**`SuggestAssignmentService`** (US-039)
- Valida: caller es vendor dueño de la orden, orden en estado `VALIDATED`
- Carga accounts para `serviceId + vendorId` via nuevo `findByServiceIdAndVendorId`
- Branch por `saleMode`:
  - `BY_PROFILE`: busca primer perfil `AVAILABLE` entre los accounts del servicio
  - `FULL_ACCOUNT`: busca primer account con status `AVAILABLE` y devuelve su perfil dueño (`isOwner=true`) como `suggestedProfileUuid`
- Si no hay inventario: registra `notification_log` con type `NO_INVENTORY_ALERT` y payload `{"vendorId":"...","orderId":"...","serviceId":"..."}` (sin email — agente externo resuelve)
- Retorna `AssignmentSuggestion(hasSuggestion=false/true)`

**`ConfirmAssignmentService`** (US-040 + dispara US-041)
- `@Transactional`
- Validaciones: dueño de orden, orden en `VALIDATED`, perfil en `AVAILABLE` (→ 400 si no)
- Guard idempotencia: `subscriptionRepo.findByOrderId(order.id)` → 409 si ya existe
- Validación: `service.durationDays != null` (→ 400 "Service has no duration configured")
- Cálculo: `startDate = order.approvedAt → LocalDate (UTC)`, `endDate = startDate.plusDays(service.durationDays)`
- Transacción:
  - `BY_PROFILE`: profile → `ACTIVE`, crear `Subscription` (con `orderId`, `endDate`, `paymentDueDate = endDate`), orden → `COMPLETED` + entrada en `order_status_history`
  - `FULL_ACCOUNT`: el perfil dueño (`isOwner=true`) ancla `subscriptions.profile_id`; todos los perfiles de la cuenta → `ACTIVE`; account → `FULL`
- Post-commit (failure-safe):
  ```java
  try { deliverAccessUseCase.deliver(savedSubscription); }
  catch (Exception e) { log.error(...); /* no rethrow — orden ya está COMPLETED */ }
  ```
- **NO** reutiliza `ChangeOrderStatusUseCase` — opera directamente sobre `OrderRepositoryPort` para mantener atomicidad

**`ManualAssignmentService`** (US-042)
- `@Transactional`
- Validaciones: cliente y perfil pertenecen al vendor, perfil en `AVAILABLE` (→ 400), no existe suscripción activa para el perfil via `existsActiveByProfileId` (→ 409)
- Crea `Subscription` con `orderId = null`, fechas provistas por el vendor
- Post-commit failure-safe idéntico al de `ConfirmAssignmentService`

---

#### REST — `AssignmentController` → `/api/v1/assignments`

| Método | Path | US | Body | Response |
|--------|------|----|------|----------|
| GET | `/suggest/{orderUuid}` | US-039 | — | `SuggestAssignmentResponse` 200 |
| POST | `/confirm/{orderUuid}` | US-040 | `ConfirmAssignmentRequest` | `ConfirmAssignmentResponse` 201 |
| POST | `/manual` | US-042 | `ManualAssignmentRequest` | `ManualAssignmentResponse` 201 |

```java
// ConfirmAssignmentRequest
record ConfirmAssignmentRequest(@NotNull UUID profileId) {}

// ManualAssignmentRequest
record ManualAssignmentRequest(
    @NotNull UUID clientId,
    @NotNull UUID serviceId,
    @NotNull UUID profileId,
    @NotNull LocalDate startDate,
    @NotNull LocalDate endDate
) {}
```

**`AssignmentSecurityConfig`** — `HttpSecurityCustomizer`:
```java
.requestMatchers("/api/v1/assignments/**").hasAnyRole("VENDOR", "SUPER_ADMIN")
```

---

### Fase 4 — Tests

#### Unit (`*UT.java`, `@ExtendWith(MockitoExtension.class)`)

**`SuggestAssignmentServiceUT`**
- `suggest_byProfile_shouldReturnSuggestion_whenAvailableProfileExists`
- `suggest_byProfile_shouldReturnNoSuggestion_andLogAlert_whenNoInventory`
- `suggest_fullAccount_shouldReturnSuggestion_whenAvailableAccountExists`
- `suggest_shouldThrow403_whenVendorDoesNotOwnOrder`
- `suggest_shouldThrowBusinessRule_whenOrderNotValidated`

**`ConfirmAssignmentServiceUT`**
- `confirm_shouldSetProfileActive_createSubscription_completeOrder`
- `confirm_shouldComputeEndDateFromServiceDurationDays`
- `confirm_shouldThrow400_whenProfileNotAvailable`
- `confirm_shouldThrow400_whenServiceHasNoDurationDays`
- `confirm_shouldThrow409_whenAssignmentAlreadyConfirmed`
- `confirm_shouldNotRevertOrder_whenNotificationThrows`

**`DeliverAccessServiceUT`**
- `deliver_shouldRecordAccessDeliveredNotification_withFullPayload`
- `deliver_shouldOmitPin_whenProfileHasNoPin`

**`ManualAssignmentServiceUT`**
- `assign_shouldCreateSubscription_withVendorDates_andNotifyClient`
- `assign_shouldThrow400_whenProfileNotAvailable`
- `assign_shouldThrow409_whenProfileAlreadyHasActiveSubscription`
- `assign_shouldThrow403_whenVendorDoesNotOwnClient`

#### Integration (`*IT.java`, `@SpringBootTest`, Testcontainers)

**`AssignmentRepositoryIT`**
- Subscription creada con `orderId` y `endDate` persistidos (V24)
- Profile status = `active` en DB post-confirmación
- `findByOrderId` retorna la subscription creada
- Order status = `completed` post-confirmación
- `notification_log` tiene fila `ACCESS_DELIVERED`
- Manual assignment crea subscription con `orderId = null`
- Tests de `REQUIRES_NEW` no usan `@Transactional` a nivel de test

---

## Orden de implementación (12 pasos)

```
[1]  V24 migration
[2]  Subscription: domain + entity + mapper + repo (findByOrderId)
[3]  Account repo: findByServiceIdAndVendorId
[4]  SubscriptionResponse + SubscriptionMapper (endDate)
[5]  Puertos e interfaces del módulo assignment/
[6]  DeliverAccessService                        ← US-041
[7]  SuggestAssignmentService                    ← US-039
[8]  ConfirmAssignmentService                    ← US-040
[9]  ManualAssignmentService                     ← US-042
[10] AssignmentRestMapper + Controller + SecurityConfig
[11] Unit tests (se escriben junto con cada servicio)
[12] AssignmentRepositoryIT
```

> Cada paso = un módulo completo. Tests manuales por Alex antes de avanzar al siguiente.

---

## Archivos críticos a modificar (existentes)

- `apps/api/src/main/java/com/neversion/api/subscription/domain/model/Subscription.java`
- `apps/api/src/main/java/com/neversion/api/subscription/infrastructure/adapters/out/SubscriptionEntity.java`
- `apps/api/src/main/java/com/neversion/api/subscription/infrastructure/adapters/out/SubscriptionPersistenceMapper.java`
- `apps/api/src/main/java/com/neversion/api/subscription/domain/port/out/SubscriptionRepositoryPort.java`
- `apps/api/src/main/java/com/neversion/api/subscription/infrastructure/adapters/out/JpaSubscriptionAdapter.java`
- `apps/api/src/main/java/com/neversion/api/subscription/infrastructure/adapters/in/rest/dto/SubscriptionResponse.java`
- `apps/api/src/main/java/com/neversion/api/account/domain/port/out/AccountRepositoryPort.java`
- `apps/api/src/main/java/com/neversion/api/account/infrastructure/adapters/out/JpaAccountAdapter.java`

Todos los demás archivos del módulo `assignment/` son **nuevos**.
