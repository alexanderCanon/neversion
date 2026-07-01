-- ---------------------------------------------------------------
-- V34: Loyalty points program (rewards)
-- Adds vendor-configurable earn percentage and an append-only
-- ledger table for points movements (earn / redeem / adjustment / reversal).
-- ---------------------------------------------------------------

-- Vendor-configurable rewards configuration: { "enabled": true, "earn_pct": 2.0 }
ALTER TABLE vendors
    ADD COLUMN IF NOT EXISTS rewards_cfg JSONB NULL;

-- Snapshot of points redeemed at checkout (BR-style denormalization, mirrors discount/total)
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS points_redeemed BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS points_discount NUMERIC(10, 2) NOT NULL DEFAULT 0;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS points_redeemed BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS points_discount NUMERIC(10, 2) NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS client_points_ledger (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid            UUID NOT NULL DEFAULT gen_random_uuid() UNIQUE,
    client_id       BIGINT NOT NULL REFERENCES clients (id),
    vendor_id       BIGINT NOT NULL REFERENCES vendors (id),
    order_id        BIGINT NULL REFERENCES orders (id),
    reservation_id  BIGINT NULL REFERENCES reservations (id),
    entry_type      VARCHAR(20) NOT NULL, -- EARN, REDEEM, ADJUSTMENT, REVERSAL
    status          VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE', -- AVAILABLE, PENDING, CANCELLED
    points          BIGINT NOT NULL, -- positive for EARN/ADJUSTMENT(+)/REVERSAL(+), negative for REDEEM/ADJUSTMENT(-)
    notes           TEXT NULL,
    created_by      VARCHAR(255) NULL, -- Supabase externalId when manually adjusted by a vendor
    created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_points_ledger_client_id ON client_points_ledger (client_id);
CREATE INDEX IF NOT EXISTS idx_points_ledger_vendor_id ON client_points_ledger (vendor_id);
CREATE INDEX IF NOT EXISTS idx_points_ledger_order_id ON client_points_ledger (order_id);
CREATE INDEX IF NOT EXISTS idx_points_ledger_reservation_id ON client_points_ledger (reservation_id);
