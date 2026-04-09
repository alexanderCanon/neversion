# Neversion — Contexto de Integración n8n + Sistema Existente

## Fecha de este documento
2026-04-01

---

## Situación actual

El usuario tiene un sistema de gestión de cuentas de streaming **ya en desarrollo**:

| Capa | Tecnología |
|---|---|
| Backend | Spring Boot (Java) |
| Frontend | Angular 17 (panel admin) |
| Base de datos | Supabase (PostgreSQL) |
| Auth | Configurada (probablemente Spring Security + JWT) |
| CI/CD | Configurado |

**El dolor principal identificado:** el sistema no tenía manejo de notificaciones de renovación. Se decidió resolver esto con n8n como motor de automatización, en lugar de construirlo dentro del backend.

---

## Qué se construyó hoy (sesión 2026-04-01)

### Workflow n8n: Autofication — Streaming Renewals
- **URL instancia:** `https://automation.neversion.com`
- **Workflow ID:** `uzRMsMJAngKHaBL1`
- **Estado:** ✅ Funcionando y testeado

**Flujo del workflow:**
```
Schedule Trigger (8am diario)
  ├── Query Upcoming (Postgres) → Tag Upcoming ─┐
  └── Query Log (Postgres)     → Tag Log       ─┤
                                                Merge
                                                  ↓
                                          Compute Pending (JS)
                                                  ↓
                                           Has Pending? (IF)
                                          ┌──────┴──────┐
                                    [true]              [false]
                              Build Groq Prompt      No Pending Today
                              + Prepare Log Row
                                    ↓
                              Call Groq API (llama-3.3-70b)
                                    ↓
                              Send Email (Gmail)
                              Append To Log (Postgres)
```

**Lógica de notificaciones:**
- Hitos: 7d, 3d, 1d antes del vencimiento + día de vencimiento + overdue
- Anti-duplicado: cruza contra `notification_log` antes de enviar
- Cubre dos tipos: vencimiento de suscripción de cliente Y renovación de cuenta matriz

---

## Schema de Base de Datos (diseñado hoy)

Este schema debe vivir en Supabase y **reemplazar o convivir** con el schema existente del backend Spring.

```sql
CREATE TABLE services (
  id           SERIAL PRIMARY KEY,
  name         VARCHAR(100) NOT NULL UNIQUE,
  max_profiles INT NOT NULL DEFAULT 5
);

CREATE TABLE accounts (
  id              SERIAL PRIMARY KEY,
  service_id      INT NOT NULL REFERENCES services(id),
  email           VARCHAR(255) NOT NULL,
  password        VARCHAR(255) NOT NULL,
  renewal_date    DATE NOT NULL,
  plan            VARCHAR(50),
  sale_mode       VARCHAR(20) NOT NULL DEFAULT 'by_profile',
  -- 'full' = cuenta completa a 1 cliente
  -- 'by_profile' = vendida por slot de perfil
  notes           TEXT,
  created_at      TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE profiles (
  id         SERIAL PRIMARY KEY,
  account_id INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  name       VARCHAR(100) NOT NULL,
  pin        VARCHAR(10),
  is_owner   BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE clients (
  id         SERIAL PRIMARY KEY,
  name       VARCHAR(150) NOT NULL,
  phone      VARCHAR(30),
  notes      TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE subscriptions (
  id               SERIAL PRIMARY KEY,
  client_id        INT NOT NULL REFERENCES clients(id),
  profile_id       INT NOT NULL REFERENCES profiles(id),
  start_date       DATE NOT NULL DEFAULT CURRENT_DATE,
  payment_due_date DATE NOT NULL,
  months_paid      INT NOT NULL DEFAULT 1,
  status           VARCHAR(20) NOT NULL DEFAULT 'active',
  -- 'active' | 'pending' | 'suspended' | 'cancelled'
  notes            TEXT,
  created_at       TIMESTAMPTZ DEFAULT NOW()
);

-- Vista para el workflow n8n (renovaciones de clientes)
CREATE VIEW upcoming_renewals AS
SELECT
  s.id              AS subscription_id,
  c.name            AS client_name,
  c.phone           AS client_phone,
  sv.name           AS service_name,
  a.email           AS account_email,
  p.name            AS profile_name,
  s.payment_due_date,
  s.status,
  s.months_paid,
  (s.payment_due_date - CURRENT_DATE) AS days_until_due
FROM subscriptions s
JOIN profiles   p  ON p.id  = s.profile_id
JOIN accounts   a  ON a.id  = p.account_id
JOIN services   sv ON sv.id = a.service_id
JOIN clients    c  ON c.id  = s.client_id
WHERE s.status IN ('active', 'pending');

-- Vista para el workflow n8n (renovaciones de cuentas matriz)
CREATE VIEW upcoming_account_renewals AS
SELECT
  a.id,
  sv.name        AS service_name,
  a.email,
  a.renewal_date,
  (a.renewal_date - CURRENT_DATE) AS days_until_due
FROM accounts a
JOIN services sv ON sv.id = a.service_id
WHERE a.renewal_date >= CURRENT_DATE - INTERVAL '30 days';

-- Log de notificaciones enviadas (usado por n8n para deduplicar)
CREATE TABLE notification_log (
  id          SERIAL PRIMARY KEY,
  entity_type VARCHAR(20) NOT NULL,  -- 'subscription' | 'account'
  entity_id   INT NOT NULL,
  stage       VARCHAR(20) NOT NULL,  -- '7d' | '3d' | '1d' | 'due' | 'overdue'
  sent_at     TIMESTAMPTZ DEFAULT NOW(),
  UNIQUE (entity_type, entity_id, stage)
);
```

---

## Integración con el backend Spring

### Estrategia recomendada: Spring gestiona CRUD, n8n gestiona eventos

```
Angular (panel admin)
        ↓
Spring Boot API
        ↓
Supabase PostgreSQL  ←→  n8n (solo lee DB, no hace CRUD de negocio)
```

n8n **solo** hace:
- Leer `upcoming_renewals` y `upcoming_account_renewals`
- Leer y escribir `notification_log`
- Enviar emails via Gmail

Spring hace todo lo demás:
- CRUD de clientes, cuentas, perfiles, suscripciones
- Cambio de status (active → suspended cuando no paga)
- Autenticación y autorización
- Exponer API al panel Angular

### Refactor requerido en Spring

Si el schema actual del backend es diferente, los cambios mínimos son:

1. **Migración de tablas** — adaptar las entidades JPA al schema nuevo
2. **Nuevas vistas** — crear `upcoming_renewals` y `upcoming_account_renewals` en Supabase
3. **Tabla `notification_log`** — agregar (Spring no necesita tocarla, es exclusiva de n8n)
4. **`sale_mode` en accounts** — si no existe, agregar columna
5. **`is_owner` en profiles** — si no existe, agregar columna

### Entidades JPA aproximadas

```java
// services → ServiceEntity
// accounts → AccountEntity (con @ManyToOne a ServiceEntity)
// profiles → ProfileEntity (con @ManyToOne a AccountEntity)
// clients → ClientEntity
// subscriptions → SubscriptionEntity (con @ManyToOne a ClientEntity y ProfileEntity)
```

---

## Credenciales n8n (referencia)

| Credencial en n8n | ID | Uso |
|---|---|---|
| Postgres account | `rccc9gCNmt4d3v8m` | Supabase DB |
| Gmail account | `QajhIYPqHEYqz2qU` | Envío de emails |
| Header Auth account | `qwTplD04Iybtuj8Z` | Groq API Key |

**Groq credential config:**
- Header Name: `Authorization`
- Header Value: `Bearer gsk_...`

---

## Para la próxima sesión

### MCPs a tener activos
- **n8n MCP** (`n8n-mcp`) — para modificar workflows directamente sin REST API manual
- **Supabase MCP** — para ejecutar queries, ver tablas, manejar schema
- **GitHub MCP** (recomendado) — si el proyecto Spring/Angular está en GitHub

### Contexto que dar al inicio de la próxima sesión
- Proyecto: `neversion` — gestión de cuentas de streaming
- Backend: Spring Boot + Supabase en producción
- Workflow n8n funcionando: ID `uzRMsMJAngKHaBL1`
- Pendiente: refactor del schema Spring para alinearlo con el modelo ER diseñado

### Próximos pasos concretos
1. Comparar schema actual de Spring vs. schema diseñado hoy → identificar delta
2. Escribir script de migración SQL
3. Actualizar entidades JPA en Spring
4. Probar workflow n8n contra datos reales del sistema
5. Conectar panel Angular para gestión de suscripciones (CRUD)
