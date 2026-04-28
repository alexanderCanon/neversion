# Análisis de Complejidad — EPICs Restantes (Backend)

## Resumen ejecutivo

| EPIC | US | Complejidad | Razonamiento | Código existente | Modelo recomendado |
|---|:---:|:---:|:---:|:---:|---|
| **EPIC-04** Clientes | 4 | 🟢 Baja | Bajo | ~80% scaffolded | **Sonnet** |
| **EPIC-05** Órdenes | 6 | 🟡 Media | Medio | ~60% scaffolded | **Sonnet** |
| **EPIC-06** Asignación | 4 | 🔴 Alta | **Alto** | ~30% scaffolded | **Opus** |
| **EPIC-07** Suscripciones | 6 | 🔴 Alta | **Alto** | ~50% scaffolded | **Opus** |
| **EPIC-08** Notificaciones | 8 | 🟡 Media | Medio | 0% (greenfield) | **Sonnet** / Opus |
| **EPIC-09** Panel Cliente | 6 | 🟢 Baja | Bajo | N/A (frontend) | **Gemini** |
| **EPIC-10** KPIs | 5 | 🟡 Media | Medio | 0% (queries) | **Sonnet** |
| **EPIC-11** Migración | 5 | 🟢 Baja | Bajo | Reusa US-031/048 | **Sonnet** |

---

## Análisis detallado por EPIC

---

### 🟢 EPIC-04 — Gestión de Clientes (Sonnet)

**US:** 029 (Listar) · 030 (Detalle) · 031 (Crear manual) · 032 (Editar)

**¿Por qué es simple?**
- El módulo `client/` ya está **completamente scaffolded**: domain, entity, mapper, repository, controller, security config, request/response DTOs — todo existe desde EPIC-00.
- El patrón es **idéntico** al de EPIC-03 (ownership por vendorId, listado con filtros, detalle con hijos, CRUD con JWT).
- La única pieza nueva es US-031 que crea un `User` con rol `CLIENT` + envío de email de bienvenida — pero la creación de User ya existe en EPIC-01 (`RegisterClientService`).

**Razonamiento requerido:** Mínimo. Es "seguir el patrón de EPIC-03 sobre código que ya existe".

**Riesgo:** US-031 necesita coordinación con Supabase (crear usuario + enviar email de reset). Si se decide que el backend NO crea la cuenta Supabase (como en ADR-09 revised), entonces es aún más simple.

**Estimación:** ~1-2 horas de sesión.

---

### 🟡 EPIC-05 — Órdenes y Comprobantes (Sonnet)

**US:** 033 (Reservación) · 034 (Subir comprobante) · 035 (Aprobar) · 036 (Rechazar) · 037 (Listar pendientes) · 038 (Detalle)

**¿Por qué es media?**
- Los módulos `reservation/` y `order/` ya están **scaffolded al ~60%**: domain models, entities, repositories, pricing service, scheduler de expiración — todo existe.
- **US-033** (crear reservación) es la más compleja — involucra carrito multi-item, pricing con descuento combo, bloqueo de perfiles, timer de expiración 1h. Pero `CreateReservationService`, `ReservationPricingService`, y `ReservationExpirationScheduler` ya están implementados parcialmente.
- **US-034** (subir comprobante) ya tiene `UploadReceiptService` + integración S3 implementada.
- **US-035/036** ya tienen `ValidateReservationService` con lógica de transición de estados.
- **US-037/038** son lecturas puras — triviales.

**Razonamiento requerido:** Medio. Las state machines de reservación (`pending → uploaded → validated/cancelled`) y la coordinación de transiciones entre módulos (reservación ↔ orden ↔ perfil status) necesitan cuidado, pero la lógica ya está documentada.

**Riesgo:** La integración S3 puede necesitar ajustes de configuración en tests. El scheduler de expiración puede necesitar tests específicos.

**Estimación:** ~2-3 horas de sesión.

---

### 🔴 EPIC-06 — Asignación y Entrega (Opus)

**US:** 039 (Sugerir recurso) · 040 (Confirmar asignación) · 041 (Entregar accesos) · 042 (Asignación manual)

**¿Por qué es compleja?**

Esta es la **épica más difícil del proyecto** por las siguientes razones:

1. **Orquestación multi-módulo:** Un solo flujo (US-040: confirmar asignación) toca **5 módulos simultáneamente**:
   - `Profile` → status `available` → `active`
   - `Subscription` → crear registro nuevo
   - `Order` → status `pending` → `completed`
   - `Account` → recalcular status (available/partial/full)
   - `Notification` → enviar credenciales por email

2. **US-039: Algoritmo de sugerencia.** No es CRUD — requiere lógica de selección inteligente:
   - Si `BY_PROFILE` → buscar perfil `available` en cuentas del servicio correspondiente.
   - Si `FULL_ACCOUNT` → buscar cuenta completa `available`.
   - Si no hay stock → notificar sin bloquear.
   - Esto requiere queries transversales que no existen aún.

3. **Transaccionalidad:** US-040 debe ser **atómica** — si falla el email (US-041), la orden ya fue completada. El doc dice explícitamente que el fallo de email NO revierte la orden. Esto requiere diseño cuidadoso con `@Transactional` + event-driven para el email.

4. **US-042 (asignación manual):** Bypass completo del flujo comercial — crea suscripción sin reservación ni orden. Es un "shortcut" que necesita su propio servicio, no puede reutilizar el flujo normal.

**Razonamiento requerido:** **Alto.** Diseño de orquestador, decisiones de transaccionalidad, algoritmo de sugerencia, manejo de fallos parciales.

**Código existente:** `SubscriptionService` tiene `assign()` parcial. `ProfileService` tiene cambio de estado. Pero el **orquestador** que los conecta no existe.

**Estimación:** ~3-4 horas de sesión con Opus.

---

### 🔴 EPIC-07 — Suscripciones (Opus)

**US:** 043 (Listar) · 044 (Detalle) · 045 (Renovar) · 046 (Revocar) · 047 (Detectar vencidas) · 048 (Crear manual)

**¿Por qué es compleja?**

1. **US-045: Renovación con BR-07.** La regla de negocio de renovación es la más compleja del proyecto:
   - Si pago ≤ 2 días post-vencimiento → nueva `due_date` = vieja `due_date` + duración.
   - Si pago > 2 días post-vencimiento → nueva `due_date` = fecha de pago + duración.
   - Incrementar `months_paid`, mantener perfil `active`, enviar email.
   - Esto requiere **razonamiento temporal** y manejo de edge cases (¿qué pasa si la suscripción ya está `cancelled`? ¿qué si el perfil fue reasignado?).

2. **US-047: Cron job de detección.** Infraestructura nueva:
   - `@Scheduled` que corre diariamente.
   - Identifica `active` con `due_date = today`.
   - Transita suscripción → `suspended`, perfil → `expired`.
   - Envía notificación consolidada al vendedor.
   - Debe ser **idempotente** (si corre 2 veces el mismo día, no duplica transiciones).

3. **US-046: Revocar.** Cascada inversa: suscripción → `cancelled`, perfil → `available`. Debe validar que no esté ya cancelada.

4. **US-048: Crear manual.** Bypass del flujo comercial (similar a US-042). Reutilizable parcialmente.

**Razonamiento requerido:** **Alto.** BR-07 es la regla más sutil del proyecto. El cron job necesita diseño de idempotencia. Las cascadas de estado entre suscripción ↔ perfil son bidireccionales.

**Código existente:** `Subscription` domain, entity, mapper, repository, controller — todo scaffolded. `UpdateSubscriptionService` existe con `suspend()` y `terminate()`. Pero la lógica de renovación, cron, y creación manual son greenfield.

**Estimación:** ~3-4 horas de sesión con Opus.

---

### 🟡 EPIC-08 — Notificaciones (Sonnet + Opus para infraestructura)

**US:** 049-056 (8 tipos de email)

**¿Por qué es media?**

- **Infraestructura greenfield:** No existe módulo de notificaciones. Hay que crear:
  - `notification_log` table (Flyway migration)
  - `NotificationLog` domain/entity/repository
  - `EmailService` (integración con proveedor SMTP / SendGrid / etc.)
  - Email templates (Thymeleaf o similar)

- **Cada US individual es mecánica:** Una vez que la infraestructura existe, cada US es:
  ```
  1. Detectar evento → 2. Construir payload → 3. Enviar email → 4. Loggear en notification_log
  ```

- **US-054 (recordatorios 7d/3d/1d)** es la más compleja — requiere un segundo cron job + verificación de duplicados en `notification_log`.

**Razonamiento requerido:**
- **Alto** para el setup de infraestructura (diseño del `EmailService`, elección de SMTP, templates, retry policy).
- **Bajo** para cada US individual después del setup.

**Recomendación:** Usar **Opus** para la primera sesión (crear infraestructura + US-053/054), luego **Sonnet** para el resto.

**Estimación:** ~2 horas infraestructura + ~1 hora por batch de US.

---

### 🟢 EPIC-09 — Panel del Cliente (Gemini)

**US:** 057-062

**¿Por qué es simple y no es para Claude?**

Este EPIC es **100% frontend** (`/apps/store` — Angular 16). No requiere endpoints nuevos del backend — todos los datos ya están disponibles vía las APIs creadas en EPIC-04 a EPIC-07.

- US-057/058: Consumir `GET /subscriptions` filtrado por clientId.
- US-059/060: Consumir `GET /orders` filtrado por clientId.
- US-061: Llamar al endpoint de renovación existente.
- US-062: Llamar al endpoint de edición de cliente.

**Modelo:** **Gemini** (frontend agent per AGENTS.md).

---

### 🟡 EPIC-10 — KPIs del Vendedor (Sonnet)

**US:** 063-067

**¿Por qué es media?**

- No hay módulo de KPIs — es **greenfield**, pero son queries de agregación puras.
- Cada US es un endpoint `GET` que ejecuta una query con `GROUP BY` / `COUNT` / `SUM` sobre datos existentes.
- No hay mutación de estado, no hay transacciones, no hay orquestación.

**La complejidad está en:**
- US-063: Query con rangos de fechas (`hoy`, `mañana`, `esta semana`) + join suscripciones ↔ clientes ↔ perfiles.
- US-067: Cálculo financiero `SUM(price_sold - discount_applied)` con filtro de período + moneda.
- Las queries JPQL pueden ser verbose pero no difíciles conceptualmente.

**Razonamiento requerido:** Medio. Diseño de DTOs de respuesta y optimización de queries. Sin decisiones arquitectónicas.

**Estimación:** ~1.5-2 horas de sesión.

---

### 🟢 EPIC-11 — Migración (Sonnet)

**US:** 068-072

**¿Por qué es simple?**

- **US-068** = reutiliza **US-031** (crear cliente manual). Mismo endpoint, mismo servicio.
- **US-069** = ya cubierto por **EPIC-02** (POST /services).
- **US-070** = ya cubierto por **US-022** (POST /accounts).
- **US-071** = reutiliza **US-048** (crear suscripción manual).
- **US-072** = no es código — es validación con los KPIs de EPIC-10.

Es un EPIC de **reutilización**. La única pieza nueva potencial es carga masiva (bulk import), pero el doc dice "según implementación técnica" — puede ser individual.

**Razonamiento requerido:** Bajo. Es wiring de endpoints existentes.

**Estimación:** ~30 min - 1 hora (mayormente validación).
