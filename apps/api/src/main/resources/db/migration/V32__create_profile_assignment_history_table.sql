-- V32: Create profile_assignment_history table.
-- Stores a snapshot of credentials and profile data at the moment of each
-- assignment. This allows us to:
--   1. Reset profiles safely after subscription cancellation (Spotify).
--   2. Retain a full audit trail of who had what credentials and when.
CREATE TABLE IF NOT EXISTS profile_assignment_history (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    uuid            UUID        NOT NULL DEFAULT gen_random_uuid(),

    -- The profile slot that was assigned.
    profile_id      BIGINT      NOT NULL REFERENCES profiles (id),

    -- The subscription this assignment belongs to.
    subscription_id BIGINT      NOT NULL REFERENCES subscriptions (id),

    -- Snapshot of the master account credentials at assignment time.
    account_email    VARCHAR(255),
    account_password VARCHAR(255),

    -- Snapshot of the profile slot data at assignment time.
    profile_name    VARCHAR(100),
    profile_pin     VARCHAR(20),
    profile_notes   TEXT,

    -- Lifecycle timestamps.
    assigned_at     TIMESTAMPTZ NOT NULL DEFAULT now(),
    released_at     TIMESTAMPTZ,           -- NULL means still active.

    vendor_id       BIGINT      REFERENCES vendors (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS uidx_profile_assignment_history_uuid
    ON profile_assignment_history (uuid);

-- Speeds up the "find open assignment for this profile" query used on revocation.
CREATE INDEX IF NOT EXISTS idx_pah_profile_released
    ON profile_assignment_history (profile_id, released_at)
    WHERE released_at IS NULL;
