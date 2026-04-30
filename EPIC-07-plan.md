EPIC-07 — Suscripciones: Plan de Implementación
Gestión del ciclo de vida de suscripciones activas: listado filtrado por vendedor, detalle con origen comercial, renovación con lógica BR-07, revocación con liberación de perfiles/cuentas, detección automática de vencimientos (cron), y creación manual sin orden previa.

IMPORTANT

Modelo sugerido: Claude Opus 4.7 / Sonnet 4.6 — BR-07 requiere lógica de negocio compleja con cálculos de fechas condicionales. Las cascadas de estado bidireccionales (suscripción ↔ perfil ↔ cuenta) requieren razonamiento alto.

Scope
Cubre: US-043 a US-048 (6 stories). Lifecycle completo de suscripciones: CRUD de lectura, renovación, revocación, detección automática de vencimientos, y creación manual.

No cubre: Envío real de emails (solo notification_log). No toca la UI (EPIC-09). No implementa el worker de notificaciones (EPIC-08).

Estado actual del módulo subscription
El módulo ya tiene infraestructura base de EPIC-06:

Domain: Subscription, SubStatus (ACTIVE, PENDING, SUSPENDED, CANCELLED)
Ports: SubscriptionRepositoryPort con CRUD completo + findOverdue + existsActiveByProfileId
Services: SubscriptionService (assign), UpdateSubscriptionService (suspend/terminate) — sin ownership check ni cascada a perfiles
Controller: endpoints básicos sin JWT/ownership check
Missing: No hay sale_mode en Subscription. Se resuelve vía Profile → Account → SaleMode. No hay query filtrada por vendorId + status + serviceId.

User Review Required
IMPORTANT

US-044 — Snapshots financieros: La story dice "Pendiente de decisión: si se agregan price_sold, discount_applied, sale_mode, service_id". ¿Los implementamos ahora (V24 migration + 4 columnas nuevas) o se dejan para un futuro? Mi recomendación: agregarlos ahora como campos opcionales — son baratos de implementar y el frontend los necesitará para el detalle.

Alex response: "Si agregarlos ahora"

IMPORTANT

US-048 — Crear suscripción manual: El SubscriptionService.assign() ya existe de EPIC-06 pero carece de: ownership check, endDate/paymentDueDate calculation, snapshots financieros, y notificación opcional. ¿Lo refactorizamos para que sirva tanto al flujo automático (EPIC-06) como al manual (US-048)? Mi recomendación: refactorizar — un solo punto de creación con un flag manual=true/false.

Alex response: "Si refactorizamos para que sirva tanto al flujo automático (EPIC-06) como al manual (US-048)"

Open Questions
WARNING

Q1 — Grace period config: ADR-07 dice que el valor de gracia (2 días) va en application.yml. ¿Ya está configurado o lo agrego yo? Lo busco antes de codificar.

Alex response: "Buscalo y si no esta entonces agregalo, debe ir en todos los perfiles, dev, local, prod, test, etc si aplica"

WARNING

Q2 — US-047 Cron trigger: La story dice "cron job". ¿Lo implemento como @Scheduled dentro de Spring Boot, o Alex lo maneja externamente con n8n y solo expongo un endpoint POST /api/v1/subscriptions/detect-expired? Mi recomendación: @Scheduled + endpoint manual para testing, controlado por property neversion.cron.subscription-expiry.enabled=true.

Alex response: "Entonces Scheduled + endpoint manual"

Proposed Changes
Phase 1 — Infraestructura & Listado (Módulos 1-2)
Module 1: US-043 — Listar suscripciones por vendedor
Cambios:

[MODIFY] 
SubscriptionRepositoryPort.java
Nuevo método: findByVendorIdFiltered(Long vendorId, Long serviceId, SubStatus status) con sort ASC por paymentDueDate
[MODIFY] 
SpringDataSubscriptionRepository.java
JPQL query con filtros opcionales y ORDER BY s.paymentDueDate ASC
Necesita JOIN nativo a profiles → accounts → services para filtrar por serviceId
[MODIFY] 
JpaSubscriptionAdapter.java
Implementar nuevo método del port
[NEW] ListSubscriptionsUseCase.java
Interface: List<Subscription> listByVendor(UUID vendorUuid, Long serviceId, SubStatus status, String callerExternalId)
[NEW] ListSubscriptionsService.java
Ownership check (JWT → User → Vendor → verify vendorId)
Delega a repository filtrado
[MODIFY] 
SubscriptionResponse.java
Agregar: serviceName, clientName, profileName (resolver en controller o service)
[MODIFY] 
SubscriptionController.java
Nuevo endpoint: GET /vendor/{vendorUuid}?serviceId=X&status=Y con JWT
Reemplaza el GET / actual (sin ownership check)
Tests: ListSubscriptionsServiceUT.java — 4 tests (happy path, ownership 403, filters, empty)

Module 2: US-044 — Ver detalle de suscripción
Cambios:

[NEW] V24 migration (si Alex aprueba snapshots)
ALTER TABLE subscriptions ADD COLUMN service_id BIGINT REFERENCES services(id)
ALTER TABLE subscriptions ADD COLUMN price_sold NUMERIC(10,2)
ALTER TABLE subscriptions ADD COLUMN discount_applied NUMERIC(10,2)
ALTER TABLE subscriptions ADD COLUMN sale_mode VARCHAR(20)
[NEW] SubscriptionDetailResponse.java
Order data, client data, profile data, account data, snapshots financieros
[NEW] GetSubscriptionDetailUseCase.java
SubscriptionDetail getDetail(UUID subscriptionUuid, String callerExternalId)
[NEW] GetSubscriptionDetailService.java
Ownership check
Load subscription + resolve profile + account + client + order (si existe)
[MODIFY] 
SubscriptionController.java
Refactor GET /{id} para retornar SubscriptionDetailResponse con JWT + 403
Tests: GetSubscriptionDetailServiceUT.java — 3 tests (happy, 403, 404)

Phase 2 — Lifecycle (Módulos 3-4)
Module 3: US-045 — Renovar suscripción
Cambios:

[NEW] RenewSubscriptionUseCase.java
Subscription renew(UUID subscriptionUuid, String callerExternalId)
[NEW] RenewSubscriptionService.java — Core business logic BR-07
Ownership check
Validate: only ACTIVE or SUSPENDED can be renewed
BR-07 calculation:
daysOverdue = ChronoUnit.DAYS.between(paymentDueDate, today)
If daysOverdue <= gracePeriodDays (2): new paymentDueDate = old + 30 days
If daysOverdue > gracePeriodDays: new paymentDueDate = today + 30 days
Increment monthsPaid
Set status → ACTIVE
Profile → ACTIVE
If FULL_ACCOUNT: all profiles in account → ACTIVE, account → FULL
Notification: SUBSCRIPTION_RENEWED to client
[NEW] SubscriptionRenewalDomainService.java
Pure domain service for BR-07 date calculation (testable without mocks)
LocalDate calculateNewDueDate(LocalDate currentDueDate, LocalDate paymentDate, int gracePeriodDays)
[MODIFY] 
SubscriptionController.java
New endpoint: PUT /{id}/renew with JWT
Tests:

SubscriptionRenewalDomainServiceUT.java — 4 tests (within grace, outside grace, exact boundary, same day)
RenewSubscriptionServiceUT.java — 5 tests (happy BY_PROFILE, happy FULL_ACCOUNT, 403, wrong status, notification)
Module 4: US-046 — Revocar acceso
Cambios:

[NEW] RevokeSubscriptionUseCase.java
Subscription revoke(UUID subscriptionUuid, String callerExternalId)
[NEW] RevokeSubscriptionService.java
Ownership check
Validate: not already CANCELLED → 400
Status → CANCELLED
If BY_PROFILE: profile → AVAILABLE
If FULL_ACCOUNT: all profiles → AVAILABLE, account → AVAILABLE
Notification: ACCESS_REVOKED to client
[MODIFY] 
SubscriptionController.java
Refactor PUT /{id}/cancel to use new RevokeSubscriptionService with ownership check + cascading
Tests: RevokeSubscriptionServiceUT.java — 5 tests (happy BY_PROFILE, happy FULL_ACCOUNT, 403, already cancelled, notification)

Phase 3 — Automatización & Creación Manual (Módulos 5-6)
Module 5: US-047 — Detectar suscripciones vencidas (cron)
Cambios:

[NEW] DetectExpiredSubscriptionsUseCase.java
int detectAndSuspend()
[NEW] DetectExpiredSubscriptionsService.java
Query: paymentDueDate <= today AND status = ACTIVE
For each: status → SUSPENDED
If BY_PROFILE: profile → EXPIRED
If FULL_ACCOUNT: all profiles → EXPIRED, account → EXPIRED
Aggregate vendor notifications: one SUBSCRIPTIONS_EXPIRED_DAILY per vendor with summary
[NEW] SubscriptionExpiryScheduler.java
@Scheduled(cron = "0 0 2 * * *") — runs daily at 2 AM
Controlled by @ConditionalOnProperty("neversion.cron.subscription-expiry.enabled")
[MODIFY] 
SubscriptionController.java
New endpoint: POST /detect-expired (SUPER_ADMIN only, for manual trigger/testing)
[MODIFY] application.yml
Add neversion.cron.subscription-expiry.enabled: true
Add neversion.renewal.grace-period-days: 2
Tests: DetectExpiredSubscriptionsServiceUT.java — 4 tests (happy BY_PROFILE, happy FULL_ACCOUNT, no expired, vendor notification aggregation)

Module 6: US-048 — Crear suscripción manual
Cambios:

[NEW] CreateManualSubscriptionUseCase.java
[NEW] CreateManualSubscriptionService.java
Ownership check (vendor must own the profile/client)
No order/reservation required
Set profile → ACTIVE
If FULL_ACCOUNT: all profiles → ACTIVE, account → FULL
Optional notification controlled by request flag sendNotification
[NEW] CreateManualSubscriptionRequest.java
Fields: clientUuid, profileUuid, serviceId, priceSold, discountApplied, paymentDueDate, notes, sendNotification
[MODIFY] 
SubscriptionController.java
Refactor POST / to use new service with ownership + cascading
Tests: CreateManualSubscriptionServiceUT.java — 4 tests (happy BY_PROFILE, happy FULL_ACCOUNT, 403, overbooking BR-04)

Modules Summary
#	US	Description	New Files	Test Strategy
1	US-043	List subscriptions (vendor filtered)	UseCase, Service, UT (4)	UT
2	US-044	Subscription detail + snapshots	V24 migration, DetailResponse, UseCase, Service, UT (3)	UT
3	US-045	Renew subscription (BR-07)	UseCase, Service, DomainService, UT (9)	UT
4	US-046	Revoke access (cascade profiles)	UseCase, Service, UT (5)	UT
5	US-047	Detect expired (cron + cascade)	UseCase, Service, Scheduler, UT (4)	UT
6	US-048	Create manual subscription	UseCase, Service, Request DTO, UT (4)	UT
Total estimated tests: ~29 new unit tests

Verification Plan
Automated Tests
After each module:

bash
./mvnw test -Dtest="<TestClass>" -Dsurefire.failIfNoSpecifiedTests=false
Module Gate (full suite)
After all 6 modules:

bash
./mvnw test
Manual Verification
Alex runs pnpm api:sync to verify OpenAPI spec generates cleanly
Alex tests renewal logic manually with edge cases (exact 2-day boundary)

Alex final comment: "Recuerda llevar actualizadas las bitacoras, documentar decisiones criticas, y los diagramas mermaid. Si comienzas a tener problemas te detienes e informas de inmediato para resolver mediante discusion"