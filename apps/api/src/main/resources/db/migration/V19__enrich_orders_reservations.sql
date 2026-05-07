-- ---------------------------------------------------------------
-- V19: Enrich orders + reservations for EPIC-05 (Órdenes y Comprobantes)
--
-- Orders:   ADD client_id, payment_method, approved_at
-- Reservations: ADD payment_method
--
-- Additive-only — no data loss. All new columns are nullable.
-- ---------------------------------------------------------------

-- 1. Orders: add client_id FK, payment_method, approved_at
ALTER TABLE orders ADD COLUMN IF NOT EXISTS client_id       BIGINT       REFERENCES clients (id);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS payment_method  VARCHAR(50);
ALTER TABLE orders ADD COLUMN IF NOT EXISTS approved_at     TIMESTAMPTZ;

CREATE INDEX IF NOT EXISTS idx_orders_client_id ON orders (client_id);

-- 2. Reservations: add payment_method (provided by client at checkout, flows to order)
ALTER TABLE reservations ADD COLUMN IF NOT EXISTS payment_method VARCHAR(50);
