-- ---------------------------------------------------------------
-- V35: Create games table
-- Add catalog for game products sold in the app store.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS games (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid       UUID NOT NULL DEFAULT gen_random_uuid(),
    vendor_id  BIGINT NOT NULL REFERENCES vendors (id),
    code       VARCHAR(25) NOT NULL, -- e.g. ff-100, ff-310
    name       VARCHAR(100) NOT NULL,
    price      NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    image_url  VARCHAR(500) NULL,
    is_active  BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_games_price CHECK (price >= 0.00)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_games_uuid ON games (uuid);
CREATE INDEX IF NOT EXISTS idx_games_vendor_id ON games (vendor_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_games_vendor_code ON games (vendor_id, code);
