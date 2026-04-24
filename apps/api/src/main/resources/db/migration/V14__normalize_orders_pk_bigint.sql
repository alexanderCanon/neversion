-- ---------------------------------------------------------------
-- V14: Normalize orders table — PK UUID → BIGINT IDENTITY
-- US-008: Convert orders PK to BIGINT for consistency and
--         performance. Add uuid column + vendor_id for multi-tenancy.
-- DESTRUCTIVE: drops and recreates. No live data (confirmed).
-- reservation_id stays as UUID FK until US-009 normalizes reservations.
-- ---------------------------------------------------------------
DROP INDEX IF EXISTS idx_orders_reservation;
DROP TABLE IF EXISTS orders;

CREATE TABLE orders (
    id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid           UUID         NOT NULL DEFAULT gen_random_uuid(),
    reservation_id UUID,
    vendor_id      BIGINT       REFERENCES vendors (id),
    status         VARCHAR(20)  NOT NULL DEFAULT 'pending',
    notes          TEXT,
    created_at     TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_orders_uuid           ON orders (uuid);
CREATE INDEX IF NOT EXISTS idx_orders_reservation_id ON orders (reservation_id);
CREATE INDEX IF NOT EXISTS idx_orders_vendor_id      ON orders (vendor_id);
