-- ---------------------------------------------------------------
-- V13: Link subscriptions to vendors
-- US-007: Each subscription must be scoped to a vendor
--         for multi-tenancy isolation (ADR-02).
-- Column is NULLABLE — no live data to migrate.
-- ---------------------------------------------------------------
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS vendor_id BIGINT REFERENCES vendors (id);

CREATE INDEX IF NOT EXISTS idx_subscriptions_vendor_id ON subscriptions (vendor_id);
