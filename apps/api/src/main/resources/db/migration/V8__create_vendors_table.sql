-- ---------------------------------------------------------------
-- V8: Create vendors table
-- US-002: Each vendor is a business operating independently
--         within the platform. Linked to a user with role 'vendor'.
-- discount_cfg JSONB structure documented in er-diagram.md:
--   { "min_items": 2, "tiers": [{ "from": 2, "to": 3, "discount_pct": 5 }] }
-- ADR-02: multi-tenancy via vendor_id on all core tables.
-- BR-13: discount tiers applied over total cart item count.
-- ---------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vendors (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid         UUID         NOT NULL DEFAULT gen_random_uuid(),
    user_id      BIGINT       NOT NULL REFERENCES users (id),
    store_name   VARCHAR(255) NOT NULL,
    logo_url     VARCHAR(500),
    bank_details JSONB,
    discount_cfg JSONB,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_vendors_uuid    ON vendors (uuid);
CREATE UNIQUE INDEX IF NOT EXISTS idx_vendors_user_id ON vendors (user_id);
