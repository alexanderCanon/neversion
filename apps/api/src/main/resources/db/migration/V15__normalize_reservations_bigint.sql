-- ---------------------------------------------------------------
-- V15: Normalize reservations + reservation_details PK UUID → BIGINT
-- US-009 + US-010: Convert both tables to BIGINT IDENTITY PKs.
-- Also updates orders.reservation_id from UUID to BIGINT.
-- DESTRUCTIVE: drops and recreates. No live data (confirmed).
-- ---------------------------------------------------------------

-- 1. Drop dependents first
DROP TABLE IF EXISTS reservation_details;

-- 2. Drop orders.reservation_id (UUID) before changing reservations PK
-- orders was recreated in V14 with reservation_id UUID — change to BIGINT
ALTER TABLE orders DROP COLUMN IF EXISTS reservation_id;

-- 3. Drop and recreate reservations
DROP TABLE IF EXISTS reservations;

CREATE TABLE reservations (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid            UUID           NOT NULL DEFAULT gen_random_uuid(),
    client_id       BIGINT         REFERENCES clients (id),
    vendor_id       BIGINT         REFERENCES vendors (id),
    discount        NUMERIC(10, 2),
    total           NUMERIC(10, 2),
    receipt_url     TEXT UNIQUE,
    status          VARCHAR(20)    NOT NULL DEFAULT 'pending',
    expiration_date TIMESTAMPTZ,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_reservations_uuid      ON reservations (uuid);
CREATE INDEX IF NOT EXISTS idx_reservations_client_id ON reservations (client_id);
CREATE INDEX IF NOT EXISTS idx_reservations_vendor_id ON reservations (vendor_id);
CREATE INDEX IF NOT EXISTS idx_reservations_status    ON reservations (status);

-- 4. Re-add reservation_id as BIGINT FK on orders
ALTER TABLE orders ADD COLUMN reservation_id BIGINT REFERENCES reservations (id);
CREATE INDEX IF NOT EXISTS idx_orders_reservation_id ON orders (reservation_id);

-- 5. Recreate reservation_details with BIGINT PK
CREATE TABLE reservation_details (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid            UUID           NOT NULL DEFAULT gen_random_uuid(),
    reservation_id  BIGINT         NOT NULL REFERENCES reservations (id),
    service_id      BIGINT         NOT NULL REFERENCES services (id),
    qty             INT            NOT NULL DEFAULT 1,
    unit_price      NUMERIC(10, 2) NOT NULL,
    subtotal        NUMERIC(10, 2) GENERATED ALWAYS AS (qty * unit_price) STORED,
    created_at      TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_reservation_details_uuid ON reservation_details (uuid);
CREATE INDEX IF NOT EXISTS idx_reservation_details_reservation ON reservation_details (reservation_id);
