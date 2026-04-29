-- ---------------------------------------------------------------
-- V25: Subscription financial snapshots
-- EPIC-07 / US-044: Preserve commercial values used at subscription
-- creation time so detail views and KPIs are not affected by later
-- catalog/account changes.
-- ---------------------------------------------------------------
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS service_id BIGINT REFERENCES services (id),
    ADD COLUMN IF NOT EXISTS price_sold NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS discount_applied NUMERIC(10, 2),
    ADD COLUMN IF NOT EXISTS sale_mode VARCHAR(20);

CREATE INDEX IF NOT EXISTS idx_subscriptions_service_id ON subscriptions (service_id);
