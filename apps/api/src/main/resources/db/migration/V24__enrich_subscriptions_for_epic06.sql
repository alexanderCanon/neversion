-- ---------------------------------------------------------------
-- V24: Enrich subscriptions for assignment flow
-- EPIC-06: Link subscriptions to their origin order and store the
--          computed expiration date for access delivery.
-- ---------------------------------------------------------------
ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS order_id BIGINT REFERENCES orders (id),
    ADD COLUMN IF NOT EXISTS end_date DATE;

CREATE INDEX IF NOT EXISTS idx_subscriptions_order_id ON subscriptions (order_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_end_date ON subscriptions (end_date);
