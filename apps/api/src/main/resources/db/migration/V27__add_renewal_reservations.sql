-- ---------------------------------------------------------------
-- V27: Link renewal reservations to existing subscriptions
-- EPIC-09 / US-061: A client can request a renewal by creating a
-- reservation tied to one of their own subscriptions. The actual
-- subscription renewal happens only after vendor approval.
-- ---------------------------------------------------------------
ALTER TABLE reservations
    ADD COLUMN IF NOT EXISTS renewal_subscription_id BIGINT REFERENCES subscriptions (id);

CREATE INDEX IF NOT EXISTS idx_reservations_renewal_subscription_id
    ON reservations (renewal_subscription_id);

CREATE UNIQUE INDEX IF NOT EXISTS ux_reservations_active_renewal_subscription
    ON reservations (renewal_subscription_id)
    WHERE renewal_subscription_id IS NOT NULL
      AND status IN ('pending', 'uploaded');
