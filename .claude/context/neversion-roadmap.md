# Neversion — Plan de Producto y Arquitectura

## Contexto
Negocio de venta de accesos a servicios de streaming (Netflix, Disney+, Spotify, etc.).
El dolor principal: **no perder fechas de renovación** de cuentas y pagos de clientes.

---

## Stack Definido

| Capa | Tecnología | Rol |
|---|---|---|
| Base de datos | Supabase (PostgreSQL) | Única fuente de verdad |
| Automatizaciones | notification-service | Notificaciones, jobs, eventos asíncronos |
| Backend | Spring Boot 3 / Java 17 | Lógica de negocio, auth, API |
| Frontend | Angular 17 | Panel admin, tienda |

---

## Fases

### ✅ Fase 0 — Completada
- Lógica base de notificaciones: hitos 7d / 3d / 1d / due / overdue con log de auditoría
- Arquitectura de microservicio de notificaciones validada

---

### 🔧 Fase 1 — Supabase + Notificaciones (AHORA)

#### Paso 1: Crear proyecto en Supabase
1. Ir a [supabase.com](https://supabase.com) → New project
2. Guardar: `Project URL`, `anon key`, `service_role key`, `DB password`
3. Ir a **SQL Editor** y ejecutar el schema de abajo

#### Paso 2: Ejecutar schema SQL
Ver sección **Schema** más abajo.

#### Paso 3: Cargar datos iniciales desde el sheet
Migrar manualmente (o via CSV import en Supabase) los datos actuales de `neversion-db`:
- Crear los servicios (Netflix, Disney Plus, etc.)
- Crear las cuentas por servicio
- Crear los perfiles por cuenta
- Crear los clientes
- Crear las suscripciones con sus fechas de pago

---

### 🔲 Fase 2 — Panel Admin
- Backend API (Spring Boot) sobre Supabase
- Panel web para gestionar: clientes, cuentas, perfiles, suscripciones
- Cambio de status manual (activo → suspendido)
- Vista de "tablero" con renovaciones próximas

---

### 🔲 Fase 3 — Tienda + Pagos
- Tienda donde clientes ven servicios disponibles y compran su acceso
- Integración de pagos (Stripe, o pasarela local)
- Flujo: cliente paga → suscripción se activa automáticamente → se envían credenciales
- Renovación automática o recordatorio previo al vencimiento

---

## Schema SQL — Supabase

```sql
-- Servicios de streaming
CREATE TABLE services (
  id        SERIAL PRIMARY KEY,
  name      VARCHAR(100) NOT NULL UNIQUE,  -- 'Netflix', 'Spotify', etc.
  max_profiles INT NOT NULL DEFAULT 5       -- perfiles máx por cuenta
);

-- Cuentas (las que tú compras al servicio)
CREATE TABLE accounts (
  id               SERIAL PRIMARY KEY,
  service_id       INT NOT NULL REFERENCES services(id),
  email            VARCHAR(255) NOT NULL,
  password         VARCHAR(255) NOT NULL,
  renewal_date     DATE NOT NULL,           -- cuando tú pagas al servicio
  plan             VARCHAR(50),             -- 'Familiar', 'Individual', etc.
  sale_mode        VARCHAR(20) NOT NULL DEFAULT 'by_profile',
                   -- 'full'       = vendida completa a 1 cliente
                   -- 'by_profile' = vendida por perfil individual
  notes            TEXT,
  created_at       TIMESTAMPTZ DEFAULT NOW()
);

-- Perfiles dentro de cada cuenta
CREATE TABLE profiles (
  id         SERIAL PRIMARY KEY,
  account_id INT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
  name       VARCHAR(100) NOT NULL,
  pin        VARCHAR(10),
  is_owner   BOOLEAN NOT NULL DEFAULT FALSE, -- TRUE = slot personal tuyo, no se vende
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Clientes (las personas que te compran)
CREATE TABLE clients (
  id         SERIAL PRIMARY KEY,
  name       VARCHAR(150) NOT NULL,
  phone      VARCHAR(30),
  notes      TEXT,
  created_at TIMESTAMPTZ DEFAULT NOW()
);

-- Suscripciones (qué cliente paga por qué perfil)
CREATE TABLE subscriptions (
  id               SERIAL PRIMARY KEY,
  client_id        INT NOT NULL REFERENCES clients(id),
  profile_id       INT NOT NULL REFERENCES profiles(id),
  start_date       DATE NOT NULL DEFAULT CURRENT_DATE,
  payment_due_date DATE NOT NULL,           -- próxima fecha de pago
  months_paid      INT NOT NULL DEFAULT 1,  -- cuántos meses pagó (1, 2, 3...)
  status           VARCHAR(20) NOT NULL DEFAULT 'active',
                   -- 'active'    = acceso vigente
                   -- 'pending'   = esperando pago para renovar
                   -- 'suspended' = acceso revocado por falta de pago
                   -- 'cancelled' = cancelado definitivamente
  notes            TEXT,
  created_at       TIMESTAMPTZ DEFAULT NOW(),

  CONSTRAINT unique_active_profile UNIQUE (profile_id, status)
    DEFERRABLE INITIALLY DEFERRED
);

-- Vista útil: renovaciones próximas
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

-- Vista: renovaciones de cuentas (las que tú pagas al servicio)
CREATE VIEW upcoming_account_renewals AS
SELECT
  a.id,
  sv.name        AS service_name,
  a.email,
  a.renewal_date,
  (a.renewal_date - CURRENT_DATE) AS days_until_due
FROM accounts a
JOIN services sv ON sv.id = a.service_id
WHERE a.renewal_date >= CURRENT_DATE - INTERVAL '7 days';

-- Log de notificaciones enviadas
CREATE TABLE notification_log (
  id         SERIAL PRIMARY KEY,
  entity_type VARCHAR(20) NOT NULL, -- 'subscription' | 'account'
  entity_id  INT NOT NULL,
  stage      VARCHAR(20) NOT NULL,  -- '7d', '3d', '1d', 'due', 'overdue'
  sent_at    TIMESTAMPTZ DEFAULT NOW(),

  UNIQUE (entity_type, entity_id, stage)
);
```

---

## Datos de ejemplo para arrancar

```sql
-- Insertar servicios
INSERT INTO services (name, max_profiles) VALUES
  ('Netflix', 5),
  ('Disney Plus', 4),
  ('Prime Video', 3),
  ('HBO Max', 5),
  ('Spotify', 6),
  ('IPTV', 1),
  ('Crunchyroll', 4),
  ('Youtube Premium', 6);
```

---

## Próxima sesión
1. Confirmar schema creado en Supabase
2. Migrar datos del sheet a Supabase (manual o CSV)
3. Test end-to-end del microservicio de notificaciones con datos reales
4. Test end-to-end del workflow con datos reales
