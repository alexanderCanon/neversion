-- V6: Normalize varchar status columns to lowercase
-- The EnumConverter in Java writes lowercase via attribute.name().toLowerCase().
-- Migration V1 set some DEFAULT values in UPPERCASE (e.g. orders.status DEFAULT 'PENDING').
-- This migration aligns those defaults to lowercase so all records are consistent.
-- Existing uppercase data (created by DB DEFAULTs before JPA was used) is also normalized.

-- ── orders.status ──────────────────────────────────────────────────────────────
-- V1 set: DEFAULT 'PENDING' (UPPERCASE). Java enum OrderStatus writes lowercase.
ALTER TABLE orders ALTER COLUMN status SET DEFAULT 'pending';

-- Normalize any existing UPPERCASE rows (created by DB DEFAULT before JPA ran)
UPDATE orders SET status = LOWER(status) WHERE status != LOWER(status);

-- ── subscriptions.status ────────────────────────────────────────────────────────
-- V1 already set DEFAULT 'active' (lowercase) — but normalize any inconsistencies
UPDATE subscriptions SET status = LOWER(status) WHERE status != LOWER(status);

-- ── reservations.status ─────────────────────────────────────────────────────────
-- V3 set DEFAULT 'PENDING' (UPPERCASE). Java enum ReservationStatus writes lowercase.
ALTER TABLE reservations ALTER COLUMN status SET DEFAULT 'pending';

UPDATE reservations SET status = LOWER(status) WHERE status != LOWER(status);

-- ── accounts.sale_mode ──────────────────────────────────────────────────────────
-- V1 already set DEFAULT 'by_profile' (lowercase), but @Enumerated(EnumType.STRING)
-- may have written UPPERCASE values ('BY_PROFILE'). Switching to SaleModeConverter
-- (EnumConverter) which writes lowercase — normalize any existing UPPERCASE rows.
UPDATE accounts SET sale_mode = LOWER(sale_mode) WHERE sale_mode != LOWER(sale_mode);
