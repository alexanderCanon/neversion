-- ---------------------------------------------------------------
-- V11: Normalize services pricing columns
-- US-005: Explicit pricing columns replace JSONB-embedded pricing
--         for direct SQL queries, validations, and calculations.
-- The 'details' JSONB field is kept for unstructured metadata only.
-- No live data to migrate from JSONB.
-- ---------------------------------------------------------------
ALTER TABLE services
    ADD COLUMN IF NOT EXISTS description   TEXT,
    ADD COLUMN IF NOT EXISTS image_url     VARCHAR(500),
    ADD COLUMN IF NOT EXISTS price_profile NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS price_full    NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS duration_days INT,
    ADD COLUMN IF NOT EXISTS is_active     BOOLEAN NOT NULL DEFAULT true;
